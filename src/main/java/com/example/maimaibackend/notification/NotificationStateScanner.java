package com.example.maimaibackend.notification;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificationStateScanner {
    private static final String SEPARATOR = "\u001f";
    private final JdbcTemplate jdbc;
    private final PushDeliveryService deliveryService;
    private final LocalDateTime startedAt = LocalDateTime.now();

    public NotificationStateScanner(JdbcTemplate jdbc, PushDeliveryService deliveryService) {
        this.jdbc = jdbc;
        this.deliveryService = deliveryService;
    }

    @Scheduled(fixedDelayString = "${maimai.notification.push.scan-delay-ms:30000}", initialDelay = 3000)
    public synchronized void scan() {
        scanTicketIssue();
        scanRefunds();
        scanProjects();
        scanSessions();
    }

    private void scanTicketIssue() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT o.order_id,o.user_id,o.project_id,o.session_id,o.order_status,p.title,
                    (SELECT task.task_status FROM ticket_source_issue_task task WHERE task.order_id=o.order_id ORDER BY task.task_id DESC LIMIT 1) issue_task_status,
                    SUM(CASE WHEN t.ticket_status='GENERATING' THEN 1 ELSE 0 END) generating_count,
                    SUM(CASE WHEN t.ticket_status='ERROR' THEN 1 ELSE 0 END) error_count,
                    SUM(CASE WHEN t.ticket_status='UNUSED' THEN 1 ELSE 0 END) unused_count,
                    MIN(CASE WHEN t.ticket_status='UNUSED' THEN t.ticket_id ELSE NULL END) ticket_id,
                    MAX(t.update_time) ticket_update_time
                FROM ticket_order o JOIN performance_project p ON p.project_id=o.project_id
                JOIN electronic_ticket t ON t.order_id=o.order_id
                GROUP BY o.order_id,o.user_id,o.project_id,o.session_id,o.order_status,p.title
                """);
        for (Map<String, Object> row : rows) {
            int generating = integer(row, "generating_count");
            int errors = integer(row, "error_count");
            int unused = integer(row, "unused_count");
            String primary = errors > 0 ? "ERROR" : generating > 0 ? "GENERATING" : unused > 0 ? "ISSUED" : "EMPTY";
            String issueTaskStatus = text(row, "issue_task_status");
            String state = primary + SEPARATOR + text(row, "order_status") + SEPARATOR + issueTaskStatus;
            Observation observation = observe("ORDER_TICKET", text(row, "order_id"), state);
            LocalDateTime ticketUpdateTime = localDateTime(row.get("ticket_update_time"));
            boolean recentFirst = observation.first() && ticketUpdateTime != null && !ticketUpdateTime.isBefore(startedAt);
            if (!observation.changed() || (observation.first() && !recentFirst)) continue;
            String[] previous = observation.previous().split(SEPARATOR, -1);
            String previousPrimary = previous.length > 0 ? previous[0] : "";
            String previousIssueTaskStatus = previous.length > 2 ? previous[2] : "";
            boolean finalIssueError = "ERROR".equals(primary) && isFinalIssueTaskStatus(issueTaskStatus)
                    && (!"ERROR".equals(previousPrimary) || !isFinalIssueTaskStatus(previousIssueTaskStatus));
            if (finalIssueError) {
                enqueueOrder(row, observation, "TICKET_ISSUE_ERROR", "出票异常",
                        "《" + text(row, "title") + "》出票出现异常，请查看订单处理进度", "ORDER_DETAIL", null);
            } else if ("ISSUED".equals(primary) && (observation.first()
                    || "GENERATING".equals(previousPrimary)
                    || ("ERROR".equals(previousPrimary) && !isFinalIssueTaskStatus(previousIssueTaskStatus)))) {
                Long ticketId = longValue(row.get("ticket_id"));
                enqueueOrder(row, observation, "TICKET_ISSUED", "出票成功",
                        "《" + text(row, "title") + "》已出票，可在票夹中查看", ticketId == null ? "ORDER_DETAIL" : "TICKET_DETAIL", ticketId);
            }
        }
    }

    private void scanRefunds() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT r.refund_id,r.refund_status,r.apply_time,r.update_time,o.order_id,o.user_id,o.project_id,o.session_id,p.title
                FROM refund_record r JOIN ticket_order o ON o.order_id=r.order_id
                JOIN performance_project p ON p.project_id=o.project_id
                """);
        for (Map<String, Object> row : rows) {
            String status = text(row, "refund_status");
            Observation observation = observe("REFUND", text(row, "refund_id"), status);
            boolean recentFirst = observation.first() && localDateTime(row.get("apply_time")) != null
                    && !localDateTime(row.get("apply_time")).isBefore(startedAt);
            if (!observation.changed() || (observation.first() && !recentFirst)) continue;
            String eventType;
            String title;
            String content;
            if ("REFUND_SUCCESS".equals(status)) {
                eventType = "REFUND_SUCCESS";
                title = "退款成功";
                content = "《" + text(row, "title") + "》订单退款已完成";
            } else if ("REFUND_FAILED".equals(status) || "REJECTED".equals(status)) {
                eventType = "REFUND_FAILED";
                title = "退款处理结果";
                content = "《" + text(row, "title") + "》退款未完成，请查看详情";
            } else {
                eventType = "REFUND_PROGRESS";
                title = "退款进度更新";
                content = "《" + text(row, "title") + "》退款状态已更新，请查看详情";
            }
            enqueueOrder(row, observation, eventType, title, content, "ORDER_DETAIL", null);
        }
    }

    private void scanProjects() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT p.project_id,p.title,p.project_status,
                    COALESCE(MAX(CASE WHEN UPPER(COALESCE(m.source_status_value,'')) LIKE '%CANCEL%'
                        OR UPPER(COALESCE(m.source_payload_snapshot,'')) LIKE '%CANCELLED%' THEN 1 ELSE 0 END),0) source_cancelled
                FROM performance_project p LEFT JOIN ticket_source_project_mapping m ON m.project_id=p.project_id
                GROUP BY p.project_id,p.title,p.project_status
                """);
        for (Map<String, Object> row : rows) {
            String status = text(row, "project_status");
            boolean cancelled = "CANCELLED".equals(status) || integer(row, "source_cancelled") > 0;
            Observation observation = observe("PROJECT", text(row, "project_id"), status + SEPARATOR + cancelled);
            if (!observation.changed() || observation.first() || !cancelled || observation.previous().endsWith(SEPARATOR + "true")) continue;
            Long projectId = longValue(row.get("project_id"));
            List<Long> users = performanceUsers(projectId, null, false);
            deliveryService.enqueue("PROJECT:" + projectId + ":" + observation.revision() + ":CANCELLED",
                    "PERFORMANCE_CANCELLED", users, "演出取消",
                    "《" + text(row, "title") + "》已取消，请查看后续处理说明", "PERFORMANCE_DETAIL",
                    null, null, projectId, null);
        }
    }

    private void scanSessions() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT s.session_id,s.project_id,s.session_status,s.start_time,s.venue_id,v.venue_name,p.title,
                    COALESCE(MAX(CASE WHEN UPPER(COALESCE(m.source_status_value,'')) LIKE '%CANCEL%'
                        OR UPPER(COALESCE(m.source_payload_snapshot,'')) LIKE '%CANCELLED%' THEN 1 ELSE 0 END),0) source_cancelled
                FROM performance_session s JOIN performance_project p ON p.project_id=s.project_id
                JOIN venue v ON v.venue_id=s.venue_id
                LEFT JOIN ticket_source_session_mapping m ON m.session_id=s.session_id
                GROUP BY s.session_id,s.project_id,s.session_status,s.start_time,s.venue_id,v.venue_name,p.title
                """);
        for (Map<String, Object> row : rows) {
            String status = text(row, "session_status");
            String start = text(row, "start_time");
            String venueId = text(row, "venue_id");
            boolean cancelled = "CANCELLED".equals(status) || integer(row, "source_cancelled") > 0;
            String state = status + SEPARATOR + start + SEPARATOR + venueId + SEPARATOR + text(row, "venue_name") + SEPARATOR + cancelled;
            Observation observation = observe("SESSION", text(row, "session_id"), state);
            if (!observation.changed() || observation.first()) continue;
            String[] previous = observation.previous().split(SEPARATOR, -1);
            Long projectId = longValue(row.get("project_id"));
            Long sessionId = longValue(row.get("session_id"));
            List<Long> users = performanceUsers(projectId, sessionId, false);
            String prefix = "SESSION:" + sessionId + ":" + observation.revision() + ":";
            boolean previousCancelled = previous.length > 4 && "true".equals(previous[4]);
            if (!previousCancelled && cancelled) {
                deliveryService.enqueue(prefix + "CANCELLED", "PERFORMANCE_CANCELLED", users, "场次取消",
                        "《" + text(row, "title") + "》当前场次已取消，请查看详情", "PERFORMANCE_DETAIL",
                        null, null, projectId, sessionId);
            }
            if (!previous[1].equals(start)) {
                deliveryService.enqueue(prefix + "TIME", "PERFORMANCE_RESCHEDULED", users, "演出时间变更",
                        "《" + text(row, "title") + "》演出时间已调整，请查看新时间", "PERFORMANCE_DETAIL",
                        null, null, projectId, sessionId);
            }
            if (!previous[2].equals(venueId)) {
                deliveryService.enqueue(prefix + "VENUE", "PERFORMANCE_VENUE_CHANGED", users, "演出场馆变更",
                        "《" + text(row, "title") + "》场馆已调整为" + text(row, "venue_name"), "PERFORMANCE_DETAIL",
                        null, null, projectId, sessionId);
            }
            if (!"ON_SALE".equals(previous[0]) && "ON_SALE".equals(status)) {
                deliveryService.enqueue(prefix + "ONSALE", "WANTED_ON_SALE", performanceUsers(projectId, sessionId, true),
                        "想看演出已开售", "你想看的《" + text(row, "title") + "》现已开售", "PERFORMANCE_DETAIL",
                        null, null, projectId, sessionId);
            }
        }
    }

    private Observation observe(String type, String id, String state) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT state_value,state_revision FROM push_business_state WHERE entity_type=? AND entity_id=?", type, id);
        if (rows.isEmpty()) {
            try {
                jdbc.update("INSERT INTO push_business_state(entity_type,entity_id,state_value,state_revision,create_time,update_time) VALUES(?,?,?,0,NOW(),NOW())", type, id, state);
                return new Observation(true, true, "", 0);
            } catch (DuplicateKeyException ex) {
                return observe(type, id, state);
            }
        }
        String previous = text(rows.get(0), "state_value");
        int revision = integer(rows.get(0), "state_revision");
        if (previous.equals(state)) return new Observation(false, false, previous, revision);
        int nextRevision = revision + 1;
        int updated = jdbc.update("UPDATE push_business_state SET state_value=?,state_revision=?,update_time=NOW() WHERE entity_type=? AND entity_id=? AND state_revision=?",
                state, nextRevision, type, id, revision);
        return updated == 1 ? new Observation(true, false, previous, nextRevision) : observe(type, id, state);
    }

    private void enqueueOrder(Map<String, Object> row, Observation observation, String eventType, String title,
                              String content, String target, Long ticketId) {
        Long orderId = longValue(row.get("order_id"));
        Long userId = longValue(row.get("user_id"));
        deliveryService.enqueue(eventType + ":" + orderId + ":" + observation.revision(), eventType,
                userId == null ? List.of() : List.of(userId), title, content, target, orderId, ticketId,
                longValue(row.get("project_id")), longValue(row.get("session_id")));
    }

    private List<Long> performanceUsers(Long projectId, Long sessionId, boolean wantsOnly) {
        if (projectId == null) return List.of();
        if (wantsOnly) {
            return jdbc.queryForList("SELECT DISTINCT user_id FROM want_record WHERE project_id=?", Long.class, projectId);
        }
        if (sessionId == null) {
            return jdbc.queryForList("SELECT DISTINCT user_id FROM ticket_order WHERE project_id=? AND payment_status IN ('PAID','PROVIDER_CONFIRMED') AND order_status NOT IN ('CANCELED','REFUND_SUCCESS')", Long.class, projectId);
        }
        return jdbc.queryForList("SELECT DISTINCT user_id FROM ticket_order WHERE session_id=? AND payment_status IN ('PAID','PROVIDER_CONFIRMED') AND order_status NOT IN ('CANCELED','REFUND_SUCCESS')", Long.class, sessionId);
    }


    private boolean isFinalIssueTaskStatus(String status) {
        return status == null || status.isBlank() || "MANUAL_REVIEW".equals(status) || "FAILED".equals(status);
    }

    private String text(Map<String, Object> row, String key) { Object value = row.get(key); return value == null ? "" : String.valueOf(value); }
    private int integer(Map<String, Object> row, String key) { Object value = row.get(key); return value instanceof Number number ? number.intValue() : 0; }
    private Long longValue(Object value) { return value instanceof Number number ? number.longValue() : value == null ? null : Long.valueOf(String.valueOf(value)); }
    private LocalDateTime localDateTime(Object value) { return value instanceof Timestamp timestamp ? timestamp.toLocalDateTime() : value instanceof LocalDateTime time ? time : null; }

    private record Observation(boolean changed, boolean first, String previous, int revision) {}
}
