package com.example.maimaibackend.ticketsource.workflow;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceOperation;
import com.example.maimaibackend.ticketsource.provider.adapter.V11AdapterException;
import com.example.maimaibackend.ticketsource.provider.enums.*;
import com.example.maimaibackend.ticketsource.provider.model.*;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.V11ShipmentService;
import com.example.maimaibackend.ticketsource.resource.provider.V11ResourceAdapterInvoker;
import com.example.maimaibackend.ticketsource.resource.provider.model.V11ResourceSyncRequest;
import com.example.maimaibackend.ticketsource.resource.provider.V11ResourceSyncService;
import com.example.maimaibackend.ticketsource.workflow.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 第三方票源统一运行链路：第三方履约、动态二维码、整单退款、回调补查与对账。
 *
 * 关键边界：
 * 1) 正式票码/座位/退款结果均以第三方查询为事实源；
 * 2) 逐票映射只允许 clientTicketNo + holderRef，不使用数组下标；
 * 3) V1.2 一个订单只有一个 provider_sku_id；
 * 4) DYNAMIC_QR 明文只作为本次 HTTP 响应返回，绝不落库；
 * 5) 回调只落收件箱，处理阶段必须再查询第三方。
 */
@Service
public class TicketSourceWorkflowService {
    private static final ZoneOffset CN = ZoneOffset.ofHours(8);
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final V11ResourceAdapterInvoker invoker;
    private final V11ShipmentService shipmentService;
    private final V11ResourceSyncService resourceSyncService;
    private final TicketSourceWorkflowProperties properties;
    private final TransactionTemplate tx;

    public TicketSourceWorkflowService(JdbcTemplate jdbc,
                            ObjectMapper objectMapper,
                            V11ResourceAdapterInvoker invoker,
                            V11ShipmentService shipmentService,
                            V11ResourceSyncService resourceSyncService,
                            TicketSourceWorkflowProperties properties,
                            PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.invoker = invoker;
        this.shipmentService = shipmentService;
        this.resourceSyncService = resourceSyncService;
        this.properties = properties;
        this.tx = new TransactionTemplate(transactionManager);
    }

    // ---------------------------------------------------------------------
    // Fulfillment
    // ---------------------------------------------------------------------

    public Map<String, Object> getFulfillment(Long orderId) {
        requirePositive(orderId, "orderId");
        ensureIssueTask(orderId);
        return issueTaskView(orderId);
    }

    public boolean isSingleSkuProviderOrder(Long orderId) {
        if (orderId == null || orderId <= 0) return false;
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM ticket_source_order_bridge WHERE order_id=? AND order_model='SINGLE_SKU'", Integer.class, orderId);
        return count != null && count > 0;
    }

    public List<Map<String, Object>> listTickets(Long orderId, Long userId) {
        requiredOrderContext(orderId, userId);
        return jdbc.queryForList("""
                SELECT et.ticket_id AS ticketId,et.ticket_no AS ticketNo,et.ticket_status AS ticketStatus,
                       et.provider_ticket_id AS providerTicketId,et.provider_ticket_product_id AS ticketProductId,
                       et.credential_type AS credentialType,et.dynamic_qr_mode AS dynamicQrMode,
                       et.credential_version AS credentialVersion,et.credential_expire_time AS credentialExpireTime,
                       et.refresh_after_seconds AS refreshAfterSeconds,
                       CASE WHEN et.credential_payload IS NULL AND et.qr_code_value IS NULL THEN 0 ELSE 1 END AS credentialPayloadStored,
                       et.seat_info AS seatInfo,oa.client_ticket_no AS clientTicketNo,oa.holder_ref AS holderRef,
                       oa.real_name AS holderName
                FROM electronic_ticket et
                JOIN order_audience oa ON oa.order_audience_id=et.order_audience_id
                WHERE et.order_id=? ORDER BY et.ticket_id
                """, orderId);
    }

    public Map<String, Object> processFulfillment(Long orderId) {
        requirePositive(orderId, "orderId");
        ensureIssueTask(orderId);
        Map<String, Object> ctx = requiredOrderContext(orderId, null);
        requireFulfillableLocalOrder(ctx);
        Map<String, Object> task = requiredOne("SELECT * FROM ticket_source_issue_task WHERE order_id=?", orderId);
        if ("SUCCESS".equals(str(task, "task_status"))) return issueTaskView(orderId);
        if (bool(task.get("manual_hold"))) throw new BusinessException("履约任务处于人工处理状态");

        String providerCode = str(ctx, "provider_code");
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(providerCode);
        ProviderCapabilities caps = invoker.invoke(target, TicketSourceOperation.HEALTH,
                (adapter, callCtx) -> adapter.capabilities(callCtx));
        IssueTriggerMode mode = caps.issueTriggerMode();
        if (mode == null) throw new BusinessException("第三方未声明履约触发模式");

        jdbc.update("UPDATE ticket_source_issue_task SET task_status='PROCESSING',last_attempt_time=NOW(),last_operation=?,last_error_code=NULL,last_error_message=NULL,update_time=NOW() WHERE order_id=?",
                bool(task.get("request_sent")) ? "GET_TICKETS" : mode.name(), orderId);

        ProviderTicketDelivery delivery;
        try {
            if (!bool(task.get("request_sent")) && mode == IssueTriggerMode.EXPLICIT_TRIGGER_REQUIRED) {
                delivery = invoker.invoke(target, TicketSourceOperation.TRIGGER_FULFILLMENT,
                        (adapter, callCtx) -> adapter.triggerFulfillment(callCtx,
                                str(ctx, "provider_order_id"),
                                new ProviderFulfillmentTriggerRequest(
                                        str(ctx, "order_no"),
                                        intValue(task.get("expected_ticket_count")),
                                        str(task, "issue_idempotency_key"))));
                jdbc.update("UPDATE ticket_source_issue_task SET request_sent=1,last_operation='TRIGGER_FULFILLMENT',update_time=NOW() WHERE order_id=?", orderId);
            } else {
                // AUTO_AFTER_PAYMENT / CONFIRM_ORDER_TRIGGERS_ISSUE / QUERY_ONLY 均只查询，不重复触发。
                delivery = invoker.invoke(target, TicketSourceOperation.GET_TICKETS,
                        (adapter, callCtx) -> adapter.getTickets(callCtx, str(ctx, "provider_order_id")));
            }
            applyDelivery(orderId, ctx, delivery);
        } catch (RuntimeException ex) {
            recordIssueFailure(orderId, ex);
            throw invoker.translate("同步V1.2第三方履约", ex);
        }
        return issueTaskView(orderId);
    }

    /**
     * 管理后台只读式“同步供应商履约状态”。
     * 仅调用 GET_TICKETS，绝不在此入口首次触发 TRIGGER_FULFILLMENT。
     */
    public Map<String, Object> syncFulfillmentStatus(Long orderId) {
        requirePositive(orderId, "orderId");
        ensureIssueTask(orderId);
        Map<String, Object> ctx = requiredOrderContext(orderId, null);
        requireFulfillableLocalOrder(ctx);
        Map<String, Object> task = requiredOne("SELECT * FROM ticket_source_issue_task WHERE order_id=?", orderId);
        if ("SUCCESS".equals(str(task, "task_status"))) return issueTaskView(orderId);

        String providerCode = str(ctx, "provider_code");
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(providerCode);
        jdbc.update("UPDATE ticket_source_issue_task SET task_status='PROCESSING',last_attempt_time=NOW(),last_operation='GET_TICKETS',last_error_code=NULL,last_error_message=NULL,update_time=NOW() WHERE order_id=?", orderId);
        try {
            ProviderTicketDelivery delivery = invoker.invoke(target, TicketSourceOperation.GET_TICKETS,
                    (adapter, callCtx) -> adapter.getTickets(callCtx, str(ctx, "provider_order_id")));
            applyDelivery(orderId, ctx, delivery);
        } catch (RuntimeException ex) {
            recordIssueFailure(orderId, ex);
            throw invoker.translate("同步V1.2第三方履约状态", ex);
        }
        return issueTaskView(orderId);
    }

    public V12BatchResult processDueFulfillment(int requestedLimit) {
        int limit = Math.max(1, Math.min(100, requestedLimit));
        List<Long> ids = jdbc.queryForList("""
                SELECT order_id FROM ticket_source_issue_task
                WHERE manual_hold=0 AND task_status IN ('PENDING','WAIT_PROVIDER','RETRY_WAIT','PARTIAL','FAILED')
                  AND (next_attempt_time IS NULL OR next_attempt_time<=NOW())
                ORDER BY COALESCE(next_attempt_time,create_time),task_id LIMIT ?
                """, Long.class, limit);
        return runBatch(ids, this::processFulfillment);
    }

    /**
     * 旧后台“手动重试”入口的 V1.2 安全实现。
     * 已经向第三方发送过履约触发时保留 request_sent=1，重试只主动查询，不重复触发。
     */
    public Map<String, Object> retryFulfillment(Long orderId) {
        requirePositive(orderId, "orderId");
        ensureIssueTask(orderId);
        Map<String, Object> task = requiredOne("SELECT task_status,request_sent FROM ticket_source_issue_task WHERE order_id=?", orderId);
        String status = str(task, "task_status");
        if ("SUCCESS".equals(status)) throw new BusinessException("订单已经全部履约成功");
        if (!Set.of("FAILED", "PARTIAL", "MANUAL_REVIEW", "RETRY_WAIT").contains(status)) {
            throw new BusinessException("当前履约任务状态不允许手动重试");
        }
        tx.executeWithoutResult(txStatus -> {
            jdbc.update("""
                    UPDATE electronic_ticket
                    SET ticket_status='GENERATING',abnormal_reason=NULL,credential_payload=NULL,qr_code_value=NULL,
                        credential_type=NULL,dynamic_qr_mode=NULL,credential_version=NULL,credential_expire_time=NULL,refresh_after_seconds=NULL,
                        provider_ticket_id=NULL,provider_ticket_product_id=NULL,provider_issue_time=NULL,last_source_sync_time=NOW(),update_time=NOW()
                    WHERE order_id=? AND ticket_status='ERROR'
                    """, orderId);
            int rows = jdbc.update("""
                    UPDATE ticket_source_issue_task
                    SET task_status='PENDING',provider_delivery_status='UNKNOWN',retry_count=0,
                        issued_count=(SELECT COUNT(*) FROM electronic_ticket WHERE order_id=? AND ticket_status='UNUSED'),
                        failed_count=0,next_attempt_time=NOW(),complete_time=NULL,manual_hold=0,
                        last_error_code=NULL,last_error_message=NULL,last_error_retryable=0,last_operation='MANUAL_RETRY',
                        version=version+1,update_time=NOW()
                    WHERE order_id=? AND task_status IN ('FAILED','PARTIAL','MANUAL_REVIEW','RETRY_WAIT')
                    """, orderId, orderId);
            if (rows != 1) throw new BusinessException("重置V1.2履约任务失败，请刷新后重试");
        });
        return processFulfillment(orderId);
    }

