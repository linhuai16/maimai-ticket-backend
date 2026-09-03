package com.example.maimaibackend.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ServiceCardRemoteRefreshService {
    private static final Logger log = LoggerFactory.getLogger(ServiceCardRemoteRefreshService.class);
    private static final Set<String> CARD_EVENTS = Set.of(
            "TICKET_ISSUED",
            "TICKET_ISSUE_ERROR",
            "REFUND_PROGRESS",
            "REFUND_SUCCESS",
            "REFUND_FAILED",
            "PERFORMANCE_RESCHEDULED",
            "PERFORMANCE_CANCELLED",
            "PERFORMANCE_VENUE_CHANGED",
            "WANTED_ON_SALE"
    );
    private final JdbcTemplate jdbc;
    private final ServiceCardDataPayloadService dataPayloadService;
    private final ServiceCardPushGateway gateway;

    public ServiceCardRemoteRefreshService(JdbcTemplate jdbc, ServiceCardDataPayloadService dataPayloadService,
                                           ServiceCardPushGateway gateway) {
        this.jdbc = jdbc;
        this.dataPayloadService = dataPayloadService;
        this.gateway = gateway;
    }

    public void refreshIfRequired(String eventType, Long userId, Long bindingId, String pushToken) {
        if (!CARD_EVENTS.contains(eventType) || userId == null || bindingId == null || pushToken == null
                || pushToken.isBlank()) return;
        List<Map<String, Object>> forms = jdbc.queryForList("""
                SELECT service_card_binding_id,form_id,module_name,ability_name,form_name,city_name,last_push_version
                FROM service_card_binding WHERE binding_id=? ORDER BY service_card_binding_id
                """, bindingId);
        if (forms.isEmpty()) return;
        Map<String, Map<String, Object>> dataByCity = new HashMap<>();
        for (Map<String, Object> form : forms) {
            String city = text(form, "city_name");
            try {
                Map<String, Object> formData = dataByCity.computeIfAbsent(city,
                        key -> dataPayloadService.load(userId, key));
                long lastVersion = number(form.get("last_push_version"));
                long version = Math.max(System.currentTimeMillis() / 1000, lastVersion + 1);
                HuaweiPushGateway.PushResult result = gateway.sendCardRefresh(pushToken,
                        text(form, "form_id"), text(form, "module_name"), text(form, "ability_name"),
                        text(form, "form_name"), version, formData);
                if (result.success()) {
                    jdbc.update("UPDATE service_card_binding SET last_push_version=?,update_time=NOW() WHERE service_card_binding_id=?",
                            version, form.get("service_card_binding_id"));
                } else {
                    log.error("Service card push failed code={} message={}", safe(result.code()), safe(result.message()));
                }
            } catch (RuntimeException ex) {
                log.error("Service card refresh data load failed message={}", safe(ex.getMessage()));
            }
        }
    }

    private String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private long number(Object value) {
        if (value instanceof Number number) return number.longValue();
        return value == null ? 0 : Long.parseLong(String.valueOf(value));
    }

    private String safe(String value) {
        String text = value == null ? "" : value;
        return text.substring(0, Math.min(text.length(), 160));
    }
}
