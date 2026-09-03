package com.example.maimaibackend.ticketsource.provider.mock;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.ticketsource.provider.adapter.V11AdapterException;
import com.example.maimaibackend.ticketsource.provider.adapter.V11ErrorCode;
import com.example.maimaibackend.ticketsource.provider.mock.dto.MockV11BehaviorRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;

@Component
class LocalMockV11BehaviorService {
    private final JdbcTemplate jdbc;

    LocalMockV11BehaviorService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    List<Map<String, Object>> list() {
        return jdbc.queryForList("""
                SELECT operation_code,enabled,delay_ms,forced_error_code,forced_error_message,
                       forced_retryable,update_time
                FROM mock_ticket_source_behavior
                WHERE operation_code LIKE 'V11_%' ORDER BY operation_code
                """);
    }

    Map<String, Object> update(String operationCode, MockV11BehaviorRequest request) {
        String operation = normalizeOperation(operationCode);
        if (request == null) throw new BusinessException("模拟行为参数不能为空");
        int delay = request.delayMs() == null ? 0 : request.delayMs();
        if (delay < 0 || delay > 60000) throw new BusinessException("delayMs必须在0到60000之间");
        String errorCode = blankToNull(request.forcedErrorCode());
        String message = errorCode == null ? null : firstNonBlank(request.forcedErrorMessage(), "V1.1模拟器强制错误");
        int rows = jdbc.update("""
                UPDATE mock_ticket_source_behavior
                SET enabled=?,delay_ms=?,forced_error_code=?,forced_error_message=?,forced_retryable=?,update_time=NOW()
                WHERE operation_code=?
                """, Boolean.TRUE.equals(request.enabled()), delay, errorCode, message,
                Boolean.TRUE.equals(request.retryable()), operation);
        if (rows != 1) throw new BusinessException("V1.1模拟操作不存在: " + operation);
        return required(operation);
    }

    List<Map<String, Object>> reset() {
        jdbc.update("""
                UPDATE mock_ticket_source_behavior
                SET enabled=0,delay_ms=0,forced_error_code=NULL,forced_error_message=NULL,
                    forced_retryable=0,update_time=NOW()
                WHERE operation_code LIKE 'V11_%'
                """);
        return list();
    }

    void apply(String operationCode) {
        Map<String, Object> behavior = find(operationCode);
        if (behavior == null || !bool(behavior.get("enabled"))) return;
        String error = string(behavior, "forced_error_code");
        if (isPostCommitLoss(error)) return;
        int delay = intValue(behavior.get("delay_ms"));
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new V11AdapterException(V11ErrorCode.TIMEOUT,
                        "MOCK_INTERRUPTED", "V1.1模拟调用被中断", true);
            }
        }
        if (error != null) {
            throw new V11AdapterException(V11ErrorCode.PROVIDER_ERROR, error,
                    firstNonBlank(string(behavior, "forced_error_message"), "V1.1模拟器强制错误"),
                    bool(behavior.get("forced_retryable")));
        }
    }

    String postCommitLossMode(String operationCode) {
        Map<String, Object> behavior = find(operationCode);
        if (behavior == null || !bool(behavior.get("enabled"))) return null;
        String error = string(behavior, "forced_error_code");
        return isPostCommitLoss(error) ? error : null;
    }

    void registerPostCommitLoss(String mode) {
        if (mode == null) return;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("R5 post-commit behavior requires active transaction");
        }
        Map<String, Object> behavior = find("V11_CREATE_ORDER");
        int delay = behavior == null ? 0 : intValue(behavior.get("delay_ms"));
        String message = behavior == null ? null : string(behavior, "forced_error_message");
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                V11ErrorCode code = "AFTER_COMMIT_TIMEOUT".equals(mode)
                        ? V11ErrorCode.TIMEOUT : V11ErrorCode.PROVIDER_ERROR;
                String fallback = "AFTER_COMMIT_TIMEOUT".equals(mode)
                        ? "Provider 已创建订单，但成功响应返回超时"
                        : "Provider 已创建订单，但成功响应丢失";
                throw new V11AdapterException(code, "MOCK_" + mode,
                        firstNonBlank(message, fallback), true, true);
            }
        });
    }

    private Map<String, Object> required(String operationCode) {
        Map<String, Object> behavior = find(operationCode);
        if (behavior == null) throw new BusinessException("模拟行为不存在");
        return behavior;
    }

    private Map<String, Object> find(String operationCode) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM mock_ticket_source_behavior WHERE operation_code=?", operationCode);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String normalizeOperation(String operationCode) {
        String value = operationCode == null || operationCode.isBlank()
                ? null : operationCode.trim().toUpperCase();
        if (value == null) throw new BusinessException("operationCode不能为空");
        if (!value.startsWith("V11_")) value = "V11_" + value;
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mock_ticket_source_behavior WHERE operation_code=? AND operation_code LIKE 'V11_%'",
                Integer.class, value);
        if (count == null || count == 0) throw new BusinessException("不支持的V1.1模拟操作: " + operationCode);
        return value;
    }

    private boolean isPostCommitLoss(String errorCode) {
        return "AFTER_COMMIT_TIMEOUT".equals(errorCode) || "RESPONSE_LOST".equals(errorCode);
    }

    private boolean bool(Object value) {
        if (value instanceof Boolean booleanValue) return booleanValue;
        if (value instanceof Number number) return number.intValue() != 0;
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    private int intValue(Object value) {
        if (value instanceof Number number) return number.intValue();
        return value == null ? 0 : Integer.parseInt(String.valueOf(value));
    }

    private String string(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