    private void ensureIssueTask(Long orderId) {
        jdbc.update("""
                INSERT IGNORE INTO ticket_source_issue_task
                (order_id,bridge_id,provider_id,provider_order_id,task_status,provider_delivery_status,expected_ticket_count,
                 issued_count,failed_count,retry_count,max_retry_count,issue_idempotency_key,request_sent,manual_hold,
                 next_attempt_time,last_operation,create_time,update_time)
                SELECT o.order_id,b.bridge_id,b.provider_id,b.provider_order_id,'PENDING','UNKNOWN',COUNT(oa.order_audience_id),
                       0,0,0,?,CONCAT('FULFILL:',o.order_no),0,0,NOW(),'ENSURE_TASK',NOW(),NOW()
                FROM ticket_order o
                JOIN ticket_source_order_bridge b ON b.order_id=o.order_id
                JOIN order_audience oa ON oa.order_id=o.order_id
                WHERE o.order_id=? AND o.fulfillment_mode='TICKET_SOURCE' AND b.order_model='SINGLE_SKU'
                  AND b.provider_order_id IS NOT NULL
                GROUP BY o.order_id,b.bridge_id,b.provider_id,b.provider_order_id,o.order_no
                """, properties.getMaxRetryCount(), orderId);
        if (one("SELECT task_id FROM ticket_source_issue_task WHERE order_id=?", orderId) == null) {
            throw new BusinessException("订单尚未创建V1.2第三方履约任务");
        }
    }

    private void applyDelivery(Long orderId, Map<String, Object> ctx, ProviderTicketDelivery delivery) {
        if (delivery == null || delivery.deliveryStatus() == null) throw new BusinessException("第三方未返回有效履约结果");
        int expected = intValue(requiredOne("SELECT expected_ticket_count FROM ticket_source_issue_task WHERE order_id=?", orderId).get("expected_ticket_count"));
        if (delivery.expectedTicketCount() != expected) {
            markIssueManual(orderId, "SOURCE_TICKET_COUNT_MISMATCH", "第三方expectedTicketCount与本地订单票数不一致");
            throw new BusinessException("第三方出票总数与本地订单不一致");
        }
        String providerSkuId = str(ctx, "provider_sku_id");
        if (providerSkuId == null) throw new BusinessException("V1.2订单缺少唯一第三方票档ID");

        List<Map<String, Object>> locals = jdbc.queryForList("""
                SELECT oa.order_audience_id,oa.client_ticket_no,oa.holder_ref,et.ticket_id,et.ticket_status
                FROM order_audience oa JOIN electronic_ticket et ON et.order_audience_id=oa.order_audience_id
                WHERE oa.order_id=? ORDER BY oa.order_audience_id
                """, orderId);
        if (locals.size() != expected) {
            markIssueManual(orderId, "LOCAL_TICKET_COUNT_MISMATCH", "本地电子票/观演人数量与订单数量不一致");
            throw new BusinessException("本地电子票数量与订单票数不一致");
        }
        Map<String, Map<String, Object>> localByIdentity = new HashMap<>();
        for (Map<String, Object> row : locals) {
            String clientTicketNo = str(row, "client_ticket_no");
            String holderRef = str(row, "holder_ref");
            if (clientTicketNo == null || holderRef == null) {
                markIssueManual(orderId, "LOCAL_TICKET_IDENTITY_MISSING", "order_audience缺少clientTicketNo/holderRef；旧订单不能按数组下标兼容出票");
                throw new BusinessException("逐票身份键缺失，请重新创建V1.2订单后测试");
            }
            String key = identity(clientTicketNo, holderRef);
            if (localByIdentity.put(key, row) != null) throw new BusinessException("本地逐票身份键重复: " + key);
        }

        Set<String> providerTicketIds = new HashSet<>();
        Set<String> seenIdentity = new HashSet<>();
        for (ProviderTicketCredential ticket : safe(delivery.tickets())) {
            if (ticket == null) continue;
            if (!providerSkuId.equals(ticket.ticketProductId())) {
                markIssueManual(orderId, "SOURCE_MULTI_SKU_FULFILLMENT", "V1.2履约结果出现非本订单票档: " + ticket.ticketProductId());
                throw new BusinessException("第三方履约结果违反V1.2单票档约束");
            }
            String key = identity(ticket.clientTicketNo(), ticket.holderRef());
            if (!seenIdentity.add(key)) throw new BusinessException("第三方履约结果逐票身份重复: " + key);
            if (!localByIdentity.containsKey(key)) {
                markIssueManual(orderId, "SOURCE_TICKET_IDENTITY_MISMATCH", "第三方票无法按clientTicketNo+holderRef匹配本地: " + key);
                throw new BusinessException("第三方逐票身份与本地订单不一致");
            }
            if (!providerTicketIds.add(ticket.providerTicketId())) throw new BusinessException("第三方providerTicketId重复");
        }

        tx.executeWithoutResult(status -> {
            for (ProviderTicketCredential ticket : safe(delivery.tickets())) {
                if (ticket == null) continue;
                Map<String, Object> local = localByIdentity.get(identity(ticket.clientTicketNo(), ticket.holderRef()));
                updateLocalTicketFromProvider(orderId, ctx, local, ticket);
            }
            TicketDeliveryStatus ds = delivery.deliveryStatus().status();
            String taskStatus = switch (ds) {
                case SUCCESS -> delivery.issuedCount() == expected && delivery.failedCount() == 0 ? "SUCCESS" : "PARTIAL";
                case PARTIAL -> "PARTIAL";
                case FAILED -> "FAILED";
                case PENDING, PROCESSING -> "WAIT_PROVIDER";
            };
            LocalDateTime next = ("WAIT_PROVIDER".equals(taskStatus) || "PARTIAL".equals(taskStatus))
                    ? local(delivery.nextPollAt() == null ? OffsetDateTime.now(CN).plusSeconds(properties.getPollIntervalSeconds()) : delivery.nextPollAt())
                    : null;
            jdbc.update("""
                    UPDATE ticket_source_issue_task
                    SET task_status=?,provider_delivery_status=?,issued_count=?,failed_count=?,next_attempt_time=?,
                        complete_time=CASE WHEN ?='SUCCESS' THEN NOW() ELSE complete_time END,
                        provider_delivery_version=?,last_operation='GET_TICKETS',last_error_code=NULL,last_error_message=NULL,
                        last_error_retryable=0,version=version+1,update_time=NOW()
                    WHERE order_id=?
                    """, taskStatus, ds.name(), delivery.issuedCount(), delivery.failedCount(), next,
                    taskStatus, delivery.version(), orderId);
            if ("SUCCESS".equals(taskStatus)) {
                jdbc.update("UPDATE ticket_order SET ticket_issued_time=COALESCE(ticket_issued_time,NOW()),update_time=NOW() WHERE order_id=?", orderId);
                jdbc.update("UPDATE ticket_source_order_bridge SET provider_order_status='ISSUED',last_operation='GET_TICKETS',last_sync_status='SUCCESS',update_time=NOW() WHERE order_id=?", orderId);
            }
        });
    }

    private void updateLocalTicketFromProvider(Long orderId, Map<String, Object> ctx, Map<String, Object> local, ProviderTicketCredential t) {
        ProviderTicketStatus providerStatus = t.ticketStatus().status();
        String localStatus = switch (providerStatus) {
            case UNUSED -> "UNUSED";
            case USED -> "CHECKED";
            case VOIDED, EXPIRED -> "EXPIRED";
            case ERROR -> "ERROR";
            case GENERATING -> "GENERATING";
        };
        CredentialType type = t.credentialType();
        boolean dynamic = type == CredentialType.DYNAMIC_QR;
        String payload = dynamic ? null : t.credentialPayload();
        String qr = type == CredentialType.STATIC_QR ? payload : null;
        ProviderSeatAssignment seat = t.seat();
        String seatInfo = seat == null ? null : firstText(seat.fullText(), joinSeat(seat));
        String error = providerStatus == ProviderTicketStatus.ERROR ? firstText(t.errorMessage(), "第三方出票异常") : null;
        jdbc.update("""
                UPDATE electronic_ticket
                SET source_provider_id=?,provider_order_id=?,provider_ticket_id=?,provider_ticket_product_id=?,
                    ticket_status=?,credential_type=?,dynamic_qr_mode=?,credential_payload=?,credential_version=?,
                    credential_expire_time=?,qr_code_value=?,seat_info=?,seat_zone=?,seat_row=?,seat_number=?,entrance_info=?,
                    generate_time=CASE WHEN ?='UNUSED' THEN COALESCE(generate_time,NOW()) ELSE generate_time END,
                    provider_issue_time=?,last_source_sync_time=NOW(),
                    expire_time=CASE WHEN ?='EXPIRED' THEN COALESCE(expire_time,NOW()) ELSE expire_time END,
                    abnormal_reason=?,update_time=NOW()
                WHERE ticket_id=? AND order_id=?
                """,
                longValue(ctx.get("provider_id")), str(ctx, "provider_order_id"), t.providerTicketId(), t.ticketProductId(),
                localStatus, type.name(), t.dynamicQrMode() == null ? null : t.dynamicQrMode().name(), payload, t.credentialVersion(),
                local(t.expiresAt()), qr, seatInfo, seat == null ? null : seat.zone(), seat == null ? null : seat.row(),
                seat == null ? null : seat.seat(), seat == null ? null : seat.entrance(),
                localStatus, local(t.issuedAt()), localStatus, error, longValue(local.get("ticket_id")), orderId);
    }

