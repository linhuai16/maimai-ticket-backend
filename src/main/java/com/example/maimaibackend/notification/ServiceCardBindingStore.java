package com.example.maimaibackend.notification;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

@Component
public class ServiceCardBindingStore {
    private final JdbcTemplate jdbc;

    public ServiceCardBindingStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void ensureSchema() {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS service_card_binding (
                    service_card_binding_id BIGINT NOT NULL AUTO_INCREMENT,
                    binding_id BIGINT NOT NULL,
                    form_id VARCHAR(32) NOT NULL,
                    module_name VARCHAR(128) NOT NULL,
                    ability_name VARCHAR(128) NOT NULL,
                    form_name VARCHAR(128) NOT NULL,
                    city_name VARCHAR(64) NOT NULL DEFAULT '北京',
                    last_push_version BIGINT NOT NULL DEFAULT 0,
                    create_time DATETIME NOT NULL,
                    update_time DATETIME NOT NULL,
                    PRIMARY KEY (service_card_binding_id),
                    UNIQUE KEY uk_service_card_form_id (form_id),
                    KEY idx_service_card_binding_id (binding_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
    }

    public void replace(Long bindingId, List<ServiceCardRegistration> registrations) {
        jdbc.update("DELETE FROM service_card_binding WHERE binding_id=?", bindingId);
        if (registrations == null) return;
        for (ServiceCardRegistration registration : registrations.stream().limit(10).toList()) {
            ServiceCardRegistration value = normalize(registration);
            if (value == null) continue;
            jdbc.update("""
                    INSERT INTO service_card_binding(binding_id,form_id,module_name,ability_name,form_name,city_name,
                        last_push_version,create_time,update_time)
                    VALUES(?,?,?,?,?,?,0,NOW(),NOW())
                    ON DUPLICATE KEY UPDATE binding_id=VALUES(binding_id),module_name=VALUES(module_name),
                        ability_name=VALUES(ability_name),form_name=VALUES(form_name),city_name=VALUES(city_name),
                        update_time=NOW()
                    """, bindingId, value.formId(), value.moduleName(), value.abilityName(), value.formName(), value.cityName());
        }
    }

    public void removeByBinding(Long bindingId) {
        jdbc.update("DELETE FROM service_card_binding WHERE binding_id=?", bindingId);
    }

    private ServiceCardRegistration normalize(ServiceCardRegistration registration) {
        if (registration == null) return null;
        String formId = text(registration.formId(), 32);
        if (!formId.matches("[0-9]+") || new BigInteger(formId).signum() <= 0) return null;
        String moduleName = text(registration.moduleName(), 128);
        String abilityName = text(registration.abilityName(), 128);
        String formName = text(registration.formName(), 128);
        if (!"entry".equals(moduleName) || !"ServiceCardFormAbility".equals(abilityName)
                || !("TicketServiceCard2x2".equals(formName) || "TicketServiceCard2x4".equals(formName))) return null;
        String cityName = text(registration.cityName(), 64);
        return new ServiceCardRegistration(formId, moduleName, abilityName, formName,
                cityName.isEmpty() ? "北京" : cityName);
    }

    private String text(String value, int maxLength) {
        String result = value == null ? "" : value.trim();
        return result.substring(0, Math.min(result.length(), maxLength));
    }
}
