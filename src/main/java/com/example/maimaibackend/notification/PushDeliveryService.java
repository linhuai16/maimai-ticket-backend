package com.example.maimaibackend.notification;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushDeliveryService {
    private final JdbcTemplate jdbc;
    private final HuaweiPushGateway gateway;
    private final PushProperties properties;
    private final ServiceCardRemoteRefreshService serviceCardRemoteRefreshService;

    public PushDeliveryService(JdbcTemplate jdbc, HuaweiPushGateway gateway, PushProperties properties,
                               ServiceCardRemoteRefreshService serviceCardRemoteRefreshService) {
        this.jdbc = jdbc;
        this.gateway = gateway;
        this.properties = properties;
        this.serviceCardRemoteRefreshService = serviceCardRemoteRefreshService;
    }

    public void enqueue(String eventKey, String eventType, List<Long> userIds, String title, String content,
                        String targetType, Long orderId, Long ticketId, Long projectId, Long sessionId) {
        for (Long userId : userIds.stream().distinct().toList()) {
            jdbc.update("""
                    INSERT IGNORE INTO push_notification_delivery(event_key,event_type,user_id,binding_id,title,content,target_type,
                        order_id,ticket_id,project_id,session_id,delivery_status,retry_count,next_retry_time,create_time,update_time)
                    SELECT ?,?,?,binding_id,?,?,?,?,?,?,?,'PENDING',0,NOW(),NOW(),NOW()
                    FROM push_device_binding WHERE user_id=? AND binding_status='ACTIVE'
                    """, eventKey, eventType, userId, trim(title, 100), trim(content, 300), targetType,
                    orderId, ticketId, projectId, sessionId, userId);
        }
    }

    @Scheduled(fixedDelayString = "${maimai.notification.push.delivery-delay-ms:10000}")
    public void deliverDue() {
        if (!gateway.isConfigured()) return;
        jdbc.update("UPDATE push_notification_delivery SET delivery_status='RETRY',next_retry_time=NOW(),update_time=NOW() WHERE delivery_status='SENDING' AND update_time<DATE_SUB(NOW(),INTERVAL 5 MINUTE)");
        List<Long> ids = jdbc.queryForList("""
                SELECT delivery_id FROM push_notification_delivery
                WHERE delivery_status IN ('PENDING','RETRY') AND (next_retry_time IS NULL OR next_retry_time<=NOW())
                ORDER BY delivery_id LIMIT 50
                """, Long.class);
        for (Long id : ids) deliver(id);
    }

    private void deliver(Long deliveryId) {
        if (jdbc.update("UPDATE push_notification_delivery SET delivery_status='SENDING',update_time=NOW() WHERE delivery_id=? AND delivery_status IN ('PENDING','RETRY')", deliveryId) != 1) return;
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT d.*,b.user_id AS binding_user_id,b.push_token,b.binding_status FROM push_notification_delivery d
                JOIN push_device_binding b ON b.binding_id=d.binding_id WHERE d.delivery_id=?
                """, deliveryId);
        if (rows.isEmpty()) {
            jdbc.update("UPDATE push_notification_delivery SET delivery_status='DISCARDED',error_code='BINDING_MISSING',update_time=NOW() WHERE delivery_id=?", deliveryId);
            return;
        }
        Map<String, Object> row = rows.get(0);
        if (!"ACTIVE".equals(text(row, "binding_status"))) {
            jdbc.update("UPDATE push_notification_delivery SET delivery_status='DISCARDED',error_code='BINDING_INACTIVE',update_time=NOW() WHERE delivery_id=?", deliveryId);
            return;
        }
        Long deliveryUserId = longNumber(row.get("user_id"));
        Long bindingUserId = longNumber(row.get("binding_user_id"));
        if (deliveryUserId == null || !deliveryUserId.equals(bindingUserId)) {
            jdbc.update("UPDATE push_notification_delivery SET delivery_status='DISCARDED',error_code='BINDING_USER_MISMATCH',update_time=NOW() WHERE delivery_id=?", deliveryId);
            return;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("entryTarget", text(row, "target_type"));
        data.put("entryUserId", text(row, "user_id"));
        put(data, "entryOrderId", row.get("order_id"));
        put(data, "entryTicketId", row.get("ticket_id"));
        put(data, "entryProjectId", row.get("project_id"));
        put(data, "entrySessionId", row.get("session_id"));
        data.put("notificationEventType", text(row, "event_type"));
        HuaweiPushGateway.PushResult result = gateway.send(text(row, "push_token"), text(row, "title"), text(row, "content"), data);
        if (result.success()) {
            jdbc.update("UPDATE push_notification_delivery SET delivery_status='SENT',provider_request_id=?,error_code=NULL,error_message=NULL,sent_time=NOW(),update_time=NOW() WHERE delivery_id=?",
                    result.requestId(), deliveryId);
            serviceCardRemoteRefreshService.refreshIfRequired(text(row, "event_type"), deliveryUserId,
                    longNumber(row.get("binding_id")), text(row, "push_token"));
            return;
        }
        if (result.invalidToken()) {
            jdbc.update("UPDATE push_device_binding SET binding_status='INVALID',invalid_reason=?,update_time=NOW() WHERE binding_id=?",
                    trim(result.code(), 64), row.get("binding_id"));
            jdbc.update("UPDATE push_notification_delivery SET delivery_status='INVALID_TOKEN',error_code=?,error_message=?,update_time=NOW() WHERE delivery_id=?",
                    trim(result.code(), 64), trim(result.message(), 500), deliveryId);
            return;
        }
        int retry = number(row.get("retry_count")) + 1;
        boolean terminal = retry >= Math.max(1, properties.getMaxRetryCount());
        LocalDateTime next = terminal ? null : LocalDateTime.now().plusSeconds(Math.min(900, 15L * (1L << Math.min(5, retry - 1))));
        jdbc.update("UPDATE push_notification_delivery SET delivery_status=?,retry_count=?,next_retry_time=?,error_code=?,error_message=?,update_time=NOW() WHERE delivery_id=?",
                terminal ? "FAILED" : "RETRY", retry, next, trim(result.code(), 64), trim(result.message(), 500), deliveryId);
    }

    private void put(Map<String, Object> data, String key, Object value) { if (value != null) data.put(key, String.valueOf(value)); }
    private String text(Map<String, Object> row, String key) { Object value = row.get(key); return value == null ? "" : String.valueOf(value); }
    private int number(Object value) { return value instanceof Number number ? number.intValue() : 0; }
    private Long longNumber(Object value) { return value instanceof Number number ? number.longValue() : value == null ? null : Long.valueOf(String.valueOf(value)); }
    private String trim(String value, int max) { String text = value == null ? "" : value; return text.substring(0, Math.min(max, text.length())); }
}