    private void recordIssueFailure(Long orderId, RuntimeException ex) {
        Map<String, Object> task = one("SELECT retry_count,max_retry_count FROM ticket_source_issue_task WHERE order_id=?", orderId);
        if (task == null) return;
        int retry = intValue(task.get("retry_count")) + 1;
        int max = Math.max(1, intValue(task.get("max_retry_count")));
        boolean retryable = ex instanceof V11AdapterException a && a.isRetryable();
        String status = retryable && retry < max ? "RETRY_WAIT" : "MANUAL_REVIEW";
        LocalDateTime next = "RETRY_WAIT".equals(status) ? LocalDateTime.now().plusSeconds(backoff(retry)) : null;
        jdbc.update("""
                UPDATE ticket_source_issue_task SET task_status=?,retry_count=?,next_attempt_time=?,manual_hold=?,
                    last_error_code=?,last_error_message=?,last_error_retryable=?,version=version+1,update_time=NOW()
                WHERE order_id=?
                """, status, retry, next, "MANUAL_REVIEW".equals(status) ? 1 : 0,
                errorCode(ex), safeMessage(ex), retryable ? 1 : 0, orderId);
    }

    private void markIssueManual(Long orderId, String code, String message) {
        jdbc.update("UPDATE ticket_source_issue_task SET task_status='MANUAL_REVIEW',manual_hold=1,last_error_code=?,last_error_message=?,update_time=NOW() WHERE order_id=?",
                code, clip(message), orderId);
    }

    private Map<String, Object> issueTaskView(Long orderId) {
        return requiredOne("""
                SELECT t.task_id AS taskId,t.order_id AS orderId,t.task_status AS taskStatus,
                       t.provider_delivery_status AS providerDeliveryStatus,t.expected_ticket_count AS expectedTicketCount,
                       t.issued_count AS issuedCount,t.failed_count AS failedCount,t.retry_count AS retryCount,
                       t.request_sent AS requestSent,t.manual_hold AS manualHold,t.next_attempt_time AS nextAttemptTime,
                       t.last_error_code AS lastErrorCode,t.last_error_message AS lastErrorMessage,
                       o.order_status AS orderStatus,o.payment_status AS paymentStatus,
                       b.provider_order_id AS providerOrderId,p.provider_code AS providerCode
                FROM ticket_source_issue_task t
                JOIN ticket_order o ON o.order_id=t.order_id
                JOIN ticket_source_order_bridge b ON b.bridge_id=t.bridge_id
                JOIN ticket_source_provider p ON p.provider_id=t.provider_id
                WHERE t.order_id=?
                """, orderId);
    }

    // ---------------------------------------------------------------------
    // Dynamic QR
    // ---------------------------------------------------------------------

    public V12DynamicCredentialView refreshDynamicCredential(Long orderId, Long ticketId, Long userId) {
        requirePositive(orderId, "orderId"); requirePositive(ticketId, "ticketId"); requirePositive(userId, "userId");
        Map<String, Object> row = requiredOne("""
                SELECT et.ticket_id,et.order_id,et.provider_ticket_id,et.credential_type,et.dynamic_qr_mode,
                       et.credential_version,et.ticket_status,et.check_time,et.expire_time,
                       o.user_id,o.order_status,o.payment_status,b.provider_id,b.provider_order_id,p.provider_code,
                       r.refund_status
                FROM electronic_ticket et
                JOIN ticket_order o ON o.order_id=et.order_id
                JOIN ticket_source_order_bridge b ON b.order_id=o.order_id
                JOIN ticket_source_provider p ON p.provider_id=b.provider_id
                LEFT JOIN refund_record r ON r.order_id=o.order_id
                WHERE et.ticket_id=? AND et.order_id=? AND o.user_id=?
                """, ticketId, orderId, userId);
        if (!"WAIT_USE".equals(str(row, "order_status")) || !"PROVIDER_CONFIRMED".equals(str(row, "payment_status"))) {
            throw new BusinessException("订单当前不能刷新动态二维码");
        }
        if (!"UNUSED".equals(str(row, "ticket_status")) || row.get("check_time") != null) {
            throw new BusinessException("当前电子票状态不能刷新动态二维码");
        }
        String refundStatus = str(row, "refund_status");
        if ("REFUNDING".equals(refundStatus) || "REFUND_SUCCESS".equals(refundStatus)) {
            throw new BusinessException("退款中的电子票不能刷新动态二维码");
        }
        if (!"DYNAMIC_QR".equals(str(row, "credential_type"))) throw new BusinessException("该票不是动态二维码");
        if (!DynamicQrMode.REMOTE_REFRESH.name().equals(str(row, "dynamic_qr_mode"))) {
            throw new BusinessException("当前动态二维码模式不能由麦麦后台刷新");
        }
        String providerTicketId = str(row, "provider_ticket_id");
        if (providerTicketId == null) throw new BusinessException("第三方票ID尚未同步");

        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(str(row, "provider_code"));
        ProviderCapabilities caps = invoker.invoke(target, TicketSourceOperation.HEALTH,
                (adapter, callCtx) -> adapter.capabilities(callCtx));
        if (!caps.dynamicQr() || caps.dynamicQrMode() != DynamicQrMode.REMOTE_REFRESH) {
            throw new BusinessException("当前票源没有可用的REMOTE_REFRESH动态二维码能力");
        }
        ProviderDynamicCredential d;
        try {
            d = invoker.invoke(target, TicketSourceOperation.REFRESH_DYNAMIC_CREDENTIAL,
                    (adapter, callCtx) -> adapter.refreshDynamicCredential(callCtx,
                            providerTicketId, str(row, "credential_version")));
        } catch (RuntimeException ex) {
            // 失败时绝不能返回/恢复任何已过期旧码；本地也从未保存动态码明文。
            throw invoker.translate("刷新第三方动态二维码", ex);
        }
        if (d == null || d.credentialType() != CredentialType.DYNAMIC_QR || !providerTicketId.equals(d.providerTicketId())) {
            throw new BusinessException("第三方动态二维码响应无效");
        }
        if (d.expiresAt() == null || d.serverTime() == null || !d.expiresAt().isAfter(d.serverTime())) {
            throw new BusinessException("第三方返回的动态二维码已经过期");
        }
        int updated = jdbc.update("""
                UPDATE electronic_ticket et
                JOIN ticket_order o ON o.order_id=et.order_id
                SET et.credential_payload=NULL,et.qr_code_value=NULL,et.credential_version=?,et.credential_expire_time=?,
                    et.refresh_after_seconds=?,et.last_source_sync_time=NOW(),et.update_time=NOW()
                WHERE et.ticket_id=? AND et.order_id=? AND o.user_id=?
                  AND et.ticket_status='UNUSED' AND et.check_time IS NULL
                  AND o.order_status='WAIT_USE' AND o.payment_status='PROVIDER_CONFIRMED'
                  AND NOT EXISTS (SELECT 1 FROM refund_record r WHERE r.order_id=o.order_id AND r.refund_status IN ('REFUNDING','REFUND_SUCCESS'))
                """, d.credentialVersion(), local(d.expiresAt()), d.refreshAfterSeconds(), ticketId, orderId, userId);
        if (updated != 1) throw new BusinessException("动态二维码刷新期间订单或票状态已变化，本轮二维码不返回");
        return new V12DynamicCredentialView(ticketId, providerTicketId, d.credentialType().name(), d.credentialPayload(),
                d.credentialVersion(), d.issuedAt(), d.expiresAt(), d.refreshAfterSeconds(), d.serverTime());
    }

    // ---------------------------------------------------------------------
    // Refund
    // ---------------------------------------------------------------------

    public ProviderRefundQuote quoteRefund(Long orderId, Long userId) {
        Map<String, Object> ctx = requiredOrderContext(orderId, userId);
        if (!"WAIT_USE".equals(str(ctx, "order_status"))) throw new BusinessException("只有待使用订单可以申请整单退款");
        return quoteRefundForContext(ctx);
    }

    /**
     * 创建本地整单退款申请并暂时失效全部本地凭证。
     * 麦麦现有用户退款流程在这里停在 PENDING_REVIEW，等待后台管理员审核。
     */
    public Map<String, Object> prepareRefund(Long orderId, V12RefundRequest request) {
        if (request == null) throw new BusinessException("退款请求不能为空");
        requirePositive(request.userId(), "userId");
        Map<String, Object> ctx = requiredOrderContext(orderId, request.userId());
        if (!"WAIT_USE".equals(str(ctx, "order_status"))) {
            Map<String, Object> existing = one("SELECT refund_id FROM refund_record WHERE order_id=?", orderId);
            if (existing != null) return refundView(longValue(existing.get("refund_id")));
            throw new BusinessException("只有待使用订单可以申请整单退款");
        }
        if (one("SELECT refund_id FROM refund_record WHERE order_id=?", orderId) != null) throw new BusinessException("该订单已存在退款记录");
        ProviderRefundQuote quote = quoteRefundForContext(ctx);
        if (!quote.refundable()) throw new BusinessException(firstText(quote.unavailableReason(), "第三方判定当前订单不可退款"));
        String refundNo = generateRefundNo();
        String reasonCode = firstText(request.reasonCode(), "USER_REQUEST");
        String reason = firstText(request.reason(), "用户申请整单退款");
        BigDecimal refundAmount = quote.refundableAmount().toMajor();
        BigDecimal feeAmount = quote.serviceFee() == null ? BigDecimal.ZERO : quote.serviceFee().toMajor();

        tx.executeWithoutResult(status -> {
            jdbc.update("""
                    INSERT INTO refund_record
                    (refund_no,order_id,refund_rule_id,matched_stage_id,refund_type_snapshot,fee_rate_snapshot,apply_time,reason,
                     refund_amount,fee_amount,refund_status,refund_time,fail_reason,update_time)
                    VALUES (?, ?, NULL,NULL,'PROVIDER_FULL_ORDER',NULL,NOW(),?,?,?,'REFUNDING',NULL,NULL,NOW())
                    """, refundNo, orderId, reason, refundAmount, feeAmount);
            Long refundId = jdbc.queryForObject("SELECT refund_id FROM refund_record WHERE refund_no=?", Long.class, refundNo);
            if (refundId == null) throw new BusinessException("保存退款记录失败");
            int orderRows = jdbc.update("UPDATE ticket_order SET order_status='REFUNDING',update_time=NOW() WHERE order_id=? AND order_status='WAIT_USE'", orderId);
            if (orderRows != 1) throw new BusinessException("订单状态已变化，退款申请失败");
            jdbc.update("""
                    UPDATE electronic_ticket SET refund_hold_status=ticket_status,refund_hold_abnormal_reason=abnormal_reason,
                         ticket_status='EXPIRED',expire_time=NOW(),credential_payload=NULL,qr_code_value=NULL,
                         abnormal_reason='用户申请第三方整单退款，凭证暂时失效',update_time=NOW()
                    WHERE order_id=? AND refund_hold_status IS NULL AND ticket_status IN ('UNUSED','GENERATING','ERROR')
                    """, orderId);
            jdbc.update("""
                    INSERT INTO ticket_source_refund_bridge
                    (refund_id,order_id,order_bridge_id,provider_id,provider_order_id,bridge_status,provider_refund_status,
                     refund_amount,fee_amount,currency_code,request_idempotency_key,retry_count,max_retry_count,manual_hold,
                     provider_refund_quote_id,refundable_delivery_fee,non_refundable_delivery_fee,promotion_rollback_amount,
                     reason_code,last_operation,last_sync_status,create_time,update_time)
                    VALUES (?,?,?,?,?,'PENDING_REVIEW','NOT_REQUESTED',?,?, 'CNY',?,0,?,0,?,?,?,?,?,'PREPARE_REFUND','SUCCESS',NOW(),NOW())
                    """, refundId, orderId, longValue(ctx.get("bridge_id")), longValue(ctx.get("provider_id")), str(ctx, "provider_order_id"),
                    refundAmount, feeAmount, "REFUND:" + refundNo, properties.getMaxRetryCount(), quote.quoteId(),
                    major(quote.refundableDeliveryFee()), major(quote.nonRefundableDeliveryFee()), major(quote.promotionRollbackAmount()), reasonCode);
        });
        Long refundId = jdbc.queryForObject("SELECT refund_id FROM refund_record WHERE order_id=?", Long.class, orderId);
        if (refundId == null) throw new BusinessException("退款记录不存在");
        return refundView(refundId);
    }

