package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminTicketSourceProviderUpdateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 第三方票源管理后台聚合查询。
 *
 * 自动交易写操作继续复用既有 ticket-source Admin API / Adapter；
 * 本 Service 负责列表、统计和 Provider 配置。活动审核/账期结算由 AdminTicketSourceBusinessService 承担。
 */
@Service
public class AdminTicketSourceConsoleService {
    private final JdbcTemplate jdbc;

    public AdminTicketSourceConsoleService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> providers() {
        return jdbc.queryForList("""
                SELECT provider_id AS providerId,provider_code AS providerCode,provider_name AS providerName,
                       provider_type AS providerType,access_mode AS accessMode,adapter_code AS adapterCode,
                       provider_status AS providerStatus,priority,base_url AS baseUrl,credential_ref AS credentialRef,
                       connect_timeout_ms AS connectTimeoutMs,read_timeout_ms AS readTimeoutMs,remark,version,
                       update_time AS updateTime
                FROM ticket_source_provider
                WHERE provider_code='MOCK_DAMAI' AND provider_type='MOCK'
                ORDER BY priority ASC,provider_id ASC
                """);
    }

    public Map<String, Object> summary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("providerCount", scalar("SELECT COUNT(*) FROM ticket_source_provider WHERE provider_code='MOCK_DAMAI' AND provider_type='MOCK'"));
        m.put("enabledProviderCount", scalar("SELECT COUNT(*) FROM ticket_source_provider WHERE provider_code='MOCK_DAMAI' AND provider_type='MOCK' AND provider_status='ENABLED'"));
        m.put("projectMappingCount", scalar("SELECT COUNT(*) FROM ticket_source_project_mapping WHERE mapping_status='BOUND'"));
        m.put("skuMappingCount", scalar("SELECT COUNT(*) FROM ticket_source_sku_mapping WHERE mapping_status='BOUND'"));
        m.put("orderBridgeCount", scalar("SELECT COUNT(*) FROM ticket_source_order_bridge"));
        m.put("manualReviewOrderCount", scalar("SELECT COUNT(*) FROM ticket_source_order_bridge WHERE bridge_status='MANUAL_REVIEW'"));
        m.put("unknownResultOrderCount", scalar("SELECT COUNT(*) FROM ticket_source_order_bridge WHERE bridge_status='UNKNOWN_RESULT'"));
        m.put("pendingIssueCount", scalar("SELECT COUNT(*) FROM ticket_source_issue_task WHERE task_status IN ('PENDING','WAIT_PROVIDER','RETRY_WAIT','PARTIAL','FAILED','MANUAL_REVIEW')"));
        m.put("pendingShipmentCount", scalar("SELECT COUNT(*) FROM ticket_source_shipment WHERE shipment_status IN ('WAIT_SHIPMENT','SHIPPED','IN_TRANSIT','EXCEPTION')"));
        m.put("pendingRefundCount", scalar("SELECT COUNT(*) FROM ticket_source_refund_bridge WHERE bridge_status IN ('PENDING_REVIEW','REQUESTING','PROCESSING','RETRY_WAIT','MANUAL_REVIEW')"));
        m.put("pendingCallbackCount", scalar("SELECT COUNT(*) FROM ticket_source_callback_event WHERE process_status IN ('PENDING','FAILED')"));
        m.put("reconciliationDifferenceCount", scalar("SELECT COUNT(*) FROM ticket_source_reconciliation_detail WHERE compare_status IN ('DIFFERENCE','ERROR')"));
        m.put("pendingCampaignReviewCount", scalar("SELECT COUNT(*) FROM ticket_source_campaign_asset WHERE source_enabled=1 AND review_status='PENDING'"));
        m.put("draftSettlementCount", scalar("SELECT COUNT(*) FROM ticket_source_settlement_period WHERE period_status='DRAFT'"));
        m.put("confirmedSettlementCount", scalar("SELECT COUNT(*) FROM ticket_source_settlement_period WHERE period_status='CONFIRMED'"));
        m.put("gatewayFailure24h", scalar("SELECT COUNT(*) FROM ticket_source_gateway_log WHERE success=0 AND call_time >= DATE_SUB(NOW(), INTERVAL 1 DAY)"));
        return m;
    }

    public List<Map<String, Object>> mappings(String providerCode, String keyword, Integer limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT pm.mapping_id AS mappingId,p.provider_code AS providerCode,
                       pm.provider_project_id AS providerProjectId,pm.provider_project_name AS providerProjectName,
                       pm.project_id AS projectId,lp.title AS localProjectTitle,pm.mapping_status AS mappingStatus,
                       pm.source_sale_status AS sourceSaleStatus,pm.last_sync_status AS lastSyncStatus,
                       pm.last_sync_time AS lastSyncTime,pm.last_error_code AS lastErrorCode,
                       pm.last_error_message AS lastErrorMessage,
                       (SELECT COUNT(*) FROM ticket_source_session_mapping sm WHERE sm.project_mapping_id=pm.mapping_id) AS sessionCount,
                       (SELECT COUNT(*) FROM ticket_source_sku_mapping km JOIN ticket_source_session_mapping sm2 ON sm2.mapping_id=km.session_mapping_id WHERE sm2.project_mapping_id=pm.mapping_id) AS skuCount
                FROM ticket_source_project_mapping pm
                JOIN ticket_source_provider p ON p.provider_id=pm.provider_id
                LEFT JOIN performance_project lp ON lp.project_id=pm.project_id
                WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        if (text(providerCode) != null) { sql.append(" AND p.provider_code=?"); args.add(providerCode.trim().toUpperCase(Locale.ROOT)); }
        if (text(keyword) != null) {
            sql.append(" AND (pm.provider_project_id LIKE ? OR pm.provider_project_name LIKE ? OR lp.title LIKE ? OR CAST(pm.project_id AS CHAR) LIKE ?)");
            String like="%"+keyword.trim()+"%"; args.add(like); args.add(like); args.add(like); args.add(like);
        }
        sql.append(" ORDER BY pm.mapping_id DESC LIMIT ?"); args.add(safeLimit(limit, 100, 300));
        return jdbc.queryForList(sql.toString(), args.toArray());
    }


    public List<Map<String, Object>> mappingsByProjects(String providerCode, List<String> providerProjectIds) {
        String safeProviderCode = text(providerCode);
        if (safeProviderCode == null) throw new BusinessException("providerCode 不能为空");
        List<String> ids = providerProjectIds == null ? List.of() : providerProjectIds.stream()
                .filter(value -> text(value) != null)
                .map(String::trim)
                .distinct()
                .limit(50)
                .toList();
        if (ids.isEmpty()) return List.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        List<Object> args = new ArrayList<>();
        args.add(safeProviderCode.toUpperCase(Locale.ROOT));
        args.addAll(ids);
        String sql = """
                SELECT pm.mapping_id AS mappingId,p.provider_code AS providerCode,
                       pm.provider_project_id AS providerProjectId,pm.provider_project_name AS providerProjectName,
                       pm.project_id AS projectId,lp.title AS localProjectTitle,pm.mapping_status AS mappingStatus,
                       pm.source_sale_status AS sourceSaleStatus,pm.last_sync_status AS lastSyncStatus,
                       pm.last_sync_time AS lastSyncTime,pm.last_error_code AS lastErrorCode,
                       pm.last_error_message AS lastErrorMessage
                FROM ticket_source_project_mapping pm
                JOIN ticket_source_provider p ON p.provider_id=pm.provider_id
                LEFT JOIN performance_project lp ON lp.project_id=pm.project_id
                WHERE p.provider_code=?
                  AND pm.provider_project_id IN (""" + placeholders + ") ORDER BY pm.mapping_id DESC";
        return jdbc.queryForList(sql, args.toArray());
    }

    public List<Map<String, Object>> orders(String providerCode, String bridgeStatus, String keyword, Integer limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT b.bridge_id AS bridgeId,b.order_id AS orderId,o.order_no AS orderNo,o.user_id AS userId,
                       pp.title AS projectTitle,o.order_status AS localOrderStatus,o.payment_status AS paymentStatus,
                       p.provider_code AS providerCode,b.provider_order_id AS providerOrderId,
                       b.provider_order_no AS providerOrderNo,b.provider_sku_id AS providerSkuId,
                       b.bridge_status AS bridgeStatus,b.provider_order_status AS providerOrderStatus,
                       b.quantity AS quantity,b.pay_amount AS payAmount,b.currency_code AS currencyCode,
                       b.reservation_expire_time AS reservationExpireTime,b.last_operation AS lastOperation,
                       b.last_sync_status AS lastSyncStatus,b.last_error_code AS lastErrorCode,
                       b.last_error_message AS lastErrorMessage,b.last_error_retryable AS lastErrorRetryable,
                       b.unknown_result_since AS unknownResultSince,b.create_recovery_attempts AS createRecoveryAttempts,
                       b.last_recovery_time AS lastRecoveryTime,
                       b.create_time AS createTime,b.update_time AS updateTime
                FROM ticket_source_order_bridge b
                JOIN ticket_order o ON o.order_id=b.order_id
                JOIN ticket_source_provider p ON p.provider_id=b.provider_id
                LEFT JOIN performance_project pp ON pp.project_id=o.project_id
                WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        if (text(providerCode) != null) { sql.append(" AND p.provider_code=?"); args.add(providerCode.trim().toUpperCase(Locale.ROOT)); }
        if (text(bridgeStatus) != null) { sql.append(" AND b.bridge_status=?"); args.add(bridgeStatus.trim().toUpperCase(Locale.ROOT)); }
        if (text(keyword) != null) {
            sql.append(" AND (o.order_no LIKE ? OR b.provider_order_id LIKE ? OR b.provider_order_no LIKE ? OR CAST(b.order_id AS CHAR) LIKE ? OR pp.title LIKE ?)");
            String like="%"+keyword.trim()+"%"; for(int i=0;i<5;i++) args.add(like);
        }
        sql.append(" ORDER BY b.bridge_id DESC LIMIT ?"); args.add(safeLimit(limit,100,300));
        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    public Map<String, Object> orderPage(String providerCode, String bridgeStatus, String keyword, Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null ? 20 : Math.max(10, Math.min(pageSize, 100));
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (text(providerCode) != null) { where.append(" AND p.provider_code=?"); args.add(providerCode.trim().toUpperCase(Locale.ROOT)); }
        if (text(bridgeStatus) != null) { where.append(" AND b.bridge_status=?"); args.add(bridgeStatus.trim().toUpperCase(Locale.ROOT)); }
        if (text(keyword) != null) {
            where.append(" AND (o.order_no LIKE ? OR b.provider_order_id LIKE ? OR b.provider_order_no LIKE ? OR CAST(b.order_id AS CHAR) LIKE ? OR pp.title LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            for (int i = 0; i < 5; i++) args.add(like);
        }
        String joins = """
                FROM ticket_source_order_bridge b
                JOIN ticket_order o ON o.order_id=b.order_id
                JOIN ticket_source_provider p ON p.provider_id=b.provider_id
                LEFT JOIN performance_project pp ON pp.project_id=o.project_id
                """;
        Long totalValue = jdbc.queryForObject("SELECT COUNT(*) " + joins + where, Long.class, args.toArray());
        long total = totalValue == null ? 0L : totalValue;
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) safePageSize));
        safePageNo = Math.min(safePageNo, totalPages);
        int offset = (safePageNo - 1) * safePageSize;
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(safePageSize);
        pageArgs.add(offset);
        String sql = """
                SELECT b.bridge_id AS bridgeId,b.order_id AS orderId,o.order_no AS orderNo,o.user_id AS userId,
                       pp.title AS projectTitle,o.order_status AS localOrderStatus,o.payment_status AS paymentStatus,
                       p.provider_code AS providerCode,b.provider_order_id AS providerOrderId,
                       b.provider_order_no AS providerOrderNo,b.provider_sku_id AS providerSkuId,
                       b.bridge_status AS bridgeStatus,b.provider_order_status AS providerOrderStatus,
                       b.quantity AS quantity,b.pay_amount AS payAmount,b.currency_code AS currencyCode,
                       b.reservation_expire_time AS reservationExpireTime,b.last_operation AS lastOperation,
                       b.last_sync_status AS lastSyncStatus,b.last_error_code AS lastErrorCode,
                       b.last_error_message AS lastErrorMessage,b.last_error_retryable AS lastErrorRetryable,
                       b.unknown_result_since AS unknownResultSince,b.create_recovery_attempts AS createRecoveryAttempts,
                       b.last_recovery_time AS lastRecoveryTime,b.create_time AS createTime,b.update_time AS updateTime
                """ + joins + where + " ORDER BY b.bridge_id DESC LIMIT ? OFFSET ?";
        List<Map<String,Object>> items = jdbc.queryForList(sql, pageArgs.toArray());
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("pageNo", safePageNo);
        result.put("pageSize", safePageSize);
        return result;
    }

    public List<Map<String, Object>> issues(String status, Integer limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT t.task_id AS taskId,t.order_id AS orderId,o.order_no AS orderNo,p.provider_code AS providerCode,
                       o.order_status AS orderStatus,o.payment_status AS paymentStatus,b.order_model AS orderModel,
                       t.provider_order_id AS providerOrderId,t.task_status AS taskStatus,
                       t.provider_delivery_status AS providerDeliveryStatus,t.expected_ticket_count AS expectedTicketCount,
                       t.issued_count AS issuedCount,t.failed_count AS failedCount,t.retry_count AS retryCount,
                       t.max_retry_count AS maxRetryCount,t.request_sent AS requestSent,t.manual_hold AS manualHold,
                       t.next_attempt_time AS nextAttemptTime,t.last_attempt_time AS lastAttemptTime,
                       t.complete_time AS completeTime,t.last_operation AS lastOperation,
                       t.last_error_code AS lastErrorCode,t.last_error_message AS lastErrorMessage,t.update_time AS updateTime
                FROM ticket_source_issue_task t
                JOIN ticket_order o ON o.order_id=t.order_id
                JOIN ticket_source_order_bridge b ON b.bridge_id=t.bridge_id
                JOIN ticket_source_provider p ON p.provider_id=t.provider_id
                WHERE 1=1
                """);
        List<Object> args=new ArrayList<>();
        if(text(status)!=null){sql.append(" AND t.task_status=?");args.add(status.trim().toUpperCase(Locale.ROOT));}
        sql.append(" ORDER BY t.task_id DESC LIMIT ?");args.add(safeLimit(limit,100,300));
        return jdbc.queryForList(sql.toString(),args.toArray());
    }

    public List<Map<String, Object>> shipments(String status, Integer limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.shipment_id AS shipmentId,b.order_id AS orderId,o.order_no AS orderNo,p.provider_code AS providerCode,
                       b.provider_order_id AS providerOrderId,s.shipment_status AS shipmentStatus,
                       s.carrier_code AS carrierCode,s.carrier_name AS carrierName,s.waybill_no AS waybillNo,
                       s.shipped_time AS shippedTime,s.signed_time AS signedTime,s.last_sync_time AS lastSyncTime,
                       s.last_sync_status AS lastSyncStatus,s.last_error_code AS lastErrorCode,
                       s.last_error_message AS lastErrorMessage,s.update_time AS updateTime
                FROM ticket_source_shipment s
                JOIN ticket_source_order_bridge b ON b.bridge_id=s.order_bridge_id
                JOIN ticket_order o ON o.order_id=b.order_id
                JOIN ticket_source_provider p ON p.provider_id=b.provider_id
                WHERE 1=1
                """);
        List<Object> args=new ArrayList<>();
        if(text(status)!=null){sql.append(" AND s.shipment_status=?");args.add(status.trim().toUpperCase(Locale.ROOT));}
        sql.append(" ORDER BY s.shipment_id DESC LIMIT ?");args.add(safeLimit(limit,100,300));
        return jdbc.queryForList(sql.toString(),args.toArray());
    }

    public List<Map<String, Object>> refunds(String status, Integer limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT rb.bridge_id AS bridgeId,rb.refund_id AS refundId,r.refund_no AS refundNo,
                       rb.order_id AS orderId,o.order_no AS orderNo,p.provider_code AS providerCode,
                       o.order_status AS orderStatus,o.payment_status AS paymentStatus,ob.order_model AS orderModel,
                       rb.provider_order_id AS providerOrderId,rb.provider_refund_id AS providerRefundId,
                       rb.bridge_status AS bridgeStatus,rb.provider_refund_status AS providerRefundStatus,
                       r.refund_status AS localRefundStatus,rb.refund_amount AS refundAmount,rb.fee_amount AS feeAmount,
                       (SELECT COALESCE(SUM(COALESCE(oib.settlement_unit_price,oi.settlement_price,0) * oib.quantity),0)
                          FROM ticket_source_order_item_bridge oib
                          LEFT JOIN order_item oi ON oi.order_item_id=oib.order_item_id
                         WHERE oib.bridge_id=rb.order_bridge_id) AS providerSettlementBaseAmount,
                       rb.retry_count AS retryCount,rb.max_retry_count AS maxRetryCount,rb.manual_hold AS manualHold,
                       rb.next_attempt_time AS nextAttemptTime,rb.last_operation AS lastOperation,
                       rb.last_sync_status AS lastSyncStatus,rb.last_error_code AS lastErrorCode,
                       rb.last_error_message AS lastErrorMessage,rb.update_time AS updateTime
                FROM ticket_source_refund_bridge rb
                JOIN refund_record r ON r.refund_id=rb.refund_id
                JOIN ticket_order o ON o.order_id=rb.order_id
                JOIN ticket_source_order_bridge ob ON ob.bridge_id=rb.order_bridge_id
                JOIN ticket_source_provider p ON p.provider_id=rb.provider_id
                WHERE 1=1
                """);
        List<Object> args=new ArrayList<>();
        if(text(status)!=null){sql.append(" AND rb.bridge_status=?");args.add(status.trim().toUpperCase(Locale.ROOT));}
        sql.append(" ORDER BY rb.bridge_id DESC LIMIT ?");args.add(safeLimit(limit,100,300));
        return jdbc.queryForList(sql.toString(),args.toArray());
    }

    public List<Map<String, Object>> callbacks(String status, Integer limit) {
        StringBuilder sql=new StringBuilder("""
                SELECT ce.event_id AS eventId,p.provider_code AS providerCode,ce.provider_event_id AS providerEventId,
                       ce.event_type AS eventType,ce.resource_type AS resourceType,ce.provider_resource_id AS providerResourceId,
                       ce.event_version AS eventVersion,ce.process_status AS processStatus,ce.retry_count AS retryCount,
                       ce.signature_valid AS signatureValid,ce.next_attempt_time AS nextAttemptTime,
                       ce.processed_time AS processedTime,ce.last_error_code AS lastErrorCode,
                       ce.last_error_message AS lastErrorMessage,ce.create_time AS createTime,ce.update_time AS updateTime
                FROM ticket_source_callback_event ce
                JOIN ticket_source_provider p ON p.provider_id=ce.provider_id
                WHERE 1=1
                """);
        List<Object> args=new ArrayList<>();
        if(text(status)!=null){sql.append(" AND ce.process_status=?");args.add(status.trim().toUpperCase(Locale.ROOT));}
        sql.append(" ORDER BY ce.event_id DESC LIMIT ?");args.add(safeLimit(limit,100,300));
        return jdbc.queryForList(sql.toString(),args.toArray());
    }

    public List<Map<String, Object>> reconciliations(String providerCode, Integer limit) {
        StringBuilder sql=new StringBuilder("""
                SELECT batch_id AS batchId,batch_no AS batchNo,provider_code AS providerCode,batch_status AS batchStatus,
                       total_count AS totalCount,matched_count AS matchedCount,difference_count AS differenceCount,
                       error_count AS errorCount,start_time AS startTime,finish_time AS finishTime,remark,
                       create_time AS createTime,update_time AS updateTime
                FROM ticket_source_reconciliation_batch WHERE 1=1
                """);
        List<Object> args=new ArrayList<>();
        if(text(providerCode)!=null){sql.append(" AND provider_code=?");args.add(providerCode.trim().toUpperCase(Locale.ROOT));}
        sql.append(" ORDER BY batch_id DESC LIMIT ?");args.add(safeLimit(limit,50,200));
        return jdbc.queryForList(sql.toString(),args.toArray());
    }


    public Map<String, Object> updateProvider(Long providerId, AdminTicketSourceProviderUpdateRequest request) {
        if (providerId == null || providerId <= 0) throw new BusinessException("providerId 必须为正整数");
        if (request == null) throw new BusinessException("Provider 配置不能为空");
        String status = text(request.providerStatus());
        if (status == null || !(status.equalsIgnoreCase("ENABLED") || status.equalsIgnoreCase("DISABLED"))) {
            throw new BusinessException("providerStatus 仅支持 ENABLED / DISABLED");
        }
        int priority = request.priority() == null ? 100 : Math.max(0, Math.min(request.priority(), 9999));
        int connect = request.connectTimeoutMs() == null ? 3000 : Math.max(100, Math.min(request.connectTimeoutMs(), 60000));
        int read = request.readTimeoutMs() == null ? 10000 : Math.max(100, Math.min(request.readTimeoutMs(), 120000));
        int rows = jdbc.update("""
                UPDATE ticket_source_provider
                SET provider_status=?, priority=?, base_url=?, credential_ref=?, connect_timeout_ms=?, read_timeout_ms=?,
                    remark=?, version=version+1, update_time=NOW()
                WHERE provider_id=? AND provider_code='MOCK_DAMAI' AND provider_type='MOCK'
                """, status.toUpperCase(Locale.ROOT), priority, text(request.baseUrl()), text(request.credentialRef()),
                connect, read, text(request.remark()), providerId);
        if (rows != 1) throw new BusinessException("Provider 不存在");
        return jdbc.queryForMap("""
                SELECT provider_id AS providerId,provider_code AS providerCode,provider_name AS providerName,
                       provider_type AS providerType,access_mode AS accessMode,adapter_code AS adapterCode,
                       provider_status AS providerStatus,priority,base_url AS baseUrl,credential_ref AS credentialRef,
                       connect_timeout_ms AS connectTimeoutMs,read_timeout_ms AS readTimeoutMs,remark,version,update_time AS updateTime
                FROM ticket_source_provider WHERE provider_id=?
                """, providerId);
    }

    private long scalar(String sql) {
        Long v=jdbc.queryForObject(sql,Long.class); return v==null?0L:v;
    }
    private static int safeLimit(Integer value,int def,int max){return value==null?def:Math.max(1,Math.min(value,max));}
    private static String text(String value){return value==null||value.trim().isEmpty()?null:value.trim();}
}
