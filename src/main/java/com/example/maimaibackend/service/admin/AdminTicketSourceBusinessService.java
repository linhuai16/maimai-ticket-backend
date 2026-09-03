package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminSaveBannerRequest;
import com.example.maimaibackend.dto.admin.AdminTicketSourceCampaignPublishBannerRequest;
import com.example.maimaibackend.dto.admin.AdminTicketSourceCampaignReviewRequest;
import com.example.maimaibackend.dto.admin.AdminTicketSourceSettlementAdjustmentRequest;
import com.example.maimaibackend.dto.admin.AdminTicketSourceSettlementCreateRequest;
import com.example.maimaibackend.vo.admin.AdminBannerVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 第三方票源后台需要的“管理动作”。
 *
 * 与自动交易链路的职责边界：
 * 1. 履约/物流/退票协同仍由既有 Adapter 服务自动推进；本服务不替代自动任务。
 * 2. 活动素材只负责本地审核和转成本地 Banner；Provider 素材不得直接投放用户端。
 * 3. 账期结算只形成麦麦对 Provider 的应付账单，不改变用户支付/退款事实。
 */
@Service
public class AdminTicketSourceBusinessService {
    private static final DateTimeFormatter SETTLEMENT_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final JdbcTemplate jdbc;
    private final AdminBannerService adminBannerService;

    public AdminTicketSourceBusinessService(JdbcTemplate jdbc, AdminBannerService adminBannerService) {
        this.jdbc = jdbc;
        this.adminBannerService = adminBannerService;
    }

    // ---------------------------------------------------------------------
    // Campaign / promotion
    // ---------------------------------------------------------------------