    /** 统一 V1.2 直连接口：创建后立即提交第三方；麦麦现有用户接口使用 prepareRefund + 管理员审核。 */
    public Map<String, Object> requestRefund(Long orderId, V12RefundRequest request) {
        Map<String, Object> prepared = prepareRefund(orderId, request);
        return approvePreparedRefund(longValue(prepared.get("refundId")));
    }

    /** 管理员审核通过时重新按第三方最新退款试算提交 FULL_ORDER，避免使用已过期旧 quote。 */
    public Map<String, Object> approvePreparedRefund(Long refundId) {
        requirePositive(refundId, "refundId");
        Map<String, Object> bridge = requiredOne("""
                SELECT rb.*,r.refund_no,r.reason,o.order_status,o.payment_status,b.order_model,p.provider_code
                FROM ticket_source_refund_bridge rb
                JOIN refund_record r ON r.refund_id=rb.refund_id
                JOIN ticket_order o ON o.order_id=rb.order_id
                JOIN ticket_source_order_bridge b ON b.bridge_id=rb.order_bridge_id
                JOIN ticket_source_provider p ON p.provider_id=rb.provider_id
                WHERE rb.refund_id=?
                """, refundId);
        if (!"SINGLE_SKU".equals(str(bridge, "order_model"))) throw new BusinessException("该退款不是V1.2单票档退款");
        if ("SUCCESS".equals(str(bridge, "bridge_status"))) return refundView(refundId);
        if ("REJECTED".equals(str(bridge, "bridge_status"))) throw new BusinessException("该退款已被驳回");
        if (!"REFUNDING".equals(str(bridge, "order_status"))) throw new BusinessException("本地订单不处于退款中，不能发起第三方退款");
        if (str(bridge, "provider_refund_id") != null) return syncRefund(refundId);
        if (!Set.of("PENDING_REVIEW", "REQUESTING", "RETRY_WAIT", "MANUAL_REVIEW", "FAILED").contains(str(bridge, "bridge_status"))) {
            throw new BusinessException("当前退款桥接状态不允许提交第三方");
        }

        Long orderId = longValue(bridge.get("order_id"));
        Map<String, Object> ctx = requiredOrderContext(orderId, null);
        ProviderRefundQuote quote = quoteRefundForContext(ctx);
        if (!quote.refundable()) throw new BusinessException(firstText(quote.unavailableReason(), "第三方判定当前订单不可退款"));
        String refundNo = str(bridge, "refund_no");
        String reasonCode = firstText(str(bridge, "reason_code"), "USER_REQUEST");
        String reason = firstText(str(bridge, "reason"), "管理员审核通过第三方整单退款");
        BigDecimal refundAmount = quote.refundableAmount().toMajor();
        BigDecimal feeAmount = quote.serviceFee() == null ? BigDecimal.ZERO : quote.serviceFee().toMajor();
        jdbc.update("UPDATE refund_record SET refund_amount=?,fee_amount=?,update_time=NOW() WHERE refund_id=? AND refund_status='REFUNDING'",
                refundAmount, feeAmount, refundId);
        jdbc.update("""
                UPDATE ticket_source_refund_bridge
                SET bridge_status='REQUESTING',provider_refund_status='NOT_REQUESTED',refund_amount=?,fee_amount=?,
                    provider_refund_quote_id=?,refundable_delivery_fee=?,non_refundable_delivery_fee=?,promotion_rollback_amount=?,
                    last_operation='REQUEST_REFUND',last_sync_status='PENDING',manual_hold=0,next_attempt_time=NULL,
                    last_error_code=NULL,last_error_message=NULL,last_error_retryable=0,update_time=NOW()
                WHERE refund_id=?
                """, refundAmount, feeAmount, quote.quoteId(), major(quote.refundableDeliveryFee()),
                major(quote.nonRefundableDeliveryFee()), major(quote.promotionRollbackAmount()), refundId);

        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(str(ctx, "provider_code"));
        ProviderRefund providerRefund;
        try {
            providerRefund = invoker.invoke(target, TicketSourceOperation.REQUEST_REFUND,
                    (adapter, callCtx) -> adapter.requestRefund(callCtx, str(ctx, "provider_order_id"),
                            new ProviderRefundRequest(refundNo, RefundScope.FULL_ORDER, reasonCode, reason,
                                    quote.refundableAmount(), quote.quoteId(), str(bridge, "request_idempotency_key"))));
        } catch (RuntimeException ex) {
            recordRefundCallFailure(refundId, ex);
            throw invoker.translate("发起第三方整单退款", ex);
        }
        applyProviderRefund(refundId, ctx, providerRefund);
        return refundView(refundId);
    }

    /** 只标记第三方桥接驳回；本地订单和票的恢复由现有 AdminRefundService 在同一审核事务中完成。 */
    public void rejectPreparedRefundBeforeProvider(Long refundId) {
        requirePositive(refundId, "refundId");
        Map<String, Object> b = requiredOne("SELECT bridge_status,provider_refund_id FROM ticket_source_refund_bridge WHERE refund_id=?", refundId);
        if ("REJECTED".equals(str(b, "bridge_status"))) return;
        if (!"PENDING_REVIEW".equals(str(b, "bridge_status")) || str(b, "provider_refund_id") != null) {
            throw new BusinessException("第三方退款已发起，不能直接驳回，请先人工处理第三方退款");
        }
        int rows = jdbc.update("""
                UPDATE ticket_source_refund_bridge SET bridge_status='REJECTED',provider_refund_status='NOT_REQUESTED',manual_hold=0,
                    next_attempt_time=NULL,last_operation='REJECT_REFUND',last_sync_status='SUCCESS',last_error_code=NULL,last_error_message=NULL,
                    last_error_retryable=0,version=version+1,update_time=NOW()
                WHERE refund_id=? AND bridge_status='PENDING_REVIEW' AND provider_refund_id IS NULL
                """, refundId);
        if (rows != 1) throw new BusinessException("退款桥接状态已变化，请刷新后重试");
    }

