package com.example.maimaibackend.ticketsource.provider.mock;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.ticketsource.provider.adapter.V11AdapterException;
import com.example.maimaibackend.ticketsource.provider.adapter.V11ErrorCode;
import com.example.maimaibackend.ticketsource.provider.enums.*;
import com.example.maimaibackend.ticketsource.provider.mock.dto.*;
import com.example.maimaibackend.ticketsource.provider.model.*;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.V11ShipmentTransitionPolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MOCK_DAMAI V1.1 完整模拟器。
 *
 * <p>该服务只读写 mock_ticket_source_* 表，不写麦麦核心业务表。旧版模拟器继续保留，
 * V1.1 业务通过独立 Adapter/HTTP 接口调用本服务。</p>
 */
@Service
public class LocalMockV11TicketSourceService {
    private static final ZoneOffset CN_OFFSET = ZoneOffset.ofHours(8);
    private static final String PROVIDER_CODE = "MOCK_DAMAI";
    private static final String API_VERSION = "1.2";
    /** MOCK_DAMAI 动态码：真实供应商时长仍完全以第三方返回为准。 */
    private static final int DYNAMIC_QR_TTL_SECONDS = 120;
    private static final int DYNAMIC_QR_REFRESH_AFTER_SECONDS = 90;

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final LocalMockV11BehaviorService behaviorService;
    private final LocalMockV11ResourceMapper resourceMapper;

    public LocalMockV11TicketSourceService(JdbcTemplate jdbc, ObjectMapper objectMapper,
                                          LocalMockV11BehaviorService behaviorService,
                                          LocalMockV11ResourceMapper resourceMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.behaviorService = behaviorService;
        this.resourceMapper = resourceMapper;
    }

    public ProviderHealth health() {
        applyBehavior("V11_HEALTH");
        long projectCount = longValue(jdbc.queryForObject(
                "SELECT COUNT(*) FROM mock_ticket_source_project WHERE enabled=1", Long.class));
        return new ProviderHealth(
                HealthStatus.UP,
                "MOCK_DAMAI V1.2 available, projects=" + projectCount,
                now(),
                now()
        );
    }

