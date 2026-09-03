package com.example.maimaibackend.notification;

import com.example.maimaibackend.common.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Service
public class PushDeviceService {
    private final JdbcTemplate jdbc;
    private final ServiceCardBindingStore serviceCardBindingStore;

    public PushDeviceService(JdbcTemplate jdbc, ServiceCardBindingStore serviceCardBindingStore) {
        this.jdbc = jdbc;
        this.serviceCardBindingStore = serviceCardBindingStore;
    }

    @Transactional
    public Map<String, Object> bind(String authorization, Long userId, String deviceId, String pushToken,
                                    List<ServiceCardRegistration> serviceCards) {
        requireIdentity(authorization, userId);
        String device = requireText(deviceId, "deviceId", 128);
        String token = requireText(pushToken, "pushToken", 1024);
        LocalDateTime now = LocalDateTime.now();
        jdbc.update("UPDATE push_device_binding SET binding_status='INVALID',invalid_reason='TOKEN_REBOUND',update_time=? WHERE push_token=? AND device_id<>? AND binding_status='ACTIVE'",
                now, token, device);
        jdbc.update("""
                INSERT INTO push_device_binding(user_id,device_id,push_token,binding_status,invalid_reason,last_seen_time,create_time,update_time)
                VALUES(?,?,?,'ACTIVE',NULL,?,?,?)
                ON DUPLICATE KEY UPDATE user_id=VALUES(user_id),push_token=VALUES(push_token),binding_status='ACTIVE',
                    invalid_reason=NULL,last_seen_time=VALUES(last_seen_time),update_time=VALUES(update_time)
                """, userId, device, token, now, now, now);
        Long bindingId = jdbc.queryForObject("SELECT binding_id FROM push_device_binding WHERE device_id=?", Long.class, device);
        if (bindingId != null) serviceCardBindingStore.replace(bindingId, serviceCards);
        return Map.of("bound", true, "bindingId", bindingId == null ? 0L : bindingId);
    }

    @Transactional
    public Map<String, Object> unbind(String authorization, Long userId, String deviceId, String pushToken) {
        requireIdentity(authorization, userId);
        String device = requireText(deviceId, "deviceId", 128);
        String token = requireText(pushToken, "pushToken", 1024);
        int rows = jdbc.update("UPDATE push_device_binding SET binding_status='UNBOUND',invalid_reason=NULL,update_time=NOW() WHERE user_id=? AND device_id=? AND push_token=? AND binding_status='ACTIVE'",
                userId, device, token);
        if (rows > 0) {
            Long bindingId = jdbc.queryForObject("SELECT binding_id FROM push_device_binding WHERE device_id=?", Long.class, device);
            if (bindingId != null) serviceCardBindingStore.removeByBinding(bindingId);
        }
        return Map.of("unbound", rows > 0);
    }

    @Scheduled(cron = "0 20 3 * * *")
    public void cleanup() {
        jdbc.update("UPDATE push_device_binding SET binding_status='INVALID',invalid_reason='STALE',update_time=NOW() WHERE binding_status='ACTIVE' AND last_seen_time<DATE_SUB(NOW(),INTERVAL 90 DAY)");
        jdbc.update("DELETE FROM push_device_binding WHERE binding_status IN ('INVALID','UNBOUND') AND update_time<DATE_SUB(NOW(),INTERVAL 30 DAY)");
    }

    private void requireIdentity(String authorization, Long userId) {
        if (userId == null || userId <= 0) throw new BusinessException("userId无效");
        String token = authorization == null ? "" : authorization.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) token = token.substring(7).trim();
        if (!token.startsWith("mock-token-" + userId + "-")) throw new BusinessException(401, "登录凭证与用户不匹配");
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM user_account WHERE user_id=? AND account_status='NORMAL'", Integer.class, userId);
        if (count == null || count == 0) throw new BusinessException(401, "用户不存在或已停用");
    }

    private String requireText(String value, String name, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty() || text.length() > maxLength) throw new BusinessException(name + "无效");
        return text;
    }
}