    public boolean isSingleSkuProviderRefund(Long refundId) {
        if (refundId == null || refundId <= 0) return false;
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM ticket_source_refund_bridge rb
                JOIN ticket_source_order_bridge b ON b.bridge_id=rb.order_bridge_id
                WHERE rb.refund_id=? AND b.order_model='SINGLE_SKU'
                """, Integer.class, refundId);
        return count != null && count > 0;
    }

    private ProviderRefundQuote quoteRefundForContext(Map<String, Object> ctx) {
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(str(ctx, "provider_code"));
        ProviderCapabilities caps = invoker.invoke(target, TicketSourceOperation.HEALTH,
                (adapter, callCtx) -> adapter.capabilities(callCtx));
        if (!caps.refundQuote() || caps.refundScope() != RefundCapabilityScope.FULL_ORDER_ONLY) {
            throw new BusinessException("当前票源未开放V1.2整单退款试算");
        }
        ProviderRefundQuote quote;
        try {
            quote = invoker.invoke(target, TicketSourceOperation.QUOTE_REFUND,
                    (adapter, callCtx) -> adapter.quoteRefund(callCtx, str(ctx, "provider_order_id")));
        } catch (RuntimeException ex) {
            throw invoker.translate("第三方整单退款试算", ex);
        }
        validateRefundQuote(ctx, quote);
        return quote;
    }

    public Map<String, Object> syncRefund(Long refundId) {
        requirePositive(refundId, "refundId");
        Map<String, Object> bridge = requiredOne("""
                SELECT rb.*,p.provider_code,o.order_status,o.payment_status,ob.provider_sku_id,ob.sku_mapping_id
                FROM ticket_source_refund_bridge rb
                JOIN ticket_source_provider p ON p.provider_id=rb.provider_id
                JOIN ticket_order o ON o.order_id=rb.order_id
                JOIN ticket_source_order_bridge ob ON ob.bridge_id=rb.order_bridge_id
                WHERE rb.refund_id=?
                """, refundId);
        String providerRefundId = str(bridge, "provider_refund_id");
        if (providerRefundId == null) throw new BusinessException("第三方退款尚未成功提交，不能只查询状态");
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(str(bridge, "provider_code"));
        ProviderRefund providerRefund;
        try {
            providerRefund = invoker.invoke(target, TicketSourceOperation.GET_REFUND,
                    (adapter, callCtx) -> adapter.getRefund(callCtx, providerRefundId));
        } catch (RuntimeException ex) {
            recordRefundCallFailure(refundId, ex);
            throw invoker.translate("查询第三方退款", ex);
        }
        Map<String, Object> ctx = requiredOrderContext(longValue(bridge.get("order_id")), null);
        applyProviderRefund(refundId, ctx, providerRefund);
        return refundView(refundId);
    }

    public Map<String, Object> advanceRefund(Long refundId) {
        requirePositive(refundId, "refundId");
        Map<String, Object> b = requiredOne("SELECT provider_refund_id FROM ticket_source_refund_bridge WHERE refund_id=?", refundId);
        return str(b, "provider_refund_id") == null ? approvePreparedRefund(refundId) : syncRefund(refundId);
    }

    public V12BatchResult syncPendingRefunds(int requestedLimit) {
        int limit = Math.max(1, Math.min(100, requestedLimit));
        List<Long> ids = jdbc.queryForList("""
                SELECT refund_id FROM ticket_source_refund_bridge
                WHERE manual_hold=0
                  AND (
                       (bridge_status IN ('PROCESSING','RETRY_WAIT') AND (next_attempt_time IS NULL OR next_attempt_time<=NOW()))
                    OR (bridge_status='REQUESTING' AND provider_refund_id IS NULL AND update_time<=DATE_SUB(NOW(), INTERVAL 2 MINUTE))
                  )
                ORDER BY COALESCE(next_attempt_time,update_time),bridge_id LIMIT ?
                """, Long.class, limit);
        return runBatch(ids, this::advanceRefund);
    }

    private void validateRefundQuote(Map<String, Object> ctx, ProviderRefundQuote q) {
        if (q == null || q.refundScope() != RefundScope.FULL_ORDER) throw new BusinessException("第三方退款试算未返回FULL_ORDER");
        if (!Objects.equals(str(ctx, "provider_order_id"), q.providerOrderId())) throw new BusinessException("退款试算第三方订单ID不匹配");
        if (q.refundableAmount() == null || q.quoteId() == null || q.quoteId().isBlank()) throw new BusinessException("第三方退款试算缺少金额或quoteId");
        if (q.orderAmount() != null && q.orderAmount().amountMinor() != minor(decimal(ctx.get("pay_amount")))) {
            throw new BusinessException("第三方退款试算订单金额与本地实付金额不一致，需要对账");
        }
    }

    private void applyProviderRefund(Long refundId, Map<String, Object> ctx, ProviderRefund r) {
        if (r == null || r.refundStatus() == null) throw new BusinessException("第三方退款响应为空");
        ProviderRefundStatus status = r.refundStatus().status();
        if (!str(ctx, "provider_order_id").equals(r.providerOrderId())) throw new BusinessException("第三方退款订单ID不匹配");
        if (status == ProviderRefundStatus.SUCCESS) {
            ProviderInventory inventory = null;
            try {
                V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(str(ctx, "provider_code"));
                inventory = invoker.invoke(target, TicketSourceOperation.QUERY_INVENTORY,
                        (adapter, callCtx) -> adapter.queryInventory(callCtx, str(ctx, "provider_sku_id")));
            } catch (RuntimeException ignored) {
                // 退款事实可以先落库；库存查询失败由后续资源同步/对账补偿，但不得自行加库存。
            }
            ProviderInventory finalInventory = inventory;
            tx.executeWithoutResult(txStatus -> {
                LocalDateTime refundTime = local(r.refundedAt() == null ? OffsetDateTime.now(CN) : r.refundedAt());
                jdbc.update("""
                        UPDATE ticket_source_refund_bridge SET provider_refund_id=?,provider_refund_no=?,bridge_status='SUCCESS',
                            provider_refund_status='SUCCESS',fee_amount=?,provider_refund_time=?,next_attempt_time=NULL,
                            provider_refund_version=?,last_operation='GET_REFUND',last_sync_status='SUCCESS',
                            last_error_code=NULL,last_error_message=NULL,last_error_retryable=0,response_snapshot=?,update_time=NOW()
                        WHERE refund_id=?
                        """, r.providerRefundId(), r.providerRefundNo(), major(r.feeAmount()), refundTime,
                        r.version(), json(r), refundId);
                jdbc.update("UPDATE refund_record SET refund_status='REFUND_SUCCESS',refund_amount=?,fee_amount=?,refund_time=?,fail_reason=NULL,update_time=NOW() WHERE refund_id=?",
                        major(r.refundAmount()), major(r.feeAmount()), refundTime, refundId);
                jdbc.update("UPDATE ticket_order SET order_status='REFUND_SUCCESS',payment_status='REFUNDED',update_time=NOW() WHERE order_id=?",
                        longValue(ctx.get("order_id")));
                jdbc.update("""
                        UPDATE electronic_ticket SET ticket_status='EXPIRED',credential_payload=NULL,qr_code_value=NULL,
                            credential_expire_time=NULL,refresh_after_seconds=NULL,expire_time=COALESCE(expire_time,NOW()),
                            abnormal_reason='第三方整单退款成功，票已作废',refund_hold_status=NULL,refund_hold_abnormal_reason=NULL,
                            last_source_sync_time=NOW(),update_time=NOW() WHERE order_id=?
                        """, longValue(ctx.get("order_id")));
                jdbc.update("UPDATE ticket_source_order_bridge SET bridge_status='REFUNDED',provider_order_status='REFUNDED',last_operation='GET_REFUND',last_sync_status='SUCCESS',update_time=NOW() WHERE order_id=?",
                        longValue(ctx.get("order_id")));
                if (finalInventory != null && finalInventory.availableStock() != null) {
                    jdbc.update("UPDATE ticket_source_sku_mapping SET available_stock_snapshot=?,source_sale_status=?,last_inventory_sync_time=NOW(),last_sync_status='SUCCESS',update_time=NOW() WHERE mapping_id=?",
                            finalInventory.availableStock(), finalInventory.saleStatus().status().name(), longValue(ctx.get("sku_mapping_id")));
                    jdbc.update("UPDATE ticket_sku SET stock_available=?,sku_status=CASE WHEN ?=0 THEN 'SOLD_OUT' ELSE 'ON_SALE' END,update_time=NOW() WHERE sku_id=(SELECT sku_id FROM ticket_source_sku_mapping WHERE mapping_id=?)",
                            finalInventory.availableStock(), finalInventory.availableStock(), longValue(ctx.get("sku_mapping_id")));
                }
            });
            try { shipmentService.onRefundSuccess(longValue(ctx.get("order_id"))); } catch (RuntimeException ignored) { }
            return;
        }
        if (status == ProviderRefundStatus.SUBMITTED || status == ProviderRefundStatus.PROCESSING) {
            LocalDateTime next = local(r.nextPollAt() == null ? OffsetDateTime.now(CN).plusSeconds(properties.getPollIntervalSeconds()) : r.nextPollAt());
            jdbc.update("""
                    UPDATE ticket_source_refund_bridge SET provider_refund_id=?,provider_refund_no=?,bridge_status='PROCESSING',
                        provider_refund_status=?,provider_request_time=COALESCE(provider_request_time,NOW()),next_attempt_time=?,
                        provider_refund_version=?,last_operation='GET_REFUND',last_sync_status='SUCCESS',response_snapshot=?,update_time=NOW()
                    WHERE refund_id=?
                    """, r.providerRefundId(), r.providerRefundNo(), status.name(), next, r.version(), json(r), refundId);
            return;
        }
        // 明确拒绝/失败/取消：恢复本地凭证，不把第三方失败误当退款成功。
        tx.executeWithoutResult(txStatus -> {
            jdbc.update("""
                    UPDATE ticket_source_refund_bridge SET provider_refund_id=?,provider_refund_no=?,bridge_status='REJECTED',
                        provider_refund_status=?,next_attempt_time=NULL,provider_refund_version=?,last_operation='GET_REFUND',
                        last_sync_status='SUCCESS',last_error_code=?,last_error_message=?,response_snapshot=?,update_time=NOW()
                    WHERE refund_id=?
                    """, r.providerRefundId(), r.providerRefundNo(), status.name(), r.version(), r.errorCode(), r.errorMessage(), json(r), refundId);
            jdbc.update("UPDATE refund_record SET refund_status='REFUND_FAILED',fail_reason=?,update_time=NOW() WHERE refund_id=?",
                    firstText(r.errorMessage(), "第三方退款未成功"), refundId);
            jdbc.update("UPDATE ticket_order SET order_status='WAIT_USE',update_time=NOW() WHERE order_id=? AND order_status='REFUNDING'", longValue(ctx.get("order_id")));
            jdbc.update("""
                    UPDATE electronic_ticket SET ticket_status=COALESCE(refund_hold_status,'UNUSED'),
                        abnormal_reason=refund_hold_abnormal_reason,refund_hold_status=NULL,refund_hold_abnormal_reason=NULL,
                        expire_time=CASE WHEN COALESCE(refund_hold_status,'UNUSED')='EXPIRED' THEN expire_time ELSE NULL END,
                        update_time=NOW() WHERE order_id=? AND refund_hold_status IS NOT NULL
                    """, longValue(ctx.get("order_id")));
        });
    }

    private void recordRefundCallFailure(Long refundId, RuntimeException ex) {
        Map<String, Object> b = one("SELECT retry_count,max_retry_count FROM ticket_source_refund_bridge WHERE refund_id=?", refundId);
        if (b == null) return;
        int retry = intValue(b.get("retry_count")) + 1;
        int max = Math.max(1, intValue(b.get("max_retry_count")));
        boolean retryable = ex instanceof V11AdapterException a && a.isRetryable();
        boolean manual = !retryable || retry >= max;
        jdbc.update("""
                UPDATE ticket_source_refund_bridge SET bridge_status=?,retry_count=?,manual_hold=?,next_attempt_time=?,
                     last_sync_status='FAILED',last_error_code=?,last_error_message=?,last_error_retryable=?,update_time=NOW()
                WHERE refund_id=?
                """, manual ? "MANUAL_REVIEW" : "RETRY_WAIT", retry, manual ? 1 : 0,
                manual ? null : LocalDateTime.now().plusSeconds(backoff(retry)), errorCode(ex), safeMessage(ex), retryable ? 1 : 0, refundId);
    }

    public Map<String, Object> getRefund(Long refundId, Long userId) {
        requirePositive(refundId, "refundId");
        requirePositive(userId, "userId");
        Map<String, Object> owner = one("SELECT order_id FROM refund_record WHERE refund_id=?", refundId);
        if (owner == null) throw new BusinessException("退款记录不存在");
        requiredOrderContext(longValue(owner.get("order_id")), userId);
        return refundView(refundId);
    }

    public Map<String, Object> refundView(Long refundId) {
        return requiredOne("""
                SELECT r.refund_id AS refundId,r.refund_no AS refundNo,r.order_id AS orderId,r.refund_status AS refundStatus,
                       r.refund_amount AS refundAmount,r.fee_amount AS feeAmount,r.refund_time AS refundTime,r.fail_reason AS failReason,
                       rb.bridge_status AS bridgeStatus,rb.provider_refund_status AS providerRefundStatus,
                       rb.provider_refund_id AS providerRefundId,rb.provider_refund_quote_id AS quoteId,
                       rb.refundable_delivery_fee AS refundableDeliveryFee,rb.non_refundable_delivery_fee AS nonRefundableDeliveryFee,
                       rb.promotion_rollback_amount AS promotionRollbackAmount,rb.next_attempt_time AS nextAttemptTime,
                       rb.last_error_code AS lastErrorCode,rb.last_error_message AS lastErrorMessage
                FROM refund_record r JOIN ticket_source_refund_bridge rb ON rb.refund_id=r.refund_id
                WHERE r.refund_id=?
                """, refundId);
    }

    // ---------------------------------------------------------------------
    // Callback inbox: receive first, query provider during processing
    // ---------------------------------------------------------------------

    public V12CallbackAck receiveCallback(String pathProviderCode,
                                          ProviderCallbackEvent event,
                                          String timestampHeader,
                                          String nonce,
                                          String signature) {
        if (event == null) throw new BusinessException("回调事件不能为空");
        String providerCode = requiredText(pathProviderCode, "providerCode").toUpperCase(Locale.ROOT);
        if (!providerCode.equals(event.providerCode().name())) throw new BusinessException("回调路径providerCode与事件体不一致");
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(providerCode);
        ProviderCapabilities caps = invoker.invoke(target, TicketSourceOperation.HEALTH,
                (adapter, callCtx) -> adapter.capabilities(callCtx));
        if (!callbackEnabled(caps.callbacks(), event.eventType())) throw new BusinessException("当前票源未声明该回调能力: " + event.eventType());

        boolean signatureRequired = !"MOCK_DAMAI".equals(providerCode) || properties.isLocalMockCallbackSignatureRequired();
        boolean signatureValid = !signatureRequired || verifyCallbackSignature(event, timestampHeader, nonce, signature);
        if (!signatureValid) throw new BusinessException(401, "第三方回调签名无效");
        if (signatureRequired) validateTimestamp(timestampHeader);
        if (nonce == null || nonce.isBlank()) throw new BusinessException("X-Nonce不能为空");

        Map<String, Object> existing = one("SELECT process_status FROM ticket_source_callback_event WHERE provider_id=? AND provider_event_id=?",
                target.provider().getProviderId(), event.eventId());
        if (existing != null) return new V12CallbackAck(event.eventId(), true, true, str(existing, "process_status"));
        if (one("SELECT event_id FROM ticket_source_callback_event WHERE provider_id=? AND request_nonce=?", target.provider().getProviderId(), nonce) != null) {
            throw new BusinessException(409, "回调nonce重复，疑似重放");
        }
        jdbc.update("""
                INSERT INTO ticket_source_callback_event
                (provider_id,provider_event_id,event_type,resource_type,provider_resource_id,event_version,occurred_time,
                 process_status,request_timestamp,request_nonce,signature_valid,retry_count,next_attempt_time,payload_snapshot,
                 create_time,update_time)
                VALUES (?,?,?,?,?,?,?,'PENDING',?,?,?,0,NOW(),?,NOW(),NOW())
                """, target.provider().getProviderId(), event.eventId(), event.eventType().name(), event.resourceType(),
                event.providerResourceId(), event.version(), local(event.occurredAt()), parseTimestamp(timestampHeader), nonce,
                signatureValid ? 1 : 0, json(event));
        return new V12CallbackAck(event.eventId(), true, false, "PENDING");
    }

    public V12BatchResult processPendingCallbacks(int requestedLimit) {
        int limit = Math.max(1, Math.min(100, requestedLimit));
        List<Long> ids = jdbc.queryForList("""
                SELECT event_id FROM ticket_source_callback_event
                WHERE process_status IN ('PENDING','FAILED') AND (next_attempt_time IS NULL OR next_attempt_time<=NOW())
                ORDER BY event_id LIMIT ?
                """, Long.class, limit);
        return runBatch(ids, id -> { processCallback(id); return null; });
    }

    public Map<String, Object> processCallback(Long eventId) {
        Map<String, Object> e = requiredOne("""
                SELECT ce.*,p.provider_code FROM ticket_source_callback_event ce
                JOIN ticket_source_provider p ON p.provider_id=ce.provider_id WHERE ce.event_id=?
                """, eventId);
        if ("SUCCESS".equals(str(e, "process_status"))) return callbackView(eventId);
        jdbc.update("UPDATE ticket_source_callback_event SET process_status='PROCESSING',update_time=NOW() WHERE event_id=?", eventId);
        try {
            CallbackEventType type = CallbackEventType.valueOf(str(e, "event_type"));
            String providerCode = str(e, "provider_code");
            String resourceId = str(e, "provider_resource_id");
            switch (type) {
                case TICKET_ISSUED, ORDER_CHANGED -> {
                    Long orderId = providerOrderToLocal(providerCode, resourceId);
                    if (orderId == null) throw new BusinessException("回调对应的本地订单不存在: " + resourceId);
                    // 回调不直接修改票；processFulfillment 内部重新 GET /tickets。
                    processFulfillment(orderId);
                }
                case TICKET_VOIDED -> {
                    Long orderId = providerOrderToLocal(providerCode, resourceId);
                    if (orderId == null) throw new BusinessException("票作废回调对应订单不存在");
                    queryAndApplyVoidedTickets(orderId);
                }
                case REFUND_CHANGED -> {
                    Long refundId = providerRefundToLocal(providerCode, resourceId);
                    if (refundId == null) throw new BusinessException("退款回调对应本地退款不存在: " + resourceId);
                    syncRefund(refundId);
                }
                case SHIPMENT_CHANGED -> {
                    Long orderId = providerOrderToLocal(providerCode, resourceId);
                    if (orderId == null) throw new BusinessException("物流回调对应本地订单不存在");
                    shipmentService.syncAdmin(orderId); // 主动查询第三方物流事实
                }
                case PROJECT_CHANGED -> resourceSyncService.syncProject(providerCode, resourceId, syncRequest(true, true, false));
                case SESSION_CHANGED, TICKET_PRODUCT_CHANGED, INVENTORY_CHANGED -> {
                    String projectId = findProviderProjectForResource(providerCode, type, resourceId);
                    if (projectId == null) throw new BusinessException("资源回调无法定位所属项目: " + resourceId);
                    resourceSyncService.syncProject(providerCode, projectId, syncRequest(true, true, false));
                }
                case PROMOTION_CHANGED -> {
                    String projectId = findProviderProjectForResource(providerCode, type, resourceId);
                    if (projectId != null) resourceSyncService.syncProject(providerCode, projectId, syncRequest(true, false, false));
                }
                case CAMPAIGN_ASSET_CHANGED -> resourceSyncService.syncCampaignAssets(providerCode, null);
            }
            jdbc.update("UPDATE ticket_source_callback_event SET process_status='SUCCESS',processed_time=NOW(),last_error_code=NULL,last_error_message=NULL,update_time=NOW() WHERE event_id=?", eventId);
        } catch (RuntimeException ex) {
            int retry = intValue(e.get("retry_count")) + 1;
            jdbc.update("""
                    UPDATE ticket_source_callback_event SET process_status='FAILED',retry_count=?,next_attempt_time=?,
                         last_error_code=?,last_error_message=?,update_time=NOW() WHERE event_id=?
                    """, retry, LocalDateTime.now().plusSeconds(backoff(retry)), errorCode(ex), safeMessage(ex), eventId);
            throw ex;
        }
        return callbackView(eventId);
    }

    private void queryAndApplyVoidedTickets(Long orderId) {
        Map<String, Object> ctx = requiredOrderContext(orderId, null);
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(str(ctx, "provider_code"));
        ProviderTicketDelivery delivery = invoker.invoke(target, TicketSourceOperation.GET_TICKETS,
                (adapter, callCtx) -> adapter.getTickets(callCtx, str(ctx, "provider_order_id")));
        String providerSku = str(ctx, "provider_sku_id");
        for (ProviderTicketCredential t : safe(delivery.tickets())) {
            if (t == null || !providerSku.equals(t.ticketProductId())) throw new BusinessException("票作废补查返回非本订单票档");
            if (t.ticketStatus().status() == ProviderTicketStatus.VOIDED || t.ticketStatus().status() == ProviderTicketStatus.EXPIRED) {
                jdbc.update("""
                        UPDATE electronic_ticket et JOIN order_audience oa ON oa.order_audience_id=et.order_audience_id
                        SET et.ticket_status='EXPIRED',et.credential_payload=NULL,et.qr_code_value=NULL,et.credential_expire_time=NULL,
                            et.abnormal_reason='第三方通知票已作废并经主动查询确认',et.last_source_sync_time=NOW(),et.update_time=NOW()
                        WHERE et.order_id=? AND oa.client_ticket_no=? AND oa.holder_ref=?
                        """, orderId, t.clientTicketNo(), t.holderRef());
            }
        }
    }

    public Map<String, Object> callbackView(Long eventId) {
        return requiredOne("""
                SELECT event_id AS eventId,provider_event_id AS providerEventId,event_type AS eventType,resource_type AS resourceType,
                       provider_resource_id AS providerResourceId,event_version AS eventVersion,process_status AS processStatus,
                       retry_count AS retryCount,signature_valid AS signatureValid,processed_time AS processedTime,
                       last_error_code AS lastErrorCode,last_error_message AS lastErrorMessage
                FROM ticket_source_callback_event WHERE event_id=?
                """, eventId);
    }

    // ---------------------------------------------------------------------
    // Reconciliation
    // ---------------------------------------------------------------------

    public Map<String, Object> reconcile(String providerCode, List<Long> requestedOrderIds) {
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(providerCode);
        List<Long> orderIds = requestedOrderIds == null ? List.of() : requestedOrderIds.stream().filter(Objects::nonNull).distinct().toList();
        if (orderIds.isEmpty()) throw new BusinessException("对账至少指定一个orderId，避免误扫全库");
        String batchNo = "RCV12" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ThreadLocalRandom.current().nextInt(1000, 9999);
        jdbc.update("""
                INSERT INTO ticket_source_reconciliation_batch
                (batch_no,provider_id,provider_code,batch_status,total_count,matched_count,difference_count,error_count,start_time,create_time,update_time)
                VALUES (?,?,?,'PROCESSING',?,0,0,0,NOW(),NOW(),NOW())
                """, batchNo, target.provider().getProviderId(), target.provider().getProviderCode(), orderIds.size());
        Long batchId = jdbc.queryForObject("SELECT batch_id FROM ticket_source_reconciliation_batch WHERE batch_no=?", Long.class, batchNo);
        int matched = 0, difference = 0, errors = 0;
        for (Long orderId : orderIds) {
            try {
                if (reconcileOne(batchId, target, orderId)) matched++; else difference++;
            } catch (RuntimeException ex) {
                errors++;
                insertReconcileError(batchId, orderId, ex);
            }
        }
        String status = errors == orderIds.size() ? "FAILED" : (errors > 0 || difference > 0 ? "PARTIAL_FAILED" : "SUCCESS");
        jdbc.update("UPDATE ticket_source_reconciliation_batch SET batch_status=?,matched_count=?,difference_count=?,error_count=?,finish_time=NOW(),update_time=NOW() WHERE batch_id=?",
                status, matched, difference, errors, batchId);
        return requiredOne("""
                SELECT batch_id AS batchId,batch_no AS batchNo,provider_code AS providerCode,batch_status AS batchStatus,
                       total_count AS totalCount,matched_count AS matchedCount,difference_count AS differenceCount,error_count AS errorCount,
                       start_time AS startTime,finish_time AS finishTime FROM ticket_source_reconciliation_batch WHERE batch_id=?
                """, batchId);
    }

    private boolean reconcileOne(Long batchId, V11ResourceAdapterInvoker.Target target, Long orderId) {
        Map<String, Object> ctx = requiredOrderContext(orderId, null);
        if (!target.provider().getProviderId().equals(longValue(ctx.get("provider_id")))) throw new BusinessException("订单不属于本次票源");
        ProviderOrder po = invoker.invoke(target, TicketSourceOperation.GET_ORDER,
                (adapter, callCtx) -> adapter.getOrder(callCtx, str(ctx, "provider_order_id")));
        ProviderTicketDelivery tickets = invoker.invoke(target, TicketSourceOperation.GET_TICKETS,
                (adapter, callCtx) -> adapter.getTickets(callCtx, str(ctx, "provider_order_id")));
        Map<String, Object> local = requiredOne("""
                SELECT o.order_id,o.order_no,o.order_status,o.pay_amount,
                       (SELECT COUNT(*) FROM order_item oi WHERE oi.order_id=o.order_id) item_count,
                       (SELECT COUNT(*) FROM ticket_source_order_item_bridge ib WHERE ib.bridge_id=b.bridge_id) bridge_item_count,
                       (SELECT COUNT(*) FROM order_audience oa WHERE oa.order_id=o.order_id) audience_count,
                       (SELECT COUNT(*) FROM electronic_ticket et WHERE et.order_id=o.order_id) ticket_total,
                       (SELECT COUNT(*) FROM electronic_ticket et WHERE et.order_id=o.order_id AND et.ticket_status IN ('UNUSED','CHECKED')) valid_ticket_count,
                       b.provider_order_id,b.provider_sku_id,
                       r.refund_status,r.refund_amount,rb.provider_refund_status,rb.provider_refund_id
                FROM ticket_order o JOIN ticket_source_order_bridge b ON b.order_id=o.order_id
                LEFT JOIN refund_record r ON r.order_id=o.order_id
                LEFT JOIN ticket_source_refund_bridge rb ON rb.refund_id=r.refund_id
                WHERE o.order_id=?
                """, orderId);
        List<String> diffs = new ArrayList<>();
        if (intValue(local.get("item_count")) != 1) diffs.add("LOCAL_ORDER_ITEM_COUNT");
        if (intValue(local.get("bridge_item_count")) != 1) diffs.add("LOCAL_BRIDGE_ITEM_COUNT");
        if (intValue(local.get("audience_count")) != intValue(local.get("ticket_total"))) diffs.add("LOCAL_AUDIENCE_TICKET_COUNT");
        if (po.price() != null && po.price().payAmount() != null && minor(decimal(local.get("pay_amount"))) != po.price().payAmount().amountMinor()) diffs.add("PAY_AMOUNT");
        if (tickets.expectedTicketCount() != intValue(local.get("audience_count"))) diffs.add("PROVIDER_EXPECTED_TICKET_COUNT");
        Set<String> skuSet = safe(tickets.tickets()).stream().filter(Objects::nonNull).map(ProviderTicketCredential::ticketProductId).collect(Collectors.toSet());
        if (!skuSet.isEmpty() && (skuSet.size() != 1 || !skuSet.contains(str(local, "provider_sku_id")))) diffs.add("PROVIDER_MULTI_SKU");
        if (!providerTicketIdentityMatches(orderId, tickets.tickets())) diffs.add("TICKET_IDENTITY");

        ProviderRefund pr = null;
        String providerRefundId = str(local, "provider_refund_id");
        if (providerRefundId != null) {
            pr = invoker.invoke(target, TicketSourceOperation.GET_REFUND,
                    (adapter, callCtx) -> adapter.getRefund(callCtx, providerRefundId));
            if ("REFUND_SUCCESS".equals(str(local, "refund_status")) && pr.refundStatus().status() != ProviderRefundStatus.SUCCESS) diffs.add("REFUND_STATUS");
            if (pr.refundAmount() != null && decimal(local.get("refund_amount")) != null
                    && pr.refundAmount().amountMinor() != minor(decimal(local.get("refund_amount")))) diffs.add("REFUND_AMOUNT");
        }
        int providerValid = (int) safe(tickets.tickets()).stream().filter(Objects::nonNull)
                .filter(t -> t.ticketStatus().status() == ProviderTicketStatus.UNUSED || t.ticketStatus().status() == ProviderTicketStatus.USED).count();
        boolean match = diffs.isEmpty();
        jdbc.update("""
                INSERT INTO ticket_source_reconciliation_detail
                (batch_id,order_id,order_no,provider_order_id,compare_status,difference_types,local_order_status,provider_order_status,
                 local_pay_amount,provider_pay_amount,local_refund_status,provider_refund_status,local_refund_amount,provider_refund_amount,
                 local_valid_ticket_count,provider_valid_ticket_count,local_ticket_total,provider_ticket_total,snapshot_text,create_time)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())
                """, batchId, orderId, str(local, "order_no"), str(local, "provider_order_id"), match ? "MATCH" : "DIFFERENCE",
                diffs.isEmpty() ? null : String.join(",", diffs), str(local, "order_status"), po.orderStatus().status().name(),
                decimal(local.get("pay_amount")), po.price() == null || po.price().payAmount() == null ? null : po.price().payAmount().toMajor(),
                str(local, "refund_status"), pr == null ? null : pr.refundStatus().status().name(), decimal(local.get("refund_amount")),
                pr == null || pr.refundAmount() == null ? null : pr.refundAmount().toMajor(),
                intValue(local.get("valid_ticket_count")), providerValid, intValue(local.get("ticket_total")), tickets.tickets().size(),
                "singleSku=" + str(local, "provider_sku_id") + ";identityMatch=" + providerTicketIdentityMatches(orderId, tickets.tickets()));
        return match;
    }

    private boolean providerTicketIdentityMatches(Long orderId, List<ProviderTicketCredential> tickets) {
        Set<String> local = jdbc.queryForList("SELECT client_ticket_no,holder_ref FROM order_audience WHERE order_id=?", orderId).stream()
                .map(r -> identity(str(r, "client_ticket_no"), str(r, "holder_ref"))).collect(Collectors.toSet());
        Set<String> remote = safe(tickets).stream().filter(Objects::nonNull)
                .map(t -> identity(t.clientTicketNo(), t.holderRef())).collect(Collectors.toSet());
        return local.equals(remote);
    }

    private void insertReconcileError(Long batchId, Long orderId, RuntimeException ex) {
        Map<String, Object> row = one("""
                SELECT o.order_no,b.provider_order_id,o.order_status,o.pay_amount FROM ticket_order o
                LEFT JOIN ticket_source_order_bridge b ON b.order_id=o.order_id WHERE o.order_id=?
                """, orderId);
        if (row == null) return;
        jdbc.update("""
                INSERT INTO ticket_source_reconciliation_detail
                (batch_id,order_id,order_no,provider_order_id,compare_status,difference_types,local_order_status,local_pay_amount,
                 local_valid_ticket_count,provider_valid_ticket_count,local_ticket_total,provider_ticket_total,error_code,error_message,create_time)
                VALUES (?,?,?,?, 'ERROR','PROVIDER_QUERY_ERROR',?,?,0,0,0,0,?,?,NOW())
                """, batchId, orderId, str(row, "order_no"), firstText(str(row, "provider_order_id"), "UNKNOWN"),
                str(row, "order_status"), decimal(row.get("pay_amount")), errorCode(ex), safeMessage(ex));
    }

    // ---------------------------------------------------------------------
    // Shared helpers
    // ---------------------------------------------------------------------

    private Map<String, Object> requiredOrderContext(Long orderId, Long userId) {
        requirePositive(orderId, "orderId");
        String sql = """
                SELECT o.order_id,o.order_no,o.user_id,o.order_status,o.payment_status,o.fulfillment_mode,o.pay_amount,o.delivery_type,
                       b.bridge_id,b.provider_id,b.provider_order_id,b.provider_order_status,b.order_model,b.provider_sku_id,b.sku_mapping_id,
                       p.provider_code,ib.provider_sku_id AS item_provider_sku_id,ib.quantity AS item_quantity,ib.sku_mapping_id AS item_sku_mapping_id
                FROM ticket_order o
                JOIN ticket_source_order_bridge b ON b.order_id=o.order_id
                JOIN ticket_source_provider p ON p.provider_id=b.provider_id
                LEFT JOIN ticket_source_order_item_bridge ib ON ib.bridge_id=b.bridge_id
                WHERE o.order_id=?
                """ + (userId == null ? "" : " AND o.user_id=?");
        Map<String, Object> row = userId == null ? one(sql, orderId) : one(sql, orderId, userId);
        if (row == null) throw new BusinessException(userId == null ? "V1.2订单不存在" : "订单不存在或不属于当前用户");
        if (!"TICKET_SOURCE".equals(str(row, "fulfillment_mode"))) throw new BusinessException("该订单不是第三方票源履约订单");
        if (!"SINGLE_SKU".equals(str(row, "order_model"))) throw new BusinessException("该订单不是V1.2单票档订单");
        if (jdbc.queryForObject("SELECT COUNT(*) FROM ticket_source_order_item_bridge WHERE bridge_id=?", Integer.class, longValue(row.get("bridge_id"))) != 1) {
            throw new BusinessException("V1.2订单必须且只能有一条第三方订单项桥接");
        }
        return row;
    }

    private void requireFulfillableLocalOrder(Map<String, Object> ctx) {
        if (!"WAIT_USE".equals(str(ctx, "order_status")) || !"PROVIDER_CONFIRMED".equals(str(ctx, "payment_status"))) {
            throw new BusinessException("本地订单未处于第三方已确认支付的待使用状态");
        }
        if (str(ctx, "provider_order_id") == null) throw new BusinessException("第三方订单ID缺失");
    }

    private Long providerOrderToLocal(String providerCode, String providerOrderId) {
        return queryLong("""
                SELECT b.order_id FROM ticket_source_order_bridge b JOIN ticket_source_provider p ON p.provider_id=b.provider_id
                WHERE p.provider_code=? AND b.provider_order_id=? LIMIT 1
                """, providerCode, providerOrderId);
    }

    private Long providerRefundToLocal(String providerCode, String providerRefundId) {
        return queryLong("""
                SELECT rb.refund_id FROM ticket_source_refund_bridge rb JOIN ticket_source_provider p ON p.provider_id=rb.provider_id
                WHERE p.provider_code=? AND rb.provider_refund_id=? LIMIT 1
                """, providerCode, providerRefundId);
    }

    private String findProviderProjectForResource(String providerCode, CallbackEventType type, String resourceId) {
        if (type == CallbackEventType.PROJECT_CHANGED || type == CallbackEventType.PROMOTION_CHANGED) return resourceId;
        if (type == CallbackEventType.SESSION_CHANGED) {
            Map<String, Object> r = one("""
                    SELECT pm.provider_project_id FROM ticket_source_session_mapping sm
                    JOIN ticket_source_project_mapping pm ON pm.mapping_id=sm.project_mapping_id
                    JOIN ticket_source_provider p ON p.provider_id=pm.provider_id
                    WHERE p.provider_code=? AND sm.provider_session_id=? LIMIT 1
                    """, providerCode, resourceId);
            return r == null ? null : str(r, "provider_project_id");
        }
        if (type == CallbackEventType.TICKET_PRODUCT_CHANGED || type == CallbackEventType.INVENTORY_CHANGED) {
            Map<String, Object> r = one("""
                    SELECT pm.provider_project_id FROM ticket_source_sku_mapping skm
                    JOIN ticket_source_session_mapping sm ON sm.mapping_id=skm.session_mapping_id
                    JOIN ticket_source_project_mapping pm ON pm.mapping_id=sm.project_mapping_id
                    JOIN ticket_source_provider p ON p.provider_id=pm.provider_id
                    WHERE p.provider_code=? AND skm.provider_sku_id=? LIMIT 1
                    """, providerCode, resourceId);
            return r == null ? null : str(r, "provider_project_id");
        }
        return null;
    }

    private boolean callbackEnabled(CallbackCapabilities c, CallbackEventType t) {
        if (c == null) return false;
        return switch (t) {
            case PROJECT_CHANGED, SESSION_CHANGED, TICKET_PRODUCT_CHANGED, PROMOTION_CHANGED, CAMPAIGN_ASSET_CHANGED -> c.resource();
            case INVENTORY_CHANGED -> c.inventory();
            case ORDER_CHANGED -> c.order();
            case TICKET_ISSUED, TICKET_VOIDED -> c.ticket();
            case REFUND_CHANGED -> c.refund();
            case SHIPMENT_CHANGED -> c.shipment();
        };
    }

    private boolean verifyCallbackSignature(ProviderCallbackEvent e, String ts, String nonce, String sig) {
        if (ts == null || nonce == null || sig == null || sig.isBlank()) return false;
        try {
            String canonical = ts + "\n" + nonce + "\n" + e.eventId() + "\n" + e.eventType() + "\n" + e.providerCode()
                    + "\n" + e.resourceType() + "\n" + e.providerResourceId() + "\n" + nullToEmpty(e.version()) + "\n" + e.occurredAt();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getCallbackSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), sig.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            return false;
        }
    }

    private void validateTimestamp(String header) {
        Long ms = parseTimestamp(header);
        if (ms == null) throw new BusinessException("X-Timestamp不能为空");
        long diff = Math.abs(System.currentTimeMillis() - ms);
        if (diff > Math.max(30, properties.getCallbackWindowSeconds()) * 1000L) throw new BusinessException(401, "回调时间戳超出允许窗口");
    }

    private Long parseTimestamp(String header) {
        if (header == null || header.isBlank()) return null;
        try {
            long v = Long.parseLong(header.trim());
            return v < 10_000_000_000L ? v * 1000L : v;
        } catch (NumberFormatException ex) { return null; }
    }

    private V11ResourceSyncRequest syncRequest(boolean autoPublish, boolean syncInventory, boolean syncCampaigns) {
        V11ResourceSyncRequest r = new V11ResourceSyncRequest();
        r.setAutoPublish(autoPublish);
        r.setSyncInventory(syncInventory);
        r.setSyncCampaignAssets(syncCampaigns);
        return r;
    }

    private <T> V12BatchResult runBatch(List<Long> ids, java.util.function.Function<Long, T> fn) {
        List<Long> ok = new ArrayList<>(); List<String> failures = new ArrayList<>();
        for (Long id : ids) {
            try { fn.apply(id); ok.add(id); }
            catch (RuntimeException ex) { failures.add(id + ": " + safeMessage(ex)); }
        }
        return new V12BatchResult(ids.size(), ok.size(), failures.size(), ok, failures);
    }

    private Map<String, Object> requiredOne(String sql, Object... args) {
        Map<String, Object> row = one(sql, args);
        if (row == null) throw new BusinessException("数据不存在或状态已变化");
        return row;
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Long queryLong(String sql, Object... args) {
        List<Long> rows = jdbc.queryForList(sql, Long.class, args);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String str(Map<String, Object> row, String key) {
        if (row == null) return null;
        Object v = row.get(key);
        if (v == null) {
            // JdbcTemplate map keys may keep DB casing depending on driver; fallback case-insensitively.
            for (Map.Entry<String, Object> e : row.entrySet()) if (e.getKey().equalsIgnoreCase(key)) { v = e.getValue(); break; }
        }
        return v == null ? null : String.valueOf(v);
    }

    private boolean bool(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        return "1".equals(String.valueOf(v)) || "true".equalsIgnoreCase(String.valueOf(v));
    }

    private int intValue(Object v) { return v == null ? 0 : v instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(v)); }
    private long longValue(Object v) { return v == null ? 0L : v instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(v)); }
    private BigDecimal decimal(Object v) { return v == null ? null : v instanceof BigDecimal b ? b : new BigDecimal(String.valueOf(v)); }
    private long minor(BigDecimal v) { return v == null ? 0 : v.movePointRight(2).longValue(); }
    private BigDecimal major(ProviderMoney m) { return m == null ? BigDecimal.ZERO.setScale(2) : m.toMajor(); }
    private LocalDateTime local(OffsetDateTime v) { return v == null ? null : v.withOffsetSameInstant(CN).toLocalDateTime(); }
    private String identity(String clientTicketNo, String holderRef) { return requiredText(clientTicketNo, "clientTicketNo") + "|" + requiredText(holderRef, "holderRef"); }
    private String joinSeat(ProviderSeatAssignment s) { return String.join(" ", safeStrings(s.zone(), s.row(), s.seat())); }
    private List<String> safeStrings(String... items) { return Arrays.stream(items).filter(x -> x != null && !x.isBlank()).toList(); }
    private <T> List<T> safe(List<T> v) { return v == null ? List.of() : v; }
    private String firstText(String a, String b) { return a == null || a.isBlank() ? b : a; }
    private String requiredText(String v, String name) { if (v == null || v.isBlank()) throw new BusinessException(name + "不能为空"); return v.trim(); }
    private void requirePositive(Long v, String name) { if (v == null || v <= 0) throw new BusinessException(name + "必须为正整数"); }
    private String nullToEmpty(String v) { return v == null ? "" : v; }
    private long backoff(int retry) { return Math.min(300, 5L * (1L << Math.min(5, Math.max(0, retry - 1)))); }
    private String errorCode(Throwable e) { return e instanceof V11AdapterException a ? firstText(a.getSourceErrorCode(), a.getErrorCode().name()) : "TICKET_SOURCE_WORKFLOW_ERROR"; }
    private String safeMessage(Throwable e) { return clip(e == null || e.getMessage() == null ? "未知错误" : e.getMessage()); }
    private String clip(String v) { return v == null ? null : v.length() <= 500 ? v : v.substring(0, 500); }
    private String generateRefundNo() { return "RFV12" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ThreadLocalRandom.current().nextInt(1000, 9999); }
    private String json(Object v) { try { return objectMapper.writeValueAsString(v); } catch (Exception e) { return String.valueOf(v); } }
}