    public ProviderCapabilities capabilities() {
        applyBehavior("V11_CAPABILITIES");
        return new ProviderCapabilities(
                ProviderCode.MOCK_DAMAI,
                ProviderCode.MOCK_DAMAI,
                API_VERSION,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                IssueTriggerMode.EXPLICIT_TRIGGER_REQUIRED,
                true,
                true,
                DynamicQrMode.REMOTE_REFRESH,
                true,
                RefundCapabilityScope.FULL_ORDER_ONLY,
                true,
                true,
                new CallbackCapabilities(true, true, true, true, true, true)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateSkuInventory(String providerSkuId, MockV11SkuInventoryRequest request) {
        if (providerSkuId == null || providerSkuId.isBlank() || request == null) {
            throw new BusinessException("票档ID和库存参数不能为空");
        }
        if (request.availableStock() != null && request.availableStock() < 0) {
            throw new BusinessException("availableStock 不能小于 0");
        }
        Map<String, Object> current = requiredSku(providerSkuId);
        String inventoryMode = request.inventoryMode() == null || request.inventoryMode().isBlank()
                ? String.valueOf(current.get("inventory_mode")) : request.inventoryMode().trim().toUpperCase(Locale.ROOT);
        String saleStatus = request.saleStatus() == null || request.saleStatus().isBlank()
                ? String.valueOf(current.get("sale_status")) : request.saleStatus().trim().toUpperCase(Locale.ROOT);
        if ("UNKNOWN".equals(inventoryMode)) {
            // 兼容旧调用；V11 枚举使用 STATUS_ONLY 表示只提供状态、不提供精确库存。
            inventoryMode = "STATUS_ONLY";
        }
        if (!Set.of("SNAPSHOT", "REALTIME_QUERY", "STATUS_ONLY").contains(inventoryMode)) {
            throw new BusinessException("inventoryMode 仅支持 SNAPSHOT / REALTIME_QUERY / STATUS_ONLY");
        }
        if (!Set.of("PRESALE", "ON_SALE", "SOLD_OUT", "OFFLINE", "ENDED").contains(saleStatus)) {
            throw new BusinessException("saleStatus 非法: " + saleStatus);
        }
        int rows = jdbc.update("""
                UPDATE mock_ticket_source_sku
                SET inventory_mode=?,available_stock=?,sale_status=?,source_status_code=?,source_status_text=?,
                    data_version=CONCAT(source_sku_id,'-inventory-',UNIX_TIMESTAMP()),version=version+1,update_time=NOW()
                WHERE source_sku_id=?
                """, inventoryMode, request.availableStock(), saleStatus, saleStatus,
                request.availableStock() == null ? "模拟库存未知" : (request.availableStock() == 0 ? "模拟明确售罄" : "模拟在售"),
                providerSkuId);
        if (rows != 1) throw new BusinessException("模拟票档不存在: " + providerSkuId);
        Map<String, Object> row = requiredSku(providerSkuId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("providerSkuId", providerSkuId);
        result.put("availableStock", row.get("available_stock"));
        result.put("inventoryMode", row.get("inventory_mode"));
        result.put("saleStatus", row.get("sale_status"));
        result.put("dataVersion", row.get("data_version"));
        return result;
    }

    public Map<String, Object> updateSkuPrice(String providerSkuId, MockV11SkuPriceRequest request) {
        if (providerSkuId == null || providerSkuId.isBlank() || request == null || request.salePrice() == null
                || request.salePrice().signum() < 0) {
            throw new BusinessException("票档ID和销售价不能为空");
        }
        BigDecimal face = request.facePrice() == null ? request.salePrice() : request.facePrice();
        BigDecimal settlement = request.settlementPrice() == null ? request.salePrice() : request.settlementPrice();
        int rows = jdbc.update("""
                UPDATE mock_ticket_source_sku
                SET face_price=?,sale_price=?,settlement_price=?,data_version=CONCAT(source_sku_id,'-price-',UNIX_TIMESTAMP()),
                    version=version+1,update_time=NOW()
                WHERE source_sku_id=?
                """, face, request.salePrice(), settlement, providerSkuId);
        if (rows != 1) throw new BusinessException("模拟票档不存在: " + providerSkuId);
        Map<String,Object> row = requiredSku(providerSkuId);
        return Map.of("providerSkuId", providerSkuId, "facePrice", decimal(row.get("face_price")),
                "salePrice", decimal(row.get("sale_price")), "settlementPrice", decimal(row.get("settlement_price")));
    }

    public ProviderPage<ProviderProjectSummary> queryProjects(ProviderProjectQuery query) {
        applyBehavior("V11_QUERY_PROJECTS");
        ProviderProjectQuery normalized = query == null
                ? new ProviderProjectQuery(null, null, null, null, 1, 20)
                : query;
        StringBuilder where = new StringBuilder(" WHERE p.enabled=1 ");
        List<Object> args = new ArrayList<>();
        if (normalized.keyword() != null) {
            where.append(" AND (p.source_project_name LIKE ? OR p.subtitle LIKE ?) ");
            String keyword = "%" + normalized.keyword() + "%";
            args.add(keyword);
            args.add(keyword);
        }
        if (normalized.cityCode() != null) {
            where.append(" AND p.city_code=? ");
            args.add(normalized.cityCode());
        }
        if (normalized.status() != null) {
            where.append(" AND p.sale_status=? ");
            args.add(toMockProjectStatus(normalized.status()));
        }
        if (normalized.updatedAfter() != null) {
            where.append(" AND p.update_time>? ");
            args.add(toLocal(normalized.updatedAfter()));
        }
        long total = longValue(jdbc.queryForObject(
                "SELECT COUNT(*) FROM mock_ticket_source_project p" + where,
                Long.class,
                args.toArray()
        ));
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add((normalized.pageNo() - 1) * normalized.pageSize());
        pageArgs.add(normalized.pageSize());
        List<Map<String, Object>> rows = jdbc.queryForList(
                projectSelectSql() + where + " ORDER BY p.update_time DESC,p.source_project_id LIMIT ?,?",
                pageArgs.toArray()
        );
        return new ProviderPage<>(rows.stream().map(resourceMapper::toProjectSummary).toList(),
                total, normalized.pageNo(), normalized.pageSize());
    }

    public ProviderProjectDetail getProject(String projectId) {
        applyBehavior("V11_GET_PROJECT");
        Map<String, Object> row = requiredOne(
                projectSelectSql() + " WHERE p.enabled=1 AND p.source_project_id=?",
                "MOCK_PROJECT_NOT_FOUND", "模拟项目不存在: " + projectId, projectId);
        ProviderProjectSummary summary = resourceMapper.toProjectSummary(row);
        List<ProviderNotice> notices = new ArrayList<>(jdbc.queryForList(
                "SELECT notice_code,title,content,priority FROM mock_ticket_source_project_notice " +
                        "WHERE source_project_id=? AND enabled=1 ORDER BY priority,notice_id", projectId
        ).stream().map(r -> resourceMapper.toNotice(r, projectId)).toList());
        List<ProviderServiceCapability> serviceCapabilities = jdbc.queryForList(
                "SELECT capability_code,enabled,display_text,source_code " +
                        "FROM mock_ticket_source_project_capability WHERE source_project_id=? ORDER BY capability_code",
                projectId
        ).stream().map(r -> new ProviderServiceCapability(
                str(r, "capability_code"), bool(r.get("enabled")),
                str(r, "display_text"), str(r, "source_code")
        )).toList();
        Integer limit = nullableInt(row.get("purchase_limitation_once"));
        if (limit != null && limit > 0
                && notices.stream().noneMatch(n -> "PURCHASE_LIMIT".equals(n.noticeCode()))) {
            notices.add(new ProviderNotice("PURCHASE_LIMIT", "限购说明",
                    "立即购买每单最多" + limit + "张，具体以提交订单页展示为准。",
                    "PROJECT", projectId, 30));
        }
        ProviderRefundPolicy refundPolicy = resourceMapper.loadRefundPolicy(projectId);
        refundPolicy = resourceMapper.completeRefundPolicy(notices, refundPolicy);
        if (refundPolicy != null && notices.stream().noneMatch(n -> "REFUND_NOTICE".equals(n.noticeCode()))
                && refundPolicy.sourceRuleText() != null && !refundPolicy.sourceRuleText().isBlank()) {
            notices.add(new ProviderNotice("REFUND_NOTICE", "退票/换票规则", refundPolicy.sourceRuleText(),
                    "PROJECT", projectId, 100));
        }
        notices.sort(Comparator.comparingInt(ProviderNotice::priority));
        List<String> mediaUrls = summary.posterUrl() == null ? List.of() : List.of(summary.posterUrl());
        return new ProviderProjectDetail(
                summary,
                str(row, "subtitle"),
                str(row, "introduction"),
                firstNonBlank(str(row, "detail_content"), str(row, "detail_html")),
                mediaUrls,
                List.of(),
                List.of("MOCK_DAMAI 主办方"),
                notices,
                serviceCapabilities,
                refundPolicy,
                limit
        );
    }

    public List<ProviderSession> querySessions(String projectId) {
        applyBehavior("V11_QUERY_SESSIONS");
        ensureExists("mock_ticket_source_project", "source_project_id", projectId,
                "MOCK_PROJECT_NOT_FOUND", "模拟项目不存在: " + projectId);
        return jdbc.queryForList("""
                SELECT source_session_id,source_project_id,source_session_name,sale_status,
                       source_status_code,source_status_text,start_time,end_time,sale_start_time,sale_end_time,
                       session_type,seat_mode,time_changed,change_reason,remark,limit_per_order,
                       real_name_mode,issue_method,pickup_method,data_version,update_time
                FROM mock_ticket_source_session
                WHERE source_project_id=? AND enabled=1
                ORDER BY start_time,source_session_id
                """, projectId).stream().map(resourceMapper::toSession).toList();
    }

    public List<ProviderTicketProduct> queryTicketProducts(String sessionId) {
        applyBehavior("V11_QUERY_TICKET_PRODUCTS");
        ensureExists("mock_ticket_source_session", "source_session_id", sessionId,
                "MOCK_SESSION_NOT_FOUND", "模拟场次不存在: " + sessionId);
        return jdbc.queryForList("""
                SELECT sku.source_sku_id,sku.source_session_id,s.source_project_id,sku.source_sku_name,
                       sku.product_type,sku.face_price,sku.sale_price,sku.settlement_price,
                       sku.sale_status,sku.source_status_code,sku.source_status_text,sku.sub_status,
                       sku.inventory_mode,sku.available_stock,sku.max_quantity_per_order,
                       sku.data_version,sku.update_time
                FROM mock_ticket_source_sku sku
                JOIN mock_ticket_source_session s ON s.source_session_id=sku.source_session_id
                WHERE sku.source_session_id=? AND sku.enabled=1
                ORDER BY sku.sale_price,sku.source_sku_id
                """, sessionId).stream().map(resourceMapper::toTicketProduct).toList();
    }

    public ProviderInventory queryInventory(String ticketProductId) {
        applyBehavior("V11_QUERY_INVENTORY");
        Map<String, Object> row = requiredOne("""
                SELECT source_sku_id,sale_status,source_status_code,source_status_text,
                       inventory_mode,available_stock,data_version,update_time
                FROM mock_ticket_source_sku
                WHERE source_sku_id=? AND enabled=1
                """, "MOCK_TICKET_PRODUCT_NOT_FOUND",
                "模拟票档不存在: " + ticketProductId, ticketProductId);
        TicketProductSaleStatus status = parseTicketSaleStatus(str(row, "sale_status"));
        Integer stock = nullableInt(row.get("available_stock"));
        return new ProviderInventory(
                ticketProductId,
                statusValue(status, row),
                toStockState(status, stock),
                stock,
                "REALTIME_QUERY".equals(str(row, "inventory_mode")) || stock != null,
                offset(row.get("update_time")),
                defaultVersion(str(row, "data_version"), "inventory")
        );
    }

    public ProviderVenue getVenue(String venueId) {
        applyBehavior("V11_GET_VENUE");
        Map<String, Object> r = requiredOne("""
                SELECT venue_id,venue_name,country_code,province_code,city_code,district_code,address,
                       longitude,latitude,coordinate_system,navigation_name,data_version,update_time
                FROM mock_ticket_source_venue WHERE venue_id=? AND enabled=1
                """, "MOCK_VENUE_NOT_FOUND", "模拟场馆不存在: " + venueId, venueId);
        return resourceMapper.toVenue(r);
    }

    public List<ProviderPromotionRule> queryPromotionRules(String projectId) {
        applyBehavior("V11_QUERY_PROMOTIONS");
        LocalDateTime current = LocalDateTime.now();
        return jdbc.queryForList("""
                SELECT promotion_id,promotion_type,title,description,source_project_id,source_session_id,
                       source_sku_id,stackable,rule_data,valid_from,valid_to,data_version,update_time
                FROM mock_ticket_source_promotion_rule
                WHERE enabled=1 AND (source_project_id IS NULL OR source_project_id=?)
                  AND valid_from<=? AND valid_to>=?
                ORDER BY valid_from,promotion_id
                """, projectId, current, current).stream().map(resourceMapper::toPromotionRule).toList();
    }

    public List<ProviderCampaignAsset> queryCampaignAssets(String cityCode) {
        applyBehavior("V11_QUERY_CAMPAIGNS");
        LocalDateTime current = LocalDateTime.now();
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT asset_id,asset_type,position_code,title,description,image_url,mobile_image_url,
                       target_type,target_value,city_codes,start_time,end_time,promotion_id,data_version,update_time
                FROM mock_ticket_source_campaign_asset
                WHERE enabled=1 AND (start_time IS NULL OR start_time<=?) AND (end_time IS NULL OR end_time>=?)
                ORDER BY start_time,asset_id
                """, current, current);
        return rows.stream()
                .filter(r -> cityCode == null || cityCode.isBlank() || csv(str(r, "city_codes")).contains(cityCode.trim()))
                .map(resourceMapper::toCampaignAsset)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public ProviderDeliveryQuote quoteDelivery(ProviderDeliveryQuoteRequest request) {
        applyBehavior("V11_QUOTE_DELIVERY");
        if (request == null) throw invalid("MOCK_DELIVERY_QUOTE_INVALID", "运费试算请求不能为空");
        for (ProviderDeliveryQuoteItem item : request.items()) {
            Map<String, Object> sku = requiredSku(item.ticketProductId());
            if (!request.sessionId().equals(str(sku, "source_session_id"))
                    || !request.projectId().equals(str(sku, "source_project_id"))) {
                throw invalid("MOCK_RESOURCE_MISMATCH", "运费试算的项目、场次和票档不匹配");
            }
        }
        long feeMinor = deliveryFeeMinor(request.address());
        String quoteId = "MOCK-DQ-" + System.currentTimeMillis() + "-" + random4();
        OffsetDateTime expiresAt = now().plusMinutes(15);
        jdbc.update("""
                INSERT INTO mock_ticket_source_delivery_quote
                (quote_id,source_project_id,source_session_id,address_snapshot,delivery_fee,expires_time,create_time)
                VALUES (?,?,?,?,?,?,NOW())
                """, quoteId, request.projectId(), request.sessionId(), json(request.address()),
                ProviderMoney.cny(feeMinor).toMajor(), toLocal(expiresAt));
        return new ProviderDeliveryQuote(true, ProviderMoney.cny(feeMinor), quoteId, expiresAt,
                now().plusDays(2), now().plusDays(5), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProviderOrder createOrder(ProviderOrderCreateRequest request) {
        // R5 G2：AFTER_COMMIT_TIMEOUT / RESPONSE_LOST 必须发生在 Provider 订单真正提交以后。
        // applyBehavior 对这两种特殊错误不做“提交前失败”，而是在本事务 afterCommit 阶段抛出
        // resultUnknown=true 的异常，保证 Provider 事实已存在、麦麦却没有可靠收到成功响应。
        String postCommitLossMode = postCommitLossMode("V11_CREATE_ORDER");
        applyBehavior("V11_CREATE_ORDER");
        if (request == null) throw invalid("MOCK_ORDER_INVALID", "订单请求不能为空");
        Map<String, Object> existing = one(
                "SELECT * FROM mock_ticket_source_order WHERE create_idempotency_key=?",
                request.idempotencyKey());
        if (existing != null) {
            validateCreateIdempotency(existing, request);
            return getOrderInternal(str(existing, "provider_order_id"), false);
        }

        ensureExists("mock_ticket_source_project", "source_project_id", request.projectId(),
                "MOCK_PROJECT_NOT_FOUND", "模拟项目不存在: " + request.projectId());
        Map<String, Object> session = requiredOne(
                "SELECT * FROM mock_ticket_source_session WHERE source_session_id=? AND enabled=1",
                "MOCK_SESSION_NOT_FOUND", "模拟场次不存在: " + request.sessionId(), request.sessionId());
        if (!request.projectId().equals(str(session, "source_project_id"))) {
            throw invalid("MOCK_RESOURCE_MISMATCH", "项目与场次不匹配");
        }
        if (!isProjectSaleable(str(session, "sale_status"))) {
            throw new V11AdapterException(V11ErrorCode.NOT_SALEABLE,
                    "MOCK_SESSION_NOT_SALEABLE", "场次当前不可购买", false);
        }

        Map<String, Object> sku = requiredSku(request.ticketProductId());
        if (!request.projectId().equals(str(sku, "source_project_id"))
                || !request.sessionId().equals(str(sku, "source_session_id"))) {
            throw invalid("MOCK_RESOURCE_MISMATCH", "票档不属于当前项目和场次");
        }
        TicketProductSaleStatus saleStatus = parseTicketSaleStatus(str(sku, "sale_status"));
        if (saleStatus != TicketProductSaleStatus.ON_SALE) {
            throw new V11AdapterException(V11ErrorCode.NOT_SALEABLE,
                    "MOCK_TICKET_PRODUCT_NOT_SALEABLE", "票档当前不可购买: " + request.ticketProductId(), false);
        }
        int quantity = request.quantity();
        BigDecimal salePrice = decimal(sku.get("sale_price"));
        if (request.expectedUnitPrice().toMajor().compareTo(salePrice) != 0) {
            throw new V11AdapterException(V11ErrorCode.PRICE_CHANGED,
                    "MOCK_PRICE_CHANGED", "第三方票价已变化: " + request.ticketProductId(), false);
        }
        Integer max = nullableInt(sku.get("max_quantity_per_order"));
        if (max != null && quantity > max) {
            throw invalid("MOCK_LIMIT_EXCEEDED", "票档购买数量超过限购: " + request.ticketProductId());
        }
        Integer sessionLimit = nullableInt(session.get("limit_per_order"));
        if (sessionLimit != null && quantity > sessionLimit) {
            throw invalid("MOCK_SESSION_LIMIT_EXCEEDED", "购买数量超过场次限购");
        }
        Integer stock = nullableInt(sku.get("available_stock"));
        // available_stock=NULL 表示第三方不公开库存快照，不等于 0。最终创建订单时由模拟供应商
        // 直接确认是否接受本次锁单；MOCK_DAMAI 对未知库存默认接受，以覆盖 V1.3.3 的未知库存购买链路。
        if (stock != null && stock < quantity) {
            throw new V11AdapterException(V11ErrorCode.INSUFFICIENT_STOCK,
                    "MOCK_INVENTORY_NOT_ENOUGH", "第三方库存不足: " + request.ticketProductId(), false);
        }

        BigDecimal facePrice = defaultDecimal(decimal(sku.get("face_price")), salePrice);
        BigDecimal settlementPrice = defaultDecimal(decimal(sku.get("settlement_price")), salePrice);
        BigDecimal faceAmount = facePrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal saleAmount = salePrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal settlementAmount = settlementPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discount = calculatePromotionDiscount(request.projectId(), saleAmount);
        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (request.deliveryMode() == DeliveryMode.EXPRESS) {
            Map<String, Object> quote = requiredOne("""
                    SELECT * FROM mock_ticket_source_delivery_quote
                    WHERE quote_id=? AND source_project_id=? AND source_session_id=?
                    """, "MOCK_DELIVERY_QUOTE_NOT_FOUND", "运费报价不存在或资源不匹配",
                    request.deliveryQuoteId(), request.projectId(), request.sessionId());
            if (localDateTime(quote.get("expires_time")).isBefore(LocalDateTime.now())) {
                throw new V11AdapterException(V11ErrorCode.STATUS_CONFLICT,
                        "MOCK_DELIVERY_QUOTE_EXPIRED", "运费报价已过期", false);
            }
            if (str(quote, "used_provider_order_id") != null) {
                throw new V11AdapterException(V11ErrorCode.STATUS_CONFLICT,
                        "MOCK_DELIVERY_QUOTE_USED", "运费报价已被其他订单使用", false);
            }
            deliveryFee = decimal(quote.get("delivery_fee"));
            if (request.expectedDeliveryFee() == null
                    || request.expectedDeliveryFee().toMajor().compareTo(deliveryFee) != 0) {
                throw new V11AdapterException(V11ErrorCode.PRICE_CHANGED,
                        "MOCK_DELIVERY_FEE_CHANGED", "第三方运费已变化", false);
            }
        } else if (request.expectedDeliveryFee() != null && request.expectedDeliveryFee().amountMinor() != 0) {
            throw invalid("MOCK_DELIVERY_FEE_INVALID", "非快递订单运费必须为0");
        }
        BigDecimal serviceFee = BigDecimal.ZERO;
        BigDecimal payAmount = saleAmount.subtract(discount).add(deliveryFee).add(serviceFee);
        if (request.expectedTicketAmount() == null
                || request.expectedTicketAmount().toMajor().compareTo(saleAmount) != 0) {
            throw new V11AdapterException(V11ErrorCode.PRICE_CHANGED,
                    "MOCK_TICKET_AMOUNT_CHANGED", "第三方票款合计已变化", false);
        }
        if (request.expectedPayAmount() == null
                || request.expectedPayAmount().toMajor().compareTo(payAmount) != 0) {
            throw new V11AdapterException(V11ErrorCode.PRICE_CHANGED,
                    "MOCK_PAY_AMOUNT_CHANGED", "第三方应付金额已变化", false);
        }

        String version = version("order-create");
        int updated;
        if (stock == null) {
            updated = jdbc.update("""
                    UPDATE mock_ticket_source_sku
                    SET version=version+1,data_version=?,update_time=NOW()
                    WHERE source_sku_id=? AND enabled=1 AND sale_status='ON_SALE'
                      AND available_stock IS NULL
                    """, version, request.ticketProductId());
        } else {
            updated = jdbc.update("""
                    UPDATE mock_ticket_source_sku
                    SET available_stock=available_stock-?,version=version+1,data_version=?,update_time=NOW()
                    WHERE source_sku_id=? AND enabled=1 AND sale_status='ON_SALE'
                      AND available_stock IS NOT NULL AND available_stock>=?
                    """, quantity, version, request.ticketProductId(), quantity);
        }
        if (updated != 1) {
            throw new V11AdapterException(V11ErrorCode.INSUFFICIENT_STOCK,
                    "MOCK_INVENTORY_NOT_ENOUGH", "第三方库存并发不足: " + request.ticketProductId(), false);
        }

        String providerOrderId = "MOCK-V11-ORDER-" + System.currentTimeMillis() + "-" + random4();
        String providerOrderNo = "MV11" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + random4();
        LocalDateTime expireAt = request.reservationExpireAt() == null
                ? LocalDateTime.now().plusMinutes(15)
                : toLocal(request.reservationExpireAt());
        if (expireAt.isBefore(LocalDateTime.now().plusMinutes(1))) expireAt = LocalDateTime.now().plusMinutes(15);
        jdbc.update("""
                INSERT INTO mock_ticket_source_order
                (provider_order_id,provider_order_no,client_order_no,source_project_id,source_session_id,
                 order_model,source_sku_id,quantity,unit_price,total_amount,face_amount,settlement_amount,
                 discount_amount,delivery_fee,service_fee,pay_amount,ticket_mode,delivery_mode,
                 buyer_snapshot,contact_snapshot,address_snapshot,delivery_quote_id,issue_trigger_mode,
                 currency_code,order_status,create_idempotency_key,reservation_expire_time,data_version,
                 create_time,update_time)
                VALUES (?,?,?,?,?,'SINGLE_SKU',?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'CNY','WAIT_PAY',?,?,?,NOW(),NOW())
                """,
                providerOrderId, providerOrderNo, request.clientOrderNo(), request.projectId(), request.sessionId(),
                request.ticketProductId(), quantity, salePrice, payAmount, faceAmount, settlementAmount,
                discount, deliveryFee, serviceFee, payAmount, request.ticketMode().name(), request.deliveryMode().name(),
                json(request.buyer()), json(request.contact()), json(request.address()), request.deliveryQuoteId(),
                IssueTriggerMode.EXPLICIT_TRIGGER_REQUIRED.name(), request.idempotencyKey(), expireAt, version
        );
        jdbc.update("""
                INSERT INTO mock_ticket_source_order_item
                (provider_order_id,client_line_no,source_sku_id,quantity,unit_price,settlement_unit_price)
                VALUES (?,'LINE-1',?,?,?,?)
                """, providerOrderId, request.ticketProductId(), quantity, salePrice, settlementPrice);
        for (ProviderTicketAssignmentRequest ticket : request.tickets()) {
            ProviderPerson holder = request.holders().get(ticket.holderRef());
            jdbc.update("""
                    INSERT INTO mock_ticket_source_order_ticket
                    (provider_order_id,client_ticket_no,holder_ref,source_sku_id,holder_snapshot,provider_sub_order_id)
                    VALUES (?,?,?,?,?,?)
                    """, providerOrderId, ticket.clientTicketNo(), ticket.holderRef(), request.ticketProductId(),
                    json(holder), "MOCK-SUB-" + providerOrderId + "-" + ticket.clientTicketNo());
        }
        CredentialType credentialType = request.ticketMode();
        String dynamicMode = credentialType == CredentialType.DYNAMIC_QR
                ? DynamicQrMode.REMOTE_REFRESH.name() : null;
        jdbc.update("""
                INSERT INTO mock_ticket_source_delivery
                (provider_order_id,delivery_status,issue_mode,issue_trigger_mode,seat_mode,credential_type,
                 dynamic_qr_mode,expected_ticket_count,issued_count,failed_count,data_version,create_time,update_time)
                VALUES (?,'PENDING','IMMEDIATE',? ,?,?,?,?,0,0,?,NOW(),NOW())
                ON DUPLICATE KEY UPDATE delivery_status='PENDING',issue_mode='IMMEDIATE',
                  issue_trigger_mode=VALUES(issue_trigger_mode),seat_mode=VALUES(seat_mode),
                  credential_type=VALUES(credential_type),dynamic_qr_mode=VALUES(dynamic_qr_mode),
                  expected_ticket_count=VALUES(expected_ticket_count),issued_count=0,failed_count=0,
                  request_idempotency_key=NULL,data_version=VALUES(data_version),update_time=NOW()
                """, providerOrderId, IssueTriggerMode.EXPLICIT_TRIGGER_REQUIRED.name(),
                str(session, "seat_mode"), credentialType.name(), dynamicMode, quantity, version);
        if (request.deliveryMode() == DeliveryMode.EXPRESS) {
            jdbc.update("UPDATE mock_ticket_source_delivery_quote SET used_provider_order_id=? WHERE quote_id=?",
                    providerOrderId, request.deliveryQuoteId());
            upsertShipment(providerOrderId, ShipmentStatus.WAIT_SHIPMENT, null, null, null, null, null, null);
        }
        emitCallback(CallbackEventType.ORDER_CHANGED, "ORDER", providerOrderId, version);
        registerPostCommitLoss(postCommitLossMode);
        return getOrderInternal(providerOrderId, false);
    }

    /** R5：按商户订单号/创建幂等键补查订单，专用于 createOrder 结果不确定恢复。 */
    public ProviderOrder findOrder(ProviderOrderLookupRequest request) {
        applyBehavior("V11_FIND_ORDER");
        if (request == null) throw invalid("MOCK_ORDER_LOOKUP_INVALID", "订单补查请求不能为空");
        Map<String, Object> row = null;
        if (request.idempotencyKey() != null) {
            row = one("SELECT * FROM mock_ticket_source_order WHERE create_idempotency_key=?", request.idempotencyKey());
        }
        if (row == null && request.clientOrderNo() != null) {
            row = one("SELECT * FROM mock_ticket_source_order WHERE client_order_no=?", request.clientOrderNo());
        }
        if (row == null) {
            throw new V11AdapterException(V11ErrorCode.RESOURCE_NOT_FOUND,
                    "MOCK_ORDER_LOOKUP_NOT_FOUND", "按商户订单号/幂等键未查到第三方订单", false);
        }
        return getOrderInternal(str(row, "provider_order_id"), false);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProviderOrder confirmPayment(String providerOrderId, ProviderConfirmPaymentRequest request) {
        applyBehavior("V11_CONFIRM_PAYMENT");
        if (request == null) throw invalid("MOCK_PAYMENT_INVALID", "支付确认请求不能为空");
        Map<String, Object> order = requiredOrder(providerOrderId);
        String status = str(order, "order_status");
        String storedKey = str(order, "payment_idempotency_key");
        if ("PAID".equals(status) || "ISSUED".equals(status) || "PARTIAL_ISSUED".equals(status)) {
            if (storedKey != null && !storedKey.equals(request.idempotencyKey())) {
                throw conflict("MOCK_PAYMENT_IDEMPOTENCY_CONFLICT", "订单已经使用其他幂等键确认支付");
            }
            return getOrderInternal(providerOrderId, false);
        }
        if (!"WAIT_PAY".equals(status)) {
            throw conflict("MOCK_ORDER_STATUS_CONFLICT", "当前订单状态不允许确认支付: " + status);
        }
        if (!request.clientOrderNo().equals(str(order, "client_order_no"))) {
            throw invalid("MOCK_CLIENT_ORDER_MISMATCH", "本地订单号不匹配");
        }
        BigDecimal payAmount = decimal(order.get("pay_amount"));
        if (request.payAmount().toMajor().compareTo(payAmount) != 0) {
            throw new V11AdapterException(V11ErrorCode.PRICE_CHANGED,
                    "MOCK_PAY_AMOUNT_MISMATCH", "支付金额与第三方订单不一致", false);
        }
        if (localDateTime(order.get("reservation_expire_time")).isBefore(LocalDateTime.now())) {
            expireOrder(providerOrderId, order);
            throw conflict("MOCK_ORDER_EXPIRED", "第三方订单已过期");
        }
        String version = version("order-paid");
        jdbc.update("""
                UPDATE mock_ticket_source_order
                SET order_status='PAID',payment_idempotency_key=?,pay_time=?,data_version=?,update_time=NOW()
                WHERE provider_order_id=? AND order_status='WAIT_PAY'
                """, request.idempotencyKey(),
                request.paidAt() == null ? LocalDateTime.now() : toLocal(request.paidAt()), version, providerOrderId);
        emitCallback(CallbackEventType.ORDER_CHANGED, "ORDER", providerOrderId, version);
        return getOrderInternal(providerOrderId, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProviderOrder cancelOrder(String providerOrderId, ProviderCancelOrderRequest request) {
        applyBehavior("V11_CANCEL_ORDER");
        if (request == null) throw invalid("MOCK_CANCEL_INVALID", "取消请求不能为空");
        Map<String, Object> order = requiredOrder(providerOrderId);
        String status = str(order, "order_status");
        String oldKey = str(order, "cancel_idempotency_key");
        if ("CANCELED".equals(status) || "EXPIRED".equals(status)) {
            if (oldKey != null && !oldKey.equals(request.idempotencyKey())) {
                throw conflict("MOCK_CANCEL_IDEMPOTENCY_CONFLICT", "订单已使用其他幂等键取消");
            }
            return getOrderInternal(providerOrderId, false);
        }
        if (!"WAIT_PAY".equals(status)) {
            throw conflict("MOCK_ORDER_STATUS_CONFLICT", "已支付订单不能通过取消接口释放库存");
        }
        restoreOrderInventory(providerOrderId, version("cancel-restore"));
        String version = version("order-cancel");
        jdbc.update("""
                UPDATE mock_ticket_source_order
                SET order_status='CANCELED',cancel_idempotency_key=?,cancel_time=NOW(),cancel_reason=?,
                    data_version=?,update_time=NOW()
                WHERE provider_order_id=? AND order_status='WAIT_PAY'
                """, request.idempotencyKey(), request.reason(), version, providerOrderId);
        emitCallback(CallbackEventType.ORDER_CHANGED, "ORDER", providerOrderId, version);
        return getOrderInternal(providerOrderId, false);
    }

    public ProviderOrder getOrder(String providerOrderId) {
        applyBehavior("V11_GET_ORDER");
        return getOrderInternal(providerOrderId, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProviderTicketDelivery triggerFulfillment(
            String providerOrderId, ProviderFulfillmentTriggerRequest request
    ) {
        applyBehavior("V11_TRIGGER_FULFILLMENT");
        if (request == null) throw invalid("MOCK_FULFILLMENT_INVALID", "履约触发请求不能为空");
        Map<String, Object> order = requiredOrder(providerOrderId);
        if (!request.clientOrderNo().equals(str(order, "client_order_no"))) {
            throw invalid("MOCK_CLIENT_ORDER_MISMATCH", "本地订单号不匹配");
        }
        String status = str(order, "order_status");
        if (!List.of("PAID", "ISSUING", "ISSUED", "PARTIAL_ISSUED").contains(status)) {
            throw conflict("MOCK_ORDER_NOT_PAID", "订单尚未支付，不能触发第三方履约");
        }
        int ticketCount = countOrderTickets(providerOrderId);
        if (request.expectedTicketCount() != ticketCount) {
            throw invalid("MOCK_TICKET_COUNT_MISMATCH", "预计票数与第三方订单逐票明细不一致");
        }
        Map<String, Object> delivery = requiredDelivery(providerOrderId);
        String oldKey = str(delivery, "request_idempotency_key");
        if (oldKey != null && !oldKey.equals(request.idempotencyKey())) {
            throw conflict("MOCK_FULFILLMENT_IDEMPOTENCY_CONFLICT", "履约任务已绑定其他幂等键");
        }
        if (oldKey == null) {
            jdbc.update("""
                    UPDATE mock_ticket_source_delivery
                    SET request_idempotency_key=?,delivery_status='PROCESSING',data_version=?,update_time=NOW()
                    WHERE provider_order_id=?
                    """, request.idempotencyKey(), version("fulfillment-bind"), providerOrderId);
            jdbc.update("UPDATE mock_ticket_source_order SET order_status='ISSUING',update_time=NOW() WHERE provider_order_id=? AND order_status='PAID'",
                    providerOrderId);
        }
        return materializeDelivery(providerOrderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProviderTicketDelivery getTickets(String providerOrderId) {
        applyBehavior("V11_GET_TICKETS");
        requiredOrder(providerOrderId);
        Map<String, Object> delivery = requiredDelivery(providerOrderId);
        if ("PENDING".equals(str(delivery, "delivery_status"))
                || "PROCESSING".equals(str(delivery, "delivery_status"))) {
            String key = str(delivery, "request_idempotency_key");
            if (key != null) return materializeDelivery(providerOrderId);
        }
        return toTicketDelivery(requiredDelivery(providerOrderId), credentialRows(providerOrderId));
    }

    @Transactional(rollbackFor = Exception.class)
    public ProviderDynamicCredential refreshDynamicCredential(String providerTicketId, String currentVersion) {
        applyBehavior("V11_REFRESH_DYNAMIC_QR");
        Map<String, Object> credential = requiredOne("""
                SELECT * FROM mock_ticket_source_credential WHERE provider_ticket_id=?
                """, "MOCK_TICKET_NOT_FOUND", "第三方票不存在: " + providerTicketId, providerTicketId);
        if (!"DYNAMIC_QR".equals(str(credential, "credential_type"))) {
            throw new V11AdapterException(V11ErrorCode.UNSUPPORTED_OPERATION,
                    "MOCK_NOT_DYNAMIC_QR", "该票不是动态二维码", false);
        }
        if (!"ISSUED".equals(str(credential, "ticket_status"))) {
            throw conflict("MOCK_TICKET_STATUS_CONFLICT", "当前票状态不能刷新动态二维码");
        }
        int refreshAfter = DYNAMIC_QR_REFRESH_AFTER_SECONDS;
        OffsetDateTime issuedAt = now();
        OffsetDateTime expiresAt = issuedAt.plusSeconds(DYNAMIC_QR_TTL_SECONDS);
        String version = "mock-dqr-" + System.currentTimeMillis() + "-" + random4();
        String payload = "MOCK:DYNAMIC_QR:" + providerTicketId + ":" + version;
        jdbc.update("""
                UPDATE mock_ticket_source_credential
                SET credential_version=?,expire_time=?,data_version=?,update_time=NOW()
                WHERE provider_ticket_id=?
                """, version, toLocal(expiresAt), version, providerTicketId);
        return new ProviderDynamicCredential(providerTicketId, CredentialType.DYNAMIC_QR, payload, version,
                issuedAt, expiresAt, refreshAfter, now());
    }

    public ProviderShipment getShipment(String providerOrderId) {
        applyBehavior("V11_GET_SHIPMENT");
        Map<String, Object> row = requiredOne(
                "SELECT * FROM mock_ticket_source_shipment WHERE provider_order_id=?",
                "MOCK_SHIPMENT_NOT_FOUND", "该订单没有第三方物流单", providerOrderId);
        return toShipment(row);
    }

    public ProviderRefundQuote quoteRefund(String providerOrderId) {
        applyBehavior("V11_QUOTE_REFUND");
        Map<String, Object> order = requiredOrder(providerOrderId);
        String status = str(order, "order_status");
        boolean refundable = List.of("PAID", "ISSUING", "ISSUED", "PARTIAL_ISSUED").contains(status);
        BigDecimal pay = decimal(order.get("pay_amount"));
        BigDecimal deliveryFee = decimal(order.get("delivery_fee"));
        BigDecimal refundableDelivery = BigDecimal.ZERO;
        if (deliveryFee.signum() > 0) {
            Map<String, Object> shipment = one(
                    "SELECT shipment_status FROM mock_ticket_source_shipment WHERE provider_order_id=?",
                    providerOrderId);
            String shipmentStatus = shipment == null ? null : str(shipment, "shipment_status");
            if (shipmentStatus == null || "WAIT_SHIPMENT".equals(shipmentStatus)) {
                refundableDelivery = deliveryFee;
            }
        }
        BigDecimal ticketRefund = pay.subtract(deliveryFee);
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal totalRefund = ticketRefund.add(refundableDelivery);
        BigDecimal nonRefundableDelivery = deliveryFee.subtract(refundableDelivery).max(BigDecimal.ZERO);
        String orderVersion = defaultVersion(str(order, "data_version"), "order");
        return new ProviderRefundQuote(
                providerOrderId,
                RefundScope.FULL_ORDER,
                refundable,
                ProviderMoney.fromMajor(pay, "CNY"),
                ProviderMoney.fromMajor(refundable ? totalRefund : BigDecimal.ZERO, "CNY"),
                ProviderMoney.fromMajor(fee, "CNY"),
                ProviderMoney.fromMajor(refundableDelivery, "CNY"),
                ProviderMoney.fromMajor(nonRefundableDelivery, "CNY"),
                ProviderMoney.fromMajor(BigDecimal.ZERO, "CNY"),
                List.of(),
                refundable ? null : "当前订单状态不可退款: " + status,
                now().plusMinutes(15),
                "MOCK-RQ-" + providerOrderId + "-" + orderVersion,
                version("refund-quote")
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public ProviderRefund requestRefund(String providerOrderId, ProviderRefundRequest request) {
        applyBehavior("V11_REQUEST_REFUND");
        if (request == null) throw invalid("MOCK_REFUND_INVALID", "退款请求不能为空");
        if (request.refundScope() != RefundScope.FULL_ORDER) {
            throw invalid("MOCK_REFUND_SCOPE_INVALID", "V1.2只允许FULL_ORDER整单退款");
        }
        Map<String, Object> existing = one(
                "SELECT * FROM mock_ticket_source_refund WHERE request_idempotency_key=?",
                request.idempotencyKey());
        if (existing != null) return toRefund(existing);
        Map<String, Object> order = requiredOrder(providerOrderId);
        ProviderRefundQuote quote = quoteRefund(providerOrderId);
        if (!quote.refundable()) {
            throw conflict("MOCK_ORDER_NOT_REFUNDABLE", quote.unavailableReason());
        }
        if (!Objects.equals(request.quoteId(), quote.quoteId())) {
            throw new V11AdapterException(V11ErrorCode.PRICE_CHANGED,
                    "MOCK_REFUND_QUOTE_CHANGED", "第三方退款试算已变化，请重新试算", false);
        }
        if (request.quotedRefundAmount() == null
                || request.quotedRefundAmount().amountMinor() != quote.refundableAmount().amountMinor()) {
            throw new V11AdapterException(V11ErrorCode.PRICE_CHANGED,
                    "MOCK_REFUND_AMOUNT_CHANGED", "第三方可退金额已变化", false);
        }
        Map<String, Object> oldRefund = one(
                "SELECT * FROM mock_ticket_source_refund WHERE provider_order_id=? ORDER BY create_time DESC LIMIT 1",
                providerOrderId);
        if (oldRefund != null) return toRefund(oldRefund);
        Map<String, Object> plan = one(
                "SELECT * FROM mock_ticket_source_refund_plan WHERE provider_order_id=?", providerOrderId);
        String mode = plan == null ? "IMMEDIATE" : upper(str(plan, "refund_mode"), "IMMEDIATE");
        LocalDateTime available = plan == null ? null : localDateTimeNullable(plan.get("available_time"));
        String providerRefundId = "MOCK-V11-REFUND-" + System.currentTimeMillis() + "-" + random4();
        String providerRefundNo = "MVR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + random4();
        String refundStatus = "REJECT".equals(mode) ? "FAILED" : "IMMEDIATE".equals(mode) ? "PROCESSING" : "PROCESSING";
        jdbc.update("""
                INSERT INTO mock_ticket_source_refund
                (provider_refund_id,provider_refund_no,provider_order_id,client_refund_no,refund_status,
                 refund_mode,refund_amount,fee_amount,refunded_delivery_fee,currency_code,reason,
                 request_idempotency_key,available_time,inventory_restored,data_version,create_time,update_time)
                VALUES (?,?,?,?,?,?,?,?,?,'CNY',?,?,?,?,?,NOW(),NOW())
                """, providerRefundId, providerRefundNo, providerOrderId, request.clientRefundNo(), refundStatus,
                mode, quote.refundableAmount().toMajor(), quote.feeAmount().toMajor(),
                quote.refundableDeliveryFee().toMajor(), request.reason(), request.idempotencyKey(),
                available, 0, version("refund-create"));
        jdbc.update("UPDATE mock_ticket_source_order SET order_status='REFUNDING',data_version=?,update_time=NOW() WHERE provider_order_id=?",
                version("order-refunding"), providerOrderId);
        if ("REJECT".equals(mode)) {
            jdbc.update("""
                    UPDATE mock_ticket_source_refund
                    SET refund_status='FAILED',error_code='MOCK_REFUND_REJECTED',
                        error_message='模拟第三方拒绝退款',data_version=?,update_time=NOW()
                    WHERE provider_refund_id=?
                    """, version("refund-reject"), providerRefundId);
            jdbc.update("UPDATE mock_ticket_source_order SET order_status='ISSUED',update_time=NOW() WHERE provider_order_id=?",
                    providerOrderId);
        } else if ("IMMEDIATE".equals(mode)) {
            completeRefund(providerRefundId);
        }
        emitCallback(CallbackEventType.REFUND_CHANGED, "REFUND", providerRefundId, version("refund-callback"));
        return getRefundInternal(providerRefundId, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProviderRefund getRefund(String providerRefundId) {
        applyBehavior("V11_GET_REFUND");
        return getRefundInternal(providerRefundId, true);
    }

    public List<Map<String, Object>> listBehaviors() {
        return behaviorService.list();
    }

    @Transactional
    public Map<String, Object> updateBehavior(String operationCode, MockV11BehaviorRequest request) {
        return behaviorService.update(operationCode, request);
    }

    @Transactional
    public List<Map<String, Object>> resetBehaviors() {
        return behaviorService.reset();
    }

    @Transactional
    public ProviderTicketDelivery configureIssuePlan(String providerOrderId, MockV11IssuePlanRequest request) {
        requiredOrder(providerOrderId);
        if (request == null) throw new BusinessException("出票计划不能为空");
        String mode = upper(request.issueMode(), "IMMEDIATE");
        if (!List.of("IMMEDIATE", "DELAYED", "PARTIAL_FAIL", "ALL_FAIL").contains(mode)) {
            throw new BusinessException("不支持的出票模式: " + mode);
        }
        CredentialType credentialType = request.credentialType() == null
                ? CredentialType.STATIC_QR : request.credentialType();
        DynamicQrMode dynamicMode = credentialType == CredentialType.DYNAMIC_QR
                ? Optional.ofNullable(request.dynamicQrMode()).orElse(DynamicQrMode.REMOTE_REFRESH)
                : null;
        jdbc.update("""
                UPDATE mock_ticket_source_delivery
                SET delivery_status='PENDING',issue_mode=?,credential_type=?,dynamic_qr_mode=?,
                    fail_ticket_index=?,available_time=?,request_idempotency_key=NULL,
                    issued_count=0,failed_count=0,last_error_code=NULL,last_error_message=NULL,
                    data_version=?,update_time=NOW()
                WHERE provider_order_id=?
                """, mode, credentialType.name(), dynamicMode == null ? null : dynamicMode.name(),
                request.failTicketIndex(), request.availableAt() == null ? null : toLocal(request.availableAt()),
                version("issue-plan"), providerOrderId);
        jdbc.update("DELETE FROM mock_ticket_source_credential WHERE provider_order_id=?", providerOrderId);
        jdbc.update("UPDATE mock_ticket_source_order_ticket SET provider_ticket_id=NULL WHERE provider_order_id=?",
                providerOrderId);
        jdbc.update("UPDATE mock_ticket_source_order SET order_status='PAID',update_time=NOW() " +
                "WHERE provider_order_id=? AND order_status IN ('ISSUING','ISSUED','PARTIAL_ISSUED')", providerOrderId);
        return toTicketDelivery(requiredDelivery(providerOrderId), List.of());
    }

    @Transactional
    public ProviderTicketDelivery makeIssueAvailableNow(String providerOrderId) {
        requiredDelivery(providerOrderId);
        jdbc.update("UPDATE mock_ticket_source_delivery SET available_time=NOW(),update_time=NOW() WHERE provider_order_id=?",
                providerOrderId);
        return materializeDelivery(providerOrderId);
    }

    @Transactional
    public Map<String, Object> configureRefundPlan(String providerOrderId, MockV11RefundPlanRequest request) {
        requiredOrder(providerOrderId);
        if (request == null) throw new BusinessException("退款计划不能为空");
        String mode = upper(request.refundMode(), "IMMEDIATE");
        if (!List.of("IMMEDIATE", "DELAYED", "REJECT").contains(mode)) {
            throw new BusinessException("不支持的退款模式: " + mode);
        }
        jdbc.update("""
                INSERT INTO mock_ticket_source_refund_plan
                (provider_order_id,refund_mode,available_time,data_version,create_time,update_time)
                VALUES (?,?,?,?,NOW(),NOW())
                ON DUPLICATE KEY UPDATE refund_mode=VALUES(refund_mode),available_time=VALUES(available_time),
                  data_version=VALUES(data_version),update_time=NOW()
                """, providerOrderId, mode,
                request.availableAt() == null ? null : toLocal(request.availableAt()), version("refund-plan"));
        return requiredOne("SELECT * FROM mock_ticket_source_refund_plan WHERE provider_order_id=?",
                "MOCK_REFUND_PLAN_NOT_FOUND", "退款计划不存在", providerOrderId);
    }

    @Transactional
    public ProviderRefund makeRefundAvailableNow(String providerRefundId) {
        requiredRefund(providerRefundId);
        jdbc.update("UPDATE mock_ticket_source_refund SET available_time=NOW(),update_time=NOW() WHERE provider_refund_id=?",
                providerRefundId);
        completeRefund(providerRefundId);
        return getRefundInternal(providerRefundId, false);
    }

    @Transactional
    public ProviderShipment updateShipment(String providerOrderId, MockV11ShipmentUpdateRequest request) {
        if (request == null || request.shipmentStatus() == null) throw new BusinessException("物流状态不能为空");
        Map<String, Object> order = requiredOrder(providerOrderId);
        if (!DeliveryMode.EXPRESS.name().equals(str(order, "delivery_mode"))) {
            throw new BusinessException("只有快递纸质票订单可以更新物流");
        }
        Map<String, Object> currentRow = one(
                "SELECT * FROM mock_ticket_source_shipment WHERE provider_order_id=?", providerOrderId);
        ShipmentStatus current = currentRow == null ? ShipmentStatus.WAIT_SHIPMENT
                : enumValue(ShipmentStatus.class, str(currentRow, "shipment_status"), ShipmentStatus.WAIT_SHIPMENT);
        if (!V11ShipmentTransitionPolicy.canTransition(current, request.shipmentStatus())) {
            throw new BusinessException("物流状态不允许倒退: " + current + " -> " + request.shipmentStatus());
        }
        String waybillNo = firstNonBlank(request.waybillNo(), currentRow == null ? null : str(currentRow, "waybill_no"));
        if (V11ShipmentTransitionPolicy.requiresWaybill(request.shipmentStatus()) && waybillNo == null) {
            throw new BusinessException("已发货及后续物流状态必须提供运单号");
        }
        OffsetDateTime shippedAt = request.shippedAt() != null ? request.shippedAt()
                : (currentRow == null ? null : offsetNullable(currentRow.get("shipped_time")));
        OffsetDateTime signedAt = request.signedAt() != null ? request.signedAt()
                : (currentRow == null ? null : offsetNullable(currentRow.get("signed_time")));
        if (V11ShipmentTransitionPolicy.requiresWaybill(request.shipmentStatus()) && shippedAt == null) shippedAt = now();
        if (request.shipmentStatus() == ShipmentStatus.DELIVERED && signedAt == null) signedAt = now();
        String carrierCode = firstNonBlank(request.carrierCode(), currentRow == null ? null : str(currentRow, "carrier_code"));
        String carrierName = firstNonBlank(request.carrierName(), currentRow == null ? null : str(currentRow, "carrier_name"));
        String trackingUrl = firstNonBlank(request.trackingUrl(), currentRow == null ? null : str(currentRow, "tracking_url"));
        upsertShipment(providerOrderId, request.shipmentStatus(), carrierCode, carrierName,
                waybillNo, shippedAt, signedAt, trackingUrl);
        emitCallback(CallbackEventType.SHIPMENT_CHANGED, "SHIPMENT", providerOrderId, version("shipment-callback"));
        return getShipment(providerOrderId);
    }

    @Transactional
    public ProviderCallbackEvent emitCallback(MockV11CallbackRequest request) {
        if (request == null || request.eventType() == null) throw new BusinessException("回调事件类型不能为空");
        if (blankToNull(request.resourceType()) == null || blankToNull(request.providerResourceId()) == null) {
            throw new BusinessException("resourceType/providerResourceId不能为空");
        }
        return emitCallback(request.eventType(), request.resourceType().trim(),
                request.providerResourceId().trim(), firstNonBlank(request.version(), version("manual-event")));
    }

    public List<ProviderCallbackEvent> listCallbackEvents(int limit) {
        int safeLimit = Math.min(200, Math.max(1, limit));
        return jdbc.queryForList("""
                SELECT event_id,event_type,resource_type,provider_resource_id,resource_version,occurred_time
                FROM mock_ticket_source_callback_event
                ORDER BY occurred_time DESC,event_id DESC LIMIT ?
                """, safeLimit).stream().map(this::toCallbackEvent).toList();
    }

    private ProviderOrder getOrderInternal(String providerOrderId, boolean applyExpiry) {
        Map<String, Object> order = requiredOrder(providerOrderId);
        if (applyExpiry && "WAIT_PAY".equals(str(order, "order_status"))
                && localDateTime(order.get("reservation_expire_time")).isBefore(LocalDateTime.now())) {
            expireOrder(providerOrderId, order);
            order = requiredOrder(providerOrderId);
        }
        List<ProviderTicketUnit> tickets = jdbc.queryForList("""
                SELECT client_ticket_no,holder_ref,source_sku_id,provider_sub_order_id,provider_ticket_id
                FROM mock_ticket_source_order_ticket WHERE provider_order_id=? ORDER BY ticket_unit_id
                """, providerOrderId).stream().map(r -> new ProviderTicketUnit(
                str(r, "client_ticket_no"), str(r, "holder_ref"), str(r, "source_sku_id"),
                str(r, "provider_sub_order_id"), str(r, "provider_ticket_id")
        )).toList();
        return new ProviderOrder(
                providerOrderId,
                str(order, "provider_order_no"),
                str(order, "client_order_no"),
                str(order, "source_project_id"),
                str(order, "source_session_id"),
                new ProviderStatusValue<>(parseOrderStatus(str(order, "order_status")),
                        str(order, "order_status"), mockOrderStatusText(str(order, "order_status"))),
                new ProviderOrderPriceBreakdown(
                        money(order.get("face_amount")),
                        money(sumOrderItems(providerOrderId, "unit_price")),
                        money(order.get("settlement_amount")),
                        money(order.get("discount_amount")),
                        money(order.get("delivery_fee")),
                        money(order.get("service_fee")),
                        money(order.get("total_amount")),
                        money(order.get("pay_amount"))
                ),
                tickets,
                offsetNullable(order.get("reservation_expire_time")),
                offsetNullable(order.get("create_time")),
                offsetNullable(order.get("pay_time")),
                offsetNullable(order.get("cancel_time")),
                defaultVersion(str(order, "data_version"), "order")
        );
    }

    private ProviderTicketDelivery materializeDelivery(String providerOrderId) {
        Map<String, Object> delivery = requiredDelivery(providerOrderId);
        String currentStatus = str(delivery, "delivery_status");
        if (List.of("ISSUED", "PARTIAL", "FAILED", "REFUNDED").contains(currentStatus)) {
            return toTicketDelivery(delivery, credentialRows(providerOrderId));
        }
        String issueMode = upper(str(delivery, "issue_mode"), "IMMEDIATE");
        LocalDateTime available = localDateTimeNullable(delivery.get("available_time"));
        if ("DELAYED".equals(issueMode) && available != null && LocalDateTime.now().isBefore(available)) {
            jdbc.update("UPDATE mock_ticket_source_delivery SET delivery_status='PENDING',update_time=NOW() WHERE provider_order_id=?",
                    providerOrderId);
            return toTicketDelivery(requiredDelivery(providerOrderId), credentialRows(providerOrderId));
        }
        List<Map<String, Object>> ticketUnits = jdbc.queryForList("""
                SELECT ticket_unit_id,client_ticket_no,holder_ref,source_sku_id,provider_ticket_id
                FROM mock_ticket_source_order_ticket WHERE provider_order_id=? ORDER BY ticket_unit_id
                """, providerOrderId);
        Map<String, Object> session = requiredOne("""
                SELECT s.* FROM mock_ticket_source_session s
                JOIN mock_ticket_source_order o ON o.source_session_id=s.source_session_id
                WHERE o.provider_order_id=?
                """, "MOCK_SESSION_NOT_FOUND", "订单场次不存在", providerOrderId);
        int failIndex = Optional.ofNullable(nullableInt(delivery.get("fail_ticket_index"))).orElse(-1);
        CredentialType type = enumValue(CredentialType.class,
                str(delivery, "credential_type"), CredentialType.STATIC_QR);
        DynamicQrMode dynamicMode = type == CredentialType.DYNAMIC_QR
                ? enumValue(DynamicQrMode.class, str(delivery, "dynamic_qr_mode"), DynamicQrMode.REMOTE_REFRESH)
                : null;
        int issued = 0;
        int failed = 0;
        int index = 0;
        for (Map<String, Object> ticketUnit : ticketUnits) {
            index++;
            boolean fail = "ALL_FAIL".equals(issueMode)
                    || ("PARTIAL_FAIL".equals(issueMode) && index == failIndex);
            String providerTicketId = "MOCK-V11-TICKET-" + providerOrderId + "-" + index;
            String credentialVersion = version("credential-" + index);
            String payload = fail || type == CredentialType.DYNAMIC_QR || type == CredentialType.PAPER_TICKET
                    ? null : buildCredentialPayload(type, providerTicketId, credentialVersion);
            boolean assignedSeat = "ASSIGNED_SEAT".equals(str(session, "seat_mode"));
            String zone = assignedSeat ? "A区" : null;
            String row = assignedSeat ? String.valueOf(8 + index) : null;
            String seat = assignedSeat ? String.valueOf(15 + index) : null;
            String entrance = assignedSeat ? "东门检票通道" : null;
            jdbc.update("""
                    INSERT INTO mock_ticket_source_credential
                    (provider_ticket_id,provider_order_id,ticket_index,client_ticket_no,holder_ref,source_sku_id,
                     ticket_status,credential_type,credential_payload,credential_version,dynamic_qr_mode,
                     seat_zone,seat_row,seat_number,entrance_info,issue_time,expire_time,refresh_after_seconds,
                     validate_status,error_code,error_message,data_version,create_time,update_time)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())
                    ON DUPLICATE KEY UPDATE client_ticket_no=VALUES(client_ticket_no),holder_ref=VALUES(holder_ref),
                     source_sku_id=VALUES(source_sku_id),ticket_status=VALUES(ticket_status),
                     credential_type=VALUES(credential_type),credential_payload=VALUES(credential_payload),
                     credential_version=VALUES(credential_version),dynamic_qr_mode=VALUES(dynamic_qr_mode),
                     seat_zone=VALUES(seat_zone),seat_row=VALUES(seat_row),seat_number=VALUES(seat_number),
                     entrance_info=VALUES(entrance_info),issue_time=VALUES(issue_time),expire_time=VALUES(expire_time),
                     refresh_after_seconds=VALUES(refresh_after_seconds),validate_status=VALUES(validate_status),
                     error_code=VALUES(error_code),error_message=VALUES(error_message),data_version=VALUES(data_version),
                     update_time=NOW()
                    """, providerTicketId, providerOrderId, index,
                    str(ticketUnit, "client_ticket_no"), str(ticketUnit, "holder_ref"), str(ticketUnit, "source_sku_id"),
                    fail ? "FAILED" : "ISSUED", fail ? null : type.name(), payload,
                    fail ? null : credentialVersion, fail || dynamicMode == null ? null : dynamicMode.name(),
                    zone, row, seat, entrance, fail ? null : LocalDateTime.now(),
                    type == CredentialType.DYNAMIC_QR ? LocalDateTime.now().plusSeconds(DYNAMIC_QR_TTL_SECONDS) : null,
                    type == CredentialType.DYNAMIC_QR ? DYNAMIC_QR_REFRESH_AFTER_SECONDS : null,
                    "NOT_VALIDATED", fail ? "MOCK_SINGLE_TICKET_FAILED" : null,
                    fail ? "模拟单票出票失败" : null, credentialVersion);
            jdbc.update("UPDATE mock_ticket_source_order_ticket SET provider_ticket_id=? WHERE ticket_unit_id=?",
                    providerTicketId, ticketUnit.get("ticket_unit_id"));
            if (fail) failed++; else issued++;
        }
        String deliveryStatus = failed == 0 ? "ISSUED" : issued == 0 ? "FAILED" : "PARTIAL";
        String orderStatus = failed == 0 ? "ISSUED" : issued == 0 ? "PAID" : "PARTIAL_ISSUED";
        String resultVersion = version("delivery-result");
        jdbc.update("""
                UPDATE mock_ticket_source_delivery
                SET delivery_status=?,issued_count=?,failed_count=?,last_error_code=?,last_error_message=?,
                    data_version=?,update_time=NOW() WHERE provider_order_id=?
                """, deliveryStatus, issued, failed,
                failed == 0 ? null : "MOCK_TICKET_ISSUE_FAILED",
                failed == 0 ? null : "模拟第三方部分或全部出票失败",
                resultVersion, providerOrderId);
        jdbc.update("UPDATE mock_ticket_source_order SET order_status=?,data_version=?,update_time=NOW() WHERE provider_order_id=?",
                orderStatus, resultVersion, providerOrderId);
        emitCallback(CallbackEventType.TICKET_ISSUED, "ORDER", providerOrderId, resultVersion);
        return toTicketDelivery(requiredDelivery(providerOrderId), credentialRows(providerOrderId));
    }

    private ProviderTicketDelivery toTicketDelivery(
            Map<String, Object> delivery, List<Map<String, Object>> credentials
    ) {
        List<ProviderTicketCredential> tickets = credentials.stream().map(this::toCredential).toList();
        String source = str(delivery, "delivery_status");
        TicketDeliveryStatus status = switch (source == null ? "" : source) {
            case "ISSUED", "REFUNDED" -> TicketDeliveryStatus.SUCCESS;
            case "PARTIAL" -> TicketDeliveryStatus.PARTIAL;
            case "FAILED" -> TicketDeliveryStatus.FAILED;
            case "PROCESSING" -> TicketDeliveryStatus.PROCESSING;
            default -> TicketDeliveryStatus.PENDING;
        };
        return new ProviderTicketDelivery(
                str(delivery, "provider_order_id"),
                new ProviderStatusValue<>(status, source, source),
                intValue(delivery.get("expected_ticket_count")),
                intValue(delivery.get("issued_count")),
                intValue(delivery.get("failed_count")),
                "PENDING".equals(source) ? offsetNullable(delivery.get("available_time")) : null,
                tickets,
                defaultVersion(str(delivery, "data_version"), "delivery")
        );
    }

    private ProviderTicketCredential toCredential(Map<String, Object> r) {
        String sourceStatus = str(r, "ticket_status");
        ProviderTicketStatus status = switch (sourceStatus == null ? "" : sourceStatus) {
            case "ISSUED" -> ProviderTicketStatus.UNUSED;
            case "USED" -> ProviderTicketStatus.USED;
            case "VOIDED" -> ProviderTicketStatus.VOIDED;
            case "EXPIRED" -> ProviderTicketStatus.EXPIRED;
            case "FAILED" -> ProviderTicketStatus.ERROR;
            default -> ProviderTicketStatus.GENERATING;
        };
        CredentialType type = enumValue(CredentialType.class,
                str(r, "credential_type"), CredentialType.TEXT);
        DynamicQrMode dynamicMode = type == CredentialType.DYNAMIC_QR
                ? enumValue(DynamicQrMode.class, str(r, "dynamic_qr_mode"), DynamicQrMode.REMOTE_REFRESH)
                : null;
        ProviderSeatAssignment seat = null;
        if (str(r, "seat_zone") != null || str(r, "seat_row") != null || str(r, "seat_number") != null) {
            String full = String.join(" ", nonNullStrings(
                    str(r, "seat_zone"), str(r, "seat_row"), str(r, "seat_number")));
            seat = new ProviderSeatAssignment(null, null, str(r, "seat_zone"),
                    str(r, "seat_row"), str(r, "seat_number"), full, str(r, "entrance_info"));
        }
        String payload = type == CredentialType.DYNAMIC_QR ? null : str(r, "credential_payload");
        return new ProviderTicketCredential(
                str(r, "provider_ticket_id"), str(r, "client_ticket_no"), str(r, "holder_ref"), str(r, "source_sku_id"),
                new ProviderStatusValue<>(status, sourceStatus, sourceStatus),
                type, payload, str(r, "credential_version"), dynamicMode, seat,
                enumValue(ValidateStatus.class, str(r, "validate_status"), ValidateStatus.UNKNOWN),
                offsetNullable(r.get("issue_time")), offsetNullable(r.get("expire_time")),
                str(r, "error_code"), str(r, "error_message"),
                defaultVersion(str(r, "data_version"), "ticket")
        );
    }

    private ProviderShipment toShipment(Map<String, Object> row) {
        return new ProviderShipment(
                enumValue(ShipmentStatus.class, str(row, "shipment_status"), ShipmentStatus.WAIT_SHIPMENT),
                str(row, "carrier_code"), str(row, "carrier_name"), str(row, "waybill_no"),
                offsetNullable(row.get("shipped_time")), offsetNullable(row.get("signed_time")),
                str(row, "tracking_url"), offset(row.get("update_time")),
                defaultVersion(str(row, "data_version"), "shipment")
        );
    }

    private ProviderRefund getRefundInternal(String providerRefundId, boolean advance) {
        Map<String, Object> refund = requiredRefund(providerRefundId);
        if (advance && "PROCESSING".equals(str(refund, "refund_status"))) {
            LocalDateTime available = localDateTimeNullable(refund.get("available_time"));
            if (available != null && !available.isAfter(LocalDateTime.now())) {
                completeRefund(providerRefundId);
                refund = requiredRefund(providerRefundId);
            }
        }
        return toRefund(refund);
    }

    private ProviderRefund toRefund(Map<String, Object> r) {
        String source = str(r, "refund_status");
        ProviderRefundStatus status = switch (source == null ? "" : source) {
            case "PENDING" -> ProviderRefundStatus.SUBMITTED;
            case "PROCESSING" -> ProviderRefundStatus.PROCESSING;
            case "SUCCESS" -> ProviderRefundStatus.SUCCESS;
            case "REJECTED" -> ProviderRefundStatus.REJECTED;
            case "FAILED" -> "MOCK_REFUND_REJECTED".equals(str(r, "error_code"))
                    ? ProviderRefundStatus.REJECTED : ProviderRefundStatus.FAILED;
            case "CANCELLED" -> ProviderRefundStatus.CANCELLED;
            default -> ProviderRefundStatus.PROCESSING;
        };
        return new ProviderRefund(
                str(r, "provider_refund_id"), str(r, "provider_refund_no"),
                str(r, "provider_order_id"), str(r, "client_refund_no"),
                new ProviderStatusValue<>(status, source, source),
                money(r.get("refund_amount")), money(r.get("fee_amount")),
                money(r.get("refunded_delivery_fee")),
                "PROCESSING".equals(source) ? offsetNullable(r.get("available_time")) : null,
                offsetNullable(r.get("refund_time")), str(r, "error_code"), str(r, "error_message"),
                defaultVersion(str(r, "data_version"), "refund")
        );
    }

    private void completeRefund(String providerRefundId) {
        Map<String, Object> refund = requiredRefund(providerRefundId);
        if ("SUCCESS".equals(str(refund, "refund_status"))) {
            restoreRefundInventoryIfNeeded(refund);
            return;
        }
        if ("FAILED".equals(str(refund, "refund_status"))) return;
        LocalDateTime available = localDateTimeNullable(refund.get("available_time"));
        if (available != null && available.isAfter(LocalDateTime.now())) return;
        String providerOrderId = str(refund, "provider_order_id");
        jdbc.update("""
                UPDATE mock_ticket_source_refund
                SET refund_status='SUCCESS',refund_time=NOW(),error_code=NULL,error_message=NULL,
                    data_version=?,update_time=NOW() WHERE provider_refund_id=?
                """, version("refund-success"), providerRefundId);
        jdbc.update("UPDATE mock_ticket_source_order SET order_status='REFUNDED',data_version=?,update_time=NOW() WHERE provider_order_id=?",
                version("order-refunded"), providerOrderId);
        jdbc.update("""
                UPDATE mock_ticket_source_credential
                SET ticket_status='VOIDED',credential_payload=NULL,expire_time=NOW(),data_version=?,update_time=NOW()
                WHERE provider_order_id=? AND ticket_status<>'VOIDED'
                """, version("ticket-void"), providerOrderId);
        jdbc.update("UPDATE mock_ticket_source_delivery SET delivery_status='REFUNDED',data_version=?,update_time=NOW() WHERE provider_order_id=?",
                version("delivery-refunded"), providerOrderId);
        jdbc.update("""
                UPDATE mock_ticket_source_shipment
                SET shipment_status=CASE WHEN shipment_status='DELIVERED' THEN 'RETURNED' ELSE shipment_status END,
                    data_version=?,update_time=NOW() WHERE provider_order_id=?
                """, version("shipment-refund"), providerOrderId);
        restoreRefundInventoryIfNeeded(requiredRefund(providerRefundId));
        emitCallback(CallbackEventType.REFUND_CHANGED, "REFUND", providerRefundId, version("refund-success-event"));
        emitCallback(CallbackEventType.TICKET_VOIDED, "ORDER", providerOrderId, version("ticket-void-event"));
    }

    private void restoreRefundInventoryIfNeeded(Map<String, Object> refund) {
        if (bool(refund.get("inventory_restored"))) return;
        String providerRefundId = str(refund, "provider_refund_id");
        int claimed = jdbc.update("""
                UPDATE mock_ticket_source_refund SET inventory_restored=1,update_time=NOW()
                WHERE provider_refund_id=? AND inventory_restored=0
                """, providerRefundId);
        if (claimed != 1) return;
        restoreOrderInventory(str(refund, "provider_order_id"), version("refund-restore"));
    }

    private void restoreOrderInventory(String providerOrderId, String dataVersion) {
        List<Map<String, Object>> items = jdbc.queryForList(
                "SELECT source_sku_id,quantity FROM mock_ticket_source_order_item WHERE provider_order_id=?",
                providerOrderId);
        if (items.isEmpty()) {
            Map<String, Object> order = requiredOrder(providerOrderId);
            jdbc.update("""
                    UPDATE mock_ticket_source_sku
                    SET available_stock=CASE WHEN available_stock IS NULL THEN NULL ELSE available_stock+? END,
                        version=version+1,data_version=?,update_time=NOW()
                    WHERE source_sku_id=?
                    """, intValue(order.get("quantity")), dataVersion, str(order, "source_sku_id"));
            return;
        }
        for (Map<String, Object> item : items) {
            jdbc.update("""
                    UPDATE mock_ticket_source_sku
                    SET available_stock=CASE WHEN available_stock IS NULL THEN NULL ELSE available_stock+? END,
                        version=version+1,data_version=?,update_time=NOW()
                    WHERE source_sku_id=?
                    """, intValue(item.get("quantity")), dataVersion, str(item, "source_sku_id"));
        }
    }

    private void expireOrder(String providerOrderId, Map<String, Object> order) {
        int rows = jdbc.update("""
                UPDATE mock_ticket_source_order
                SET order_status='EXPIRED',cancel_time=NOW(),cancel_reason='RESERVATION_EXPIRED',
                    data_version=?,update_time=NOW()
                WHERE provider_order_id=? AND order_status='WAIT_PAY'
                """, version("order-expire"), providerOrderId);
        if (rows == 1) restoreOrderInventory(providerOrderId, version("expire-restore"));
    }


    private void upsertShipment(
            String providerOrderId,
            ShipmentStatus status,
            String carrierCode,
            String carrierName,
            String waybillNo,
            OffsetDateTime shippedAt,
            OffsetDateTime signedAt,
            String trackingUrl
    ) {
        String version = version("shipment");
        jdbc.update("""
                INSERT INTO mock_ticket_source_shipment
                (provider_order_id,shipment_status,carrier_code,carrier_name,waybill_no,shipped_time,
                 signed_time,tracking_url,data_version,update_time)
                VALUES (?,?,?,?,?,?,?,?,?,NOW())
                ON DUPLICATE KEY UPDATE shipment_status=VALUES(shipment_status),carrier_code=VALUES(carrier_code),
                  carrier_name=VALUES(carrier_name),waybill_no=VALUES(waybill_no),shipped_time=VALUES(shipped_time),
                  signed_time=VALUES(signed_time),tracking_url=VALUES(tracking_url),data_version=VALUES(data_version),
                  update_time=NOW()
                """, providerOrderId, status.name(), carrierCode, carrierName, waybillNo,
                shippedAt == null ? null : toLocal(shippedAt), signedAt == null ? null : toLocal(signedAt),
                trackingUrl, version);
    }

    private ProviderCallbackEvent emitCallback(
            CallbackEventType eventType, String resourceType, String resourceId, String resourceVersion
    ) {
        String eventId = "MOCK-EVT-" + System.currentTimeMillis() + "-" + random4();
        LocalDateTime occurred = LocalDateTime.now();
        jdbc.update("""
                INSERT INTO mock_ticket_source_callback_event
                (event_id,event_type,resource_type,provider_resource_id,resource_version,occurred_time,create_time)
                VALUES (?,?,?,?,?,?,NOW())
                """, eventId, eventType.name(), resourceType, resourceId, resourceVersion, occurred);
        return new ProviderCallbackEvent(eventId, eventType, ProviderCode.MOCK_DAMAI,
                resourceType, resourceId, resourceVersion, occurred.atOffset(CN_OFFSET));
    }

    private ProviderCallbackEvent toCallbackEvent(Map<String, Object> r) {
        return new ProviderCallbackEvent(
                str(r, "event_id"),
                enumValue(CallbackEventType.class, str(r, "event_type"), CallbackEventType.ORDER_CHANGED),
                ProviderCode.MOCK_DAMAI,
                str(r, "resource_type"), str(r, "provider_resource_id"), str(r, "resource_version"),
                offset(r.get("occurred_time"))
        );
    }

    private void applyBehavior(String operationCode) {
        behaviorService.apply(operationCode);
    }

    private String postCommitLossMode(String operationCode) {
        return behaviorService.postCommitLossMode(operationCode);
    }

    private void registerPostCommitLoss(String mode) {
        behaviorService.registerPostCommitLoss(mode);
    }

    private Map<String, Object> requiredSku(String skuId) {
        return requiredOne("""
                SELECT sku.*,s.source_project_id
                FROM mock_ticket_source_sku sku
                JOIN mock_ticket_source_session s ON s.source_session_id=sku.source_session_id
                WHERE sku.source_sku_id=? AND sku.enabled=1 AND s.enabled=1
                """, "MOCK_TICKET_PRODUCT_NOT_FOUND", "模拟票档不存在: " + skuId, skuId);
    }

    private Map<String, Object> requiredOrder(String providerOrderId) {
        return requiredOne("SELECT * FROM mock_ticket_source_order WHERE provider_order_id=?",
                "MOCK_ORDER_NOT_FOUND", "第三方订单不存在: " + providerOrderId, providerOrderId);
    }

    private Map<String, Object> requiredDelivery(String providerOrderId) {
        return requiredOne("SELECT * FROM mock_ticket_source_delivery WHERE provider_order_id=?",
                "MOCK_DELIVERY_NOT_FOUND", "第三方履约任务不存在: " + providerOrderId, providerOrderId);
    }

    private Map<String, Object> requiredRefund(String providerRefundId) {
        return requiredOne("SELECT * FROM mock_ticket_source_refund WHERE provider_refund_id=?",
                "MOCK_REFUND_NOT_FOUND", "第三方退款不存在: " + providerRefundId, providerRefundId);
    }

    private List<Map<String, Object>> credentialRows(String providerOrderId) {
        return jdbc.queryForList("""
                SELECT * FROM mock_ticket_source_credential
                WHERE provider_order_id=? ORDER BY ticket_index,provider_ticket_id
                """, providerOrderId);
    }

    private int countOrderTickets(String providerOrderId) {
        return Optional.ofNullable(jdbc.queryForObject(
                "SELECT COUNT(*) FROM mock_ticket_source_order_ticket WHERE provider_order_id=?",
                Integer.class, providerOrderId)).orElse(0);
    }

    private BigDecimal sumOrderItems(String providerOrderId, String priceColumn) {
        if (!List.of("unit_price", "settlement_unit_price").contains(priceColumn)) {
            throw new IllegalArgumentException("不允许的金额列");
        }
        BigDecimal value = jdbc.queryForObject(
                "SELECT COALESCE(SUM(" + priceColumn + "*quantity),0) FROM mock_ticket_source_order_item WHERE provider_order_id=?",
                BigDecimal.class, providerOrderId);
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal calculatePromotionDiscount(String projectId, BigDecimal saleAmount) {
        List<Map<String, Object>> rules = jdbc.queryForList("""
                SELECT promotion_type,rule_data FROM mock_ticket_source_promotion_rule
                WHERE enabled=1 AND source_project_id=? AND valid_from<=NOW() AND valid_to>=NOW()
                ORDER BY promotion_id
                """, projectId);
        BigDecimal discount = BigDecimal.ZERO;
        for (Map<String, Object> rule : rules) {
            if (!"FULL_REDUCTION".equals(str(rule, "promotion_type"))) continue;
            Map<String, Object> data = readMap(str(rule, "rule_data"));
            long thresholdMinor = numberLong(data.get("thresholdAmountMinor"));
            long discountMinor = numberLong(data.get("discountAmountMinor"));
            if (ProviderMoney.fromMajor(saleAmount, "CNY").amountMinor() >= thresholdMinor) {
                discount = discount.add(ProviderMoney.cny(discountMinor).toMajor());
            }
        }
        return discount.min(saleAmount).setScale(2, RoundingMode.HALF_UP);
    }

    private void validateCreateIdempotency(Map<String, Object> existing, ProviderOrderCreateRequest request) {
        if (!request.clientOrderNo().equals(str(existing, "client_order_no"))
                || !request.projectId().equals(str(existing, "source_project_id"))
                || !request.sessionId().equals(str(existing, "source_session_id"))
                || !request.ticketProductId().equals(str(existing, "source_sku_id"))
                || request.quantity() != intValue(existing.get("quantity"))
                || !request.ticketMode().name().equals(str(existing, "ticket_mode"))
                || !request.deliveryMode().name().equals(str(existing, "delivery_mode"))) {
            throw conflict("MOCK_CREATE_IDEMPOTENCY_CONFLICT", "创建订单幂等键对应的请求主体不一致");
        }
        String providerOrderId = str(existing, "provider_order_id");
        List<Map<String, Object>> storedItems = jdbc.queryForList("""
                SELECT client_line_no,source_sku_id,quantity
                FROM mock_ticket_source_order_item
                WHERE provider_order_id=? ORDER BY client_line_no
                """, providerOrderId);
        if (storedItems.size() != 1
                || !request.ticketProductId().equals(str(storedItems.get(0), "source_sku_id"))
                || request.quantity() != intValue(storedItems.get(0).get("quantity"))) {
            throw conflict("MOCK_CREATE_IDEMPOTENCY_CONFLICT", "创建订单幂等键对应的单票档明细不一致");
        }
        Map<String, ProviderTicketAssignmentRequest> requestTickets = request.tickets().stream()
                .collect(Collectors.toMap(ProviderTicketAssignmentRequest::clientTicketNo, Function.identity()));
        List<Map<String, Object>> storedTickets = jdbc.queryForList("""
                SELECT client_ticket_no,holder_ref,source_sku_id
                FROM mock_ticket_source_order_ticket
                WHERE provider_order_id=? ORDER BY client_ticket_no
                """, providerOrderId);
        if (storedTickets.size() != requestTickets.size()) {
            throw conflict("MOCK_CREATE_IDEMPOTENCY_CONFLICT", "创建订单幂等键对应的逐票数量不一致");
        }
        for (Map<String, Object> stored : storedTickets) {
            ProviderTicketAssignmentRequest ticket = requestTickets.get(str(stored, "client_ticket_no"));
            if (ticket == null
                    || !ticket.holderRef().equals(str(stored, "holder_ref"))
                    || !request.ticketProductId().equals(str(stored, "source_sku_id"))) {
                throw conflict("MOCK_CREATE_IDEMPOTENCY_CONFLICT", "创建订单幂等键对应的观演人绑定不一致");
            }
        }
        if (request.expectedPayAmount() == null
                || request.expectedPayAmount().toMajor().compareTo(decimal(existing.get("pay_amount"))) != 0) {
            throw conflict("MOCK_CREATE_IDEMPOTENCY_CONFLICT", "创建订单幂等键对应的应付金额不一致");
        }
    }

    private String projectSelectSql() {
        return """
                SELECT p.source_project_id,p.source_project_name,p.project_type,p.category_code,p.category_name,
                       p.city_code,p.city_name,p.venue_id,p.venue_name,p.subtitle,p.introduction,p.detail_html,
                       p.detail_content,p.poster_url,p.show_time_text,p.sale_start_time,p.sale_end_time,
                       p.purchase_limitation_once,p.has_reserved_seat,p.sale_status,p.source_status_code,p.source_status_text,
                       p.min_price,p.max_price,p.data_version,p.update_time
                FROM mock_ticket_source_project p
                """;
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Map<String, Object> requiredOne(
            String sql, String sourceCode, String message, Object... args
    ) {
        Map<String, Object> row = one(sql, args);
        if (row == null) throw new V11AdapterException(
                V11ErrorCode.RESOURCE_NOT_FOUND, sourceCode, message, false);
        return row;
    }

    private void ensureExists(
            String table, String column, String value, String sourceCode, String message
    ) {
        if (!Set.of("mock_ticket_source_project", "mock_ticket_source_session").contains(table)
                || !Set.of("source_project_id", "source_session_id").contains(column)) {
            throw new IllegalArgumentException("不允许的存在性检查");
        }
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + column + "=? AND enabled=1",
                Integer.class, value);
        if (count == null || count == 0) {
            throw new V11AdapterException(V11ErrorCode.RESOURCE_NOT_FOUND, sourceCode, message, false);
        }
    }

    private V11AdapterException invalid(String code, String message) {
        return new V11AdapterException(V11ErrorCode.INVALID_REQUEST, code, message, false);
    }

    private V11AdapterException conflict(String code, String message) {
        return new V11AdapterException(V11ErrorCode.STATUS_CONFLICT, code, message, false);
    }

    private String json(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("模拟快照序列化失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            Object value = objectMapper.readValue(json, Map.class);
            return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        } catch (Exception e) {
            throw new V11AdapterException(V11ErrorCode.INVALID_PROVIDER_RESPONSE,
                    "MOCK_RULE_DATA_INVALID", "模拟优惠规则数据不是合法JSON", false);
        }
    }

    private ProviderStatusValue<TicketProductSaleStatus> statusValue(
            TicketProductSaleStatus status, Map<String, Object> row
    ) {
        return new ProviderStatusValue<>(status,
                str(row, "source_status_code"), str(row, "source_status_text"));
    }

    private ProjectStatus parseProjectStatus(String value) {
        return switch (upper(value, "UNKNOWN")) {
            case "PRESALE" -> ProjectStatus.PRESALE;
            case "ON_SALE" -> ProjectStatus.ON_SALE;
            case "SOLD_OUT" -> ProjectStatus.SOLD_OUT;
            case "OFF_SHELF", "SUSPENDED" -> ProjectStatus.SUSPENDED;
            case "CANCELLED", "CANCELED" -> ProjectStatus.CANCELLED;
            case "ENDED" -> ProjectStatus.ENDED;
            case "PENDING_SALE" -> ProjectStatus.PENDING_SALE;
            default -> ProjectStatus.UNKNOWN;
        };
    }

    private TicketProductSaleStatus parseTicketSaleStatus(String value) {
        return switch (upper(value, "UNKNOWN")) {
            case "ON_SALE", "PRESALE" -> TicketProductSaleStatus.ON_SALE;
            case "SOLD_OUT" -> TicketProductSaleStatus.SOLD_OUT;
            case "SALE_REMINDER" -> TicketProductSaleStatus.SALE_REMINDER;
            case "STOCK_REGISTRATION" -> TicketProductSaleStatus.STOCK_REGISTRATION;
            case "OFF_SHELF", "SUSPENDED" -> TicketProductSaleStatus.SUSPENDED;
            case "NOT_ON_SALE" -> TicketProductSaleStatus.NOT_ON_SALE;
            default -> TicketProductSaleStatus.UNKNOWN;
        };
    }

    private ProviderOrderStatus parseOrderStatus(String value) {
        return switch (upper(value, "UNKNOWN")) {
            case "WAIT_PAY" -> ProviderOrderStatus.RESERVED;
            case "PAID" -> ProviderOrderStatus.PAID;
            case "ISSUING" -> ProviderOrderStatus.ISSUING;
            case "ISSUED" -> ProviderOrderStatus.ISSUED;
            case "PARTIAL_ISSUED" -> ProviderOrderStatus.PARTIALLY_ISSUED;
            case "CANCELED", "CANCELLED" -> ProviderOrderStatus.CANCELLED;
            case "EXPIRED" -> ProviderOrderStatus.EXPIRED;
            case "REFUNDING" -> ProviderOrderStatus.REFUNDING;
            case "REFUNDED" -> ProviderOrderStatus.REFUNDED;
            case "FAILED" -> ProviderOrderStatus.FAILED;
            default -> ProviderOrderStatus.UNKNOWN;
        };
    }

    private StockState toStockState(TicketProductSaleStatus saleStatus, Integer stock) {
        if (saleStatus == TicketProductSaleStatus.SOLD_OUT || (stock != null && stock == 0)) {
            return StockState.SOLD_OUT;
        }
        if (stock == null) return StockState.UNKNOWN;
        if (stock <= 10) return StockState.LOW_STOCK;
        return StockState.AVAILABLE;
    }

    private String toMockProjectStatus(ProjectStatus status) {
        return switch (status) {
            case PRESALE, PENDING_SALE -> "PRESALE";
            case ON_SALE -> "ON_SALE";
            case SOLD_OUT -> "SOLD_OUT";
            case SUSPENDED -> "OFF_SHELF";
            case CANCELLED -> "CANCELLED";
            case ENDED -> "ENDED";
            default -> "UNKNOWN";
        };
    }

    private boolean isProjectSaleable(String saleStatus) {
        return "ON_SALE".equals(saleStatus) || "PRESALE".equals(saleStatus);
    }

    private String mockOrderStatusText(String status) {
        return switch (upper(status, "UNKNOWN")) {
            case "WAIT_PAY" -> "待支付";
            case "PAID" -> "已支付";
            case "ISSUING" -> "出票中";
            case "ISSUED" -> "已出票";
            case "PARTIAL_ISSUED" -> "部分出票";
            case "CANCELED" -> "已取消";
            case "EXPIRED" -> "已过期";
            case "REFUNDING" -> "退款中";
            case "REFUNDED" -> "已退款";
            default -> "未知";
        };
    }

    private String buildCredentialPayload(CredentialType type, String providerTicketId, String version) {
        return switch (type) {
            case URL -> "https://mock.local/ticket/" + providerTicketId;
            case TEXT, SMS_CODE, EXCHANGE_CODE -> "MOCK-CODE-" + providerTicketId;
            case ID_CARD -> "ID_CARD_ENTRY:" + providerTicketId;
            case STATIC_QR -> "MOCK:STATIC_QR:" + providerTicketId + ":" + version;
            default -> null;
        };
    }

    private long deliveryFeeMinor(ProviderAddress address) {
        if (address == null) return 0;
        // V1.3.3 B10R-5：MOCK_DAMAI 与用户侧平台配送费配置保持同一口径。
        // 当前模拟服务的 EXPRESS 默认固定为 1200 分，不再按省份制造 12/18 元差异。
        return 1200L;
    }

    private ProviderMoney money(Object value) {
        BigDecimal amount = decimalNullable(value);
        return ProviderMoney.fromMajor(amount == null ? BigDecimal.ZERO : amount, "CNY");
    }

    private BigDecimal decimal(Object value) {
        BigDecimal result = decimalNullable(value);
        return result == null ? BigDecimal.ZERO : result;
    }

    private BigDecimal decimalNullable(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return new BigDecimal(value.toString());
    }

    private BigDecimal defaultDecimal(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private String str(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        if (value == null) value = map.get(key.toUpperCase(Locale.ROOT));
        return value == null ? null : value.toString();
    }

    private boolean bool(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        return "1".equals(value.toString()) || Boolean.parseBoolean(value.toString());
    }

    private int intValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(value.toString());
    }

    private Integer nullableInt(Object value) {
        return value == null ? null : intValue(value);
    }

    private Long nullableLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString());
    }

    private long longValue(Long value) {
        return value == null ? 0L : value;
    }

    private long numberLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString());
    }

    private LocalDateTime localDateTime(Object value) {
        LocalDateTime time = localDateTimeNullable(value);
        if (time == null) throw new IllegalStateException("数据库时间字段不能为空");
        return time;
    }

    private LocalDateTime localDateTimeNullable(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime time) return time;
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        if (value instanceof Date date) return new Timestamp(date.getTime()).toLocalDateTime();
        return LocalDateTime.parse(value.toString().replace(' ', 'T'));
    }

    private OffsetDateTime offset(Object value) {
        LocalDateTime time = localDateTimeNullable(value);
        return (time == null ? LocalDateTime.now() : time).atOffset(CN_OFFSET);
    }

    private OffsetDateTime offsetNullable(Object value) {
        LocalDateTime time = localDateTimeNullable(value);
        return time == null ? null : time.atOffset(CN_OFFSET);
    }

    private LocalDateTime toLocal(OffsetDateTime value) {
        return value.withOffsetSameInstant(CN_OFFSET).toLocalDateTime();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(CN_OFFSET);
    }

    private String version(String prefix) {
        return "mock-v11-" + prefix + "-" + System.currentTimeMillis();
    }

    private String defaultVersion(String value, String prefix) {
        return value == null || value.isBlank() ? version(prefix) : value;
    }

    private int random4() {
        return ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String upper(String value, String fallback) {
        String normalized = blankToNull(value);
        return normalized == null ? fallback : normalized.toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            String normalized = blankToNull(value);
            if (normalized != null) return normalized;
        }
        return null;
    }

    private List<String> csv(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
    }

    private List<String> singletonNullable(String value) {
        return value == null ? List.of() : List.of(value);
    }

    private List<String> nonNullStrings(String... values) {
        if (values == null) return List.of();
        return Arrays.stream(values).filter(Objects::nonNull).filter(v -> !v.isBlank()).toList();
    }

    private <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

}