    public List<Map<String, Object>> campaignAssets(String providerCode, String reviewStatus, Integer limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.asset_id AS assetId,p.provider_code AS providerCode,p.provider_name AS providerName,
                       a.provider_asset_id AS providerAssetId,a.asset_type AS assetType,a.position_code AS positionCode,
                       a.title,a.description,a.image_url AS imageUrl,a.mobile_image_url AS mobileImageUrl,
                       a.target_type AS targetType,a.provider_target_id AS providerTargetId,a.city_codes AS cityCodes,
                       a.start_time AS startTime,a.end_time AS endTime,a.provider_promotion_id AS providerPromotionId,
                       a.review_status AS reviewStatus,a.review_remark AS reviewRemark,a.review_time AS reviewTime,
                       a.banner_id AS bannerId,a.source_enabled AS sourceEnabled,a.source_version AS sourceVersion,
                       a.source_updated_time AS sourceUpdatedTime,a.create_time AS createTime,a.update_time AS updateTime,
                       COALESCE(pm.project_id,pm2.project_id) AS mappedProjectId,
                       COALESCE(pp.title,pp2.title) AS mappedProjectTitle,
                       sm.session_id AS mappedSessionId,ps.station_name AS mappedSessionName
                FROM ticket_source_campaign_asset a
                JOIN ticket_source_provider p ON p.provider_id=a.provider_id
                LEFT JOIN ticket_source_project_mapping pm
                       ON pm.provider_id=a.provider_id AND pm.provider_project_id=a.provider_target_id
                      AND pm.mapping_status='BOUND' AND a.target_type='PROJECT'
                LEFT JOIN performance_project pp ON pp.project_id=pm.project_id
                LEFT JOIN ticket_source_session_mapping sm
                       ON sm.provider_session_id=a.provider_target_id AND sm.mapping_status='BOUND'
                      AND a.target_type='SESSION'
                      AND sm.project_mapping_id IN (SELECT xpm.mapping_id FROM ticket_source_project_mapping xpm WHERE xpm.provider_id=a.provider_id)
                LEFT JOIN ticket_source_project_mapping pm2 ON pm2.mapping_id=sm.project_mapping_id
                LEFT JOIN performance_project pp2 ON pp2.project_id=pm2.project_id
                LEFT JOIN performance_session ps ON ps.session_id=sm.session_id
                WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        if (text(providerCode) != null) {
            sql.append(" AND p.provider_code=?");
            args.add(providerCode.trim().toUpperCase(Locale.ROOT));
        }
        if (text(reviewStatus) != null) {
            sql.append(" AND a.review_status=?");
            args.add(reviewStatus.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY a.asset_id DESC LIMIT ?");
        args.add(safeLimit(limit, 100, 300));
        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    public List<Map<String, Object>> promotions(String providerCode, String status, Integer limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT pr.promotion_id AS promotionId,p.provider_code AS providerCode,
                       pm.provider_project_id AS providerProjectId,pm.provider_project_name AS providerProjectName,
                       pm.project_id AS projectId,pp.title AS projectTitle,
                       pr.provider_promotion_id AS providerPromotionId,pr.promotion_type AS promotionType,
                       pr.promotion_title AS promotionTitle,pr.promotion_description AS promotionDescription,
                       pr.stackable,pr.target_scope_json AS targetScopeJson,pr.rule_data_json AS ruleDataJson,
                       pr.start_time AS startTime,pr.end_time AS endTime,pr.promotion_status AS promotionStatus,
                       pr.source_version AS sourceVersion,pr.source_updated_time AS sourceUpdatedTime,pr.update_time AS updateTime
                FROM ticket_source_promotion_rule pr
                JOIN ticket_source_project_mapping pm ON pm.mapping_id=pr.project_mapping_id
                JOIN ticket_source_provider p ON p.provider_id=pm.provider_id
                LEFT JOIN performance_project pp ON pp.project_id=pm.project_id
                WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        if (text(providerCode) != null) {
            sql.append(" AND p.provider_code=?");
            args.add(providerCode.trim().toUpperCase(Locale.ROOT));
        }
        if (text(status) != null) {
            sql.append(" AND pr.promotion_status=?");
            args.add(status.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY pr.promotion_id DESC LIMIT ?");
        args.add(safeLimit(limit, 100, 300));
        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    @Transactional
    public Map<String, Object> reviewCampaignAsset(Long assetId, AdminTicketSourceCampaignReviewRequest request) {
        requirePositive(assetId, "assetId");
        if (request == null) throw new BusinessException("审核请求不能为空");
        String status = text(request.reviewStatus());
        if (status == null) throw new BusinessException("reviewStatus 不能为空");
        status = status.toUpperCase(Locale.ROOT);
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            throw new BusinessException("reviewStatus 仅支持 APPROVED / REJECTED");
        }
        Map<String, Object> asset = campaignAsset(assetId);
        if ("REJECTED".equals(status) && asset.get("bannerId") != null) {
            throw new BusinessException("该素材已生成本地 Banner，不能直接改为 REJECTED；请先在 Banner 管理中处理本地投放");
        }
        jdbc.update("""
                UPDATE ticket_source_campaign_asset
                SET review_status=?,review_remark=?,review_time=NOW(),update_time=NOW()
                WHERE asset_id=?
                """, status, text(request.reviewRemark()), assetId);
        return campaignAsset(assetId);
    }

    @Transactional
    public Map<String, Object> publishCampaignBanner(Long assetId, AdminTicketSourceCampaignPublishBannerRequest request) {
        requirePositive(assetId, "assetId");
        if (request == null) throw new BusinessException("Banner 发布参数不能为空");
        Map<String, Object> asset = campaignAsset(assetId);
        if (!"BANNER".equalsIgnoreCase(String.valueOf(asset.get("assetType")))) {
            throw new BusinessException("只有 BANNER 类型活动素材可以转成本地 Banner");
        }
        if (!"APPROVED".equalsIgnoreCase(String.valueOf(asset.get("reviewStatus")))) {
            throw new BusinessException("第三方活动素材必须先审核通过，才能生成本地 Banner");
        }
        if (asset.get("bannerId") != null) {
            throw new BusinessException("该活动素材已经绑定本地 Banner #" + asset.get("bannerId"));
        }

        Long projectId = request.targetProjectId() != null ? request.targetProjectId() : longValue(asset.get("mappedProjectId"));
        Long sessionId = request.targetSessionId() != null ? request.targetSessionId() : longValue(asset.get("mappedSessionId"));
        if (projectId == null || projectId <= 0) {
            throw new BusinessException("无法自动解析本地目标项目，请在发布时选择本地演出项目");
        }

        AdminSaveBannerRequest banner = new AdminSaveBannerRequest();
        String title = text(request.bannerTitle());
        banner.setBannerTitle(title == null ? trimMax(String.valueOf(asset.get("title")), 100) : title);
        banner.setImageUrl(request.imageUrl());
        banner.setTargetProjectId(projectId);
        banner.setTargetSessionId(sessionId);
        banner.setEnableStatus(text(request.enableStatus()) == null ? "DISABLED" : request.enableStatus().trim().toUpperCase(Locale.ROOT));
        banner.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        banner.setStartTime(request.startTime() != null ? request.startTime() : localDateTime(asset.get("startTime")));
        banner.setEndTime(request.endTime() != null ? request.endTime() : localDateTime(asset.get("endTime")));

        AdminBannerVO created = adminBannerService.createBanner(banner);
        jdbc.update("""
                UPDATE ticket_source_campaign_asset
                SET banner_id=?,review_status='APPROVED',review_time=COALESCE(review_time,NOW()),update_time=NOW()
                WHERE asset_id=?
                """, created.getBannerId(), assetId);
        return campaignAsset(assetId);
    }

    // ---------------------------------------------------------------------
    // Settlement
    // ---------------------------------------------------------------------

    public List<Map<String, Object>> settlements(String providerCode, String status, Integer limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT period_id AS periodId,settlement_no AS settlementNo,provider_code AS providerCode,
                       date_from AS dateFrom,date_to AS dateTo,period_status AS periodStatus,
                       sale_order_count AS saleOrderCount,refund_order_count AS refundOrderCount,
                       sale_settlement_amount AS saleSettlementAmount,refund_deduction_amount AS refundDeductionAmount,
                       adjustment_amount AS adjustmentAmount,net_payable_amount AS netPayableAmount,currency_code AS currencyCode,
                       confirmed_time AS confirmedTime,close_time AS closeTime,close_mode AS closeMode,remark,
                       create_time AS createTime,update_time AS updateTime
                FROM ticket_source_settlement_period WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        if (text(providerCode) != null) {
            sql.append(" AND provider_code=?");
            args.add(providerCode.trim().toUpperCase(Locale.ROOT));
        }
        if (text(status) != null) {
            sql.append(" AND period_status=?");
            args.add(status.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY period_id DESC LIMIT ?");
        args.add(safeLimit(limit, 100, 300));
        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    public Map<String, Object> settlementDetail(Long periodId) {
        Map<String, Object> period = settlementPeriod(periodId);
        List<Map<String, Object>> details = jdbc.queryForList("""
                SELECT detail_id AS detailId,detail_type AS detailType,source_key AS sourceKey,
                       order_id AS orderId,refund_id AS refundId,provider_order_id AS providerOrderId,
                       reference_no AS referenceNo,business_time AS businessTime,user_amount AS userAmount,
                       provider_settlement_amount AS providerSettlementAmount,amount_effect AS amountEffect,
                       currency_code AS currencyCode,remark,create_time AS createTime
                FROM ticket_source_settlement_detail
                WHERE period_id=? ORDER BY business_time ASC,detail_id ASC
                """, periodId);
        period.put("details", details);
        return period;
    }

    @Transactional
    public Map<String, Object> createSettlement(AdminTicketSourceSettlementCreateRequest request) {
        if (request == null) throw new BusinessException("结算账期参数不能为空");
        String providerCode = text(request.providerCode());
        if (providerCode == null) throw new BusinessException("providerCode 不能为空");
        providerCode = providerCode.toUpperCase(Locale.ROOT);
        LocalDate from = request.dateFrom();
        LocalDate to = request.dateTo();
        if (from == null || to == null) throw new BusinessException("dateFrom/dateTo 不能为空");
        if (to.isBefore(from)) throw new BusinessException("dateTo 不能早于 dateFrom");
        Long providerId = providerId(providerCode);

        Integer overlap = jdbc.queryForObject("""
                SELECT COUNT(*) FROM ticket_source_settlement_period
                WHERE provider_id=? AND NOT (date_to < ? OR date_from > ?)
                """, Integer.class, providerId, from, to);
        if (overlap != null && overlap > 0) {
            throw new BusinessException("该 Provider 已存在与当前日期范围重叠的结算账期，请先检查已有账单");
        }

        String no = "TS-SETTLE-" + providerCode + "-" + from.toString().replace("-", "") + "-" +
                to.toString().replace("-", "") + "-" + LocalDateTime.now().format(SETTLEMENT_NO_TIME);
        jdbc.update("""
                INSERT INTO ticket_source_settlement_period
                (settlement_no,provider_id,provider_code,date_from,date_to,period_status,
                 sale_order_count,refund_order_count,sale_settlement_amount,refund_deduction_amount,
                 adjustment_amount,net_payable_amount,currency_code,remark,create_time,update_time)
                VALUES(?,?,?,?,?,'DRAFT',0,0,0,0,0,0,'CNY',?,NOW(),NOW())
                """, no, providerId, providerCode, from, to, text(request.remark()));
        Long periodId = jdbc.queryForObject("SELECT period_id FROM ticket_source_settlement_period WHERE settlement_no=?", Long.class, no);
        regenerateSettlement(periodId);
        return settlementDetail(periodId);
    }

    @Transactional
    public Map<String, Object> regenerateSettlement(Long periodId) {
        Map<String, Object> period = settlementPeriod(periodId);
        requireDraft(period);
        Long providerId = longValue(period.get("providerId"));
        LocalDate from = localDate(period.get("dateFrom"));
        LocalDate to = localDate(period.get("dateTo"));

        jdbc.update("DELETE FROM ticket_source_settlement_detail WHERE period_id=? AND detail_type IN ('SALE','REFUND')", periodId);

        List<Map<String, Object>> sales = jdbc.queryForList("""
                SELECT o.order_id AS orderId,o.order_no AS orderNo,b.provider_order_id AS providerOrderId,
                       o.pay_time AS businessTime,o.pay_amount AS userAmount,
                       SUM(CASE WHEN COALESCE(oib.settlement_unit_price,oi.settlement_price) IS NULL THEN 1 ELSE 0 END) AS missingSettlementCount,
                       SUM(COALESCE(oib.settlement_unit_price,oi.settlement_price,0) * oib.quantity) AS settlementAmount
                FROM ticket_source_order_bridge b
                JOIN ticket_order o ON o.order_id=b.order_id
                JOIN ticket_source_order_item_bridge oib ON oib.bridge_id=b.bridge_id
                LEFT JOIN order_item oi ON oi.order_item_id=oib.order_item_id
                WHERE b.provider_id=? AND o.fulfillment_mode='TICKET_SOURCE'
                  AND o.pay_time IS NOT NULL AND DATE(o.pay_time) BETWEEN ? AND ?
                  AND o.payment_status IN ('PAID','PROVIDER_CONFIRMED','REFUNDED')
                GROUP BY o.order_id,o.order_no,b.provider_order_id,o.pay_time,o.pay_amount
                ORDER BY o.order_id
                """, providerId, from, to);
        for (Map<String, Object> sale : sales) {
            if (intValue(sale.get("missingSettlementCount")) > 0) {
                throw new BusinessException("订单 #" + sale.get("orderId") + " 缺少 Provider 结算价，不能生成账期结算单");
            }
            BigDecimal settlementAmount = decimal(sale.get("settlementAmount"));
            jdbc.update("""
                    INSERT INTO ticket_source_settlement_detail
                    (period_id,detail_type,source_key,order_id,provider_order_id,reference_no,business_time,
                     user_amount,provider_settlement_amount,amount_effect,currency_code,remark,create_time)
                    VALUES(?,'SALE',?,?,?,?,?,?,?,?,'CNY','销售结算自动生成',NOW())
                    """, periodId, "SALE:" + sale.get("orderId"), sale.get("orderId"), sale.get("providerOrderId"),
                    sale.get("orderNo"), sale.get("businessTime"), sale.get("userAmount"), settlementAmount, settlementAmount);
        }

        List<Map<String, Object>> refunds = jdbc.queryForList("""
                SELECT r.refund_id AS refundId,r.refund_no AS refundNo,r.refund_time AS businessTime,
                       r.refund_amount AS userAmount,o.order_id AS orderId,b.provider_order_id AS providerOrderId,
                       SUM(CASE WHEN COALESCE(oib.settlement_unit_price,oi.settlement_price) IS NULL THEN 1 ELSE 0 END) AS missingSettlementCount,
                       SUM(COALESCE(oib.settlement_unit_price,oi.settlement_price,0) * oib.quantity) AS settlementAmount
                FROM refund_record r
                JOIN ticket_source_refund_bridge rb ON rb.refund_id=r.refund_id
                JOIN ticket_source_order_bridge b ON b.bridge_id=rb.order_bridge_id
                JOIN ticket_order o ON o.order_id=r.order_id
                JOIN ticket_source_order_item_bridge oib ON oib.bridge_id=b.bridge_id
                LEFT JOIN order_item oi ON oi.order_item_id=oib.order_item_id
                WHERE rb.provider_id=? AND r.refund_status='REFUND_SUCCESS' AND r.refund_time IS NOT NULL
                  AND DATE(r.refund_time) BETWEEN ? AND ?
                GROUP BY r.refund_id,r.refund_no,r.refund_time,r.refund_amount,o.order_id,b.provider_order_id
                ORDER BY r.refund_id
                """, providerId, from, to);
        for (Map<String, Object> refund : refunds) {
            if (intValue(refund.get("missingSettlementCount")) > 0) {
                throw new BusinessException("退款 #" + refund.get("refundId") + " 对应订单缺少 Provider 结算价，不能生成退款冲减");
            }
            BigDecimal settlementAmount = decimal(refund.get("settlementAmount"));
            jdbc.update("""
                    INSERT INTO ticket_source_settlement_detail
                    (period_id,detail_type,source_key,order_id,refund_id,provider_order_id,reference_no,business_time,
                     user_amount,provider_settlement_amount,amount_effect,currency_code,remark,create_time)
                    VALUES(?,'REFUND',?,?,?,?,?,?,?,?,?,'CNY','整单退款按原 Provider 结算额冲减；特殊供应商费用使用调整项',NOW())
                    """, periodId, "REFUND:" + refund.get("refundId"), refund.get("orderId"), refund.get("refundId"),
                    refund.get("providerOrderId"), refund.get("refundNo"), refund.get("businessTime"), refund.get("userAmount"),
                    settlementAmount, settlementAmount.negate());
        }
        refreshSettlementTotals(periodId);
        return settlementDetail(periodId);
    }

    @Transactional
    public Map<String, Object> addSettlementAdjustment(Long periodId, AdminTicketSourceSettlementAdjustmentRequest request) {
        Map<String, Object> period = settlementPeriod(periodId);
        requireDraft(period);
        if (request == null || request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("调整金额不能为空且不能为 0");
        }
        String remark = text(request.remark());
        if (remark == null) throw new BusinessException("调整原因不能为空");
        jdbc.update("""
                INSERT INTO ticket_source_settlement_detail
                (period_id,detail_type,source_key,business_time,amount_effect,currency_code,remark,create_time)
                VALUES(?,'ADJUSTMENT',?,NOW(),?,'CNY',?,NOW())
                """, periodId, "ADJ:" + UUID.randomUUID(), request.amount(), trimMax(remark, 500));
        refreshSettlementTotals(periodId);
        return settlementDetail(periodId);
    }

    @Transactional
    public Map<String, Object> confirmSettlement(Long periodId) {
        Map<String, Object> period = settlementPeriod(periodId);
        requireDraft(period);
        refreshSettlementTotals(periodId);
        jdbc.update("""
                UPDATE ticket_source_settlement_period
                SET period_status='CONFIRMED',confirmed_time=NOW(),update_time=NOW()
                WHERE period_id=? AND period_status='DRAFT'
                """, periodId);
        return settlementDetail(periodId);
    }

    @Transactional
    public Map<String, Object> closeSettlement(Long periodId, String mode) {
        Map<String, Object> period = settlementPeriod(periodId);
        if (!"CONFIRMED".equals(String.valueOf(period.get("periodStatus")))) {
            throw new BusinessException("只有 CONFIRMED 账期可以关闭结算");
        }
        String closeMode = text(mode);
        if (closeMode == null) throw new BusinessException("closeMode 不能为空");
        closeMode = closeMode.toUpperCase(Locale.ROOT);
        BigDecimal net = decimal(period.get("netPayableAmount"));
        if ("PAID".equals(closeMode)) {
            if (net.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("本期净应付为负数，应选择 CARRIED_FORWARD 结转下期，而不是标记已付款");
            }
        } else if ("CARRIED_FORWARD".equals(closeMode)) {
            if (net.compareTo(BigDecimal.ZERO) >= 0) {
                throw new BusinessException("只有净应付为负数时才使用 CARRIED_FORWARD；正数账单应在实际付款后标记 PAID");
            }
        } else {
            throw new BusinessException("closeMode 仅支持 PAID / CARRIED_FORWARD");
        }
        jdbc.update("""
                UPDATE ticket_source_settlement_period
                SET period_status=?,close_mode=?,close_time=NOW(),update_time=NOW()
                WHERE period_id=? AND period_status='CONFIRMED'
                """, closeMode, closeMode, periodId);
        return settlementDetail(periodId);
    }

    private void refreshSettlementTotals(Long periodId) {
        Map<String, Object> sums = jdbc.queryForMap("""
                SELECT
                  SUM(CASE WHEN detail_type='SALE' THEN 1 ELSE 0 END) AS saleCount,
                  SUM(CASE WHEN detail_type='REFUND' THEN 1 ELSE 0 END) AS refundCount,
                  COALESCE(SUM(CASE WHEN detail_type='SALE' THEN amount_effect ELSE 0 END),0) AS saleAmount,
                  COALESCE(SUM(CASE WHEN detail_type='REFUND' THEN -amount_effect ELSE 0 END),0) AS refundAmount,
                  COALESCE(SUM(CASE WHEN detail_type='ADJUSTMENT' THEN amount_effect ELSE 0 END),0) AS adjustmentAmount,
                  COALESCE(SUM(amount_effect),0) AS netAmount
                FROM ticket_source_settlement_detail WHERE period_id=?
                """, periodId);
        jdbc.update("""
                UPDATE ticket_source_settlement_period
                SET sale_order_count=?,refund_order_count=?,sale_settlement_amount=?,refund_deduction_amount=?,
                    adjustment_amount=?,net_payable_amount=?,update_time=NOW()
                WHERE period_id=?
                """, intValue(sums.get("saleCount")), intValue(sums.get("refundCount")),
                decimal(sums.get("saleAmount")), decimal(sums.get("refundAmount")),
                decimal(sums.get("adjustmentAmount")), decimal(sums.get("netAmount")), periodId);
    }

    private Map<String, Object> campaignAsset(Long assetId) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT a.asset_id AS assetId,p.provider_code AS providerCode,a.provider_asset_id AS providerAssetId,
                       a.asset_type AS assetType,a.title,a.description,a.image_url AS imageUrl,a.mobile_image_url AS mobileImageUrl,
                       a.target_type AS targetType,a.provider_target_id AS providerTargetId,a.start_time AS startTime,a.end_time AS endTime,
                       a.review_status AS reviewStatus,a.review_remark AS reviewRemark,a.review_time AS reviewTime,a.banner_id AS bannerId,
                       COALESCE(pm.project_id,pm2.project_id) AS mappedProjectId,sm.session_id AS mappedSessionId,a.update_time AS updateTime
                FROM ticket_source_campaign_asset a
                JOIN ticket_source_provider p ON p.provider_id=a.provider_id
                LEFT JOIN ticket_source_project_mapping pm
                       ON pm.provider_id=a.provider_id AND pm.provider_project_id=a.provider_target_id
                      AND pm.mapping_status='BOUND' AND a.target_type='PROJECT'
                LEFT JOIN ticket_source_session_mapping sm
                       ON sm.provider_session_id=a.provider_target_id AND sm.mapping_status='BOUND'
                      AND a.target_type='SESSION'
                      AND sm.project_mapping_id IN (SELECT xpm.mapping_id FROM ticket_source_project_mapping xpm WHERE xpm.provider_id=a.provider_id)
                LEFT JOIN ticket_source_project_mapping pm2 ON pm2.mapping_id=sm.project_mapping_id
                WHERE a.asset_id=?
                """, assetId);
        if (rows.isEmpty()) throw new BusinessException("第三方活动素材不存在");
        return rows.get(0);
    }

    private Map<String, Object> settlementPeriod(Long periodId) {
        requirePositive(periodId, "periodId");
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT period_id AS periodId,settlement_no AS settlementNo,provider_id AS providerId,provider_code AS providerCode,
                       date_from AS dateFrom,date_to AS dateTo,period_status AS periodStatus,
                       sale_order_count AS saleOrderCount,refund_order_count AS refundOrderCount,
                       sale_settlement_amount AS saleSettlementAmount,refund_deduction_amount AS refundDeductionAmount,
                       adjustment_amount AS adjustmentAmount,net_payable_amount AS netPayableAmount,currency_code AS currencyCode,
                       confirmed_time AS confirmedTime,close_time AS closeTime,close_mode AS closeMode,remark,
                       create_time AS createTime,update_time AS updateTime
                FROM ticket_source_settlement_period WHERE period_id=?
                """, periodId);
        if (rows.isEmpty()) throw new BusinessException("结算账期不存在");
        return rows.get(0);
    }

    private Long providerId(String providerCode) {
        List<Long> ids = jdbc.query("SELECT provider_id FROM ticket_source_provider WHERE provider_code=?", (rs, rowNum) -> rs.getLong(1), providerCode);
        if (ids.isEmpty()) throw new BusinessException("Provider 不存在：" + providerCode);
        return ids.get(0);
    }

    private static void requireDraft(Map<String, Object> period) {
        if (!"DRAFT".equals(String.valueOf(period.get("periodStatus")))) {
            throw new BusinessException("只有 DRAFT 账期允许重新生成或调整");
        }
    }

    private static void requirePositive(Long value, String field) {
        if (value == null || value <= 0) throw new BusinessException(field + " 必须为正整数");
    }

    private static String text(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static int safeLimit(Integer value, int def, int max) {
        return value == null ? def : Math.max(1, Math.min(value, max));
    }

    private static String trimMax(String value, int max) {
        if (value == null) return null;
        String v = value.trim();
        return v.length() <= max ? v : v.substring(0, max);
    }

    private static Long longValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(value)); } catch (Exception ignored) { return null; }
    }

    private static int intValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception ignored) { return 0; }
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal b) return b;
        if (value instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(String.valueOf(value));
    }

    private static LocalDate localDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Date d) return d.toLocalDate();
        if (value instanceof LocalDate d) return d;
        return LocalDate.parse(String.valueOf(value));
    }

    private static LocalDateTime localDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp t) return t.toLocalDateTime();
        if (value instanceof LocalDateTime t) return t;
        return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
    }
}
