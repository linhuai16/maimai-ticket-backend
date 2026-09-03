package com.example.maimaibackend.ticketsource.log;

import com.example.maimaibackend.mapper.ticketsource.TicketSourceGatewayLogMapper;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCallResult;
import com.example.maimaibackend.ticketsource.log.model.TicketSourceGatewayLog;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class TicketSourceGatewayLogService {
    private final TicketSourceGatewayLogMapper mapper;

    public TicketSourceGatewayLogService(TicketSourceGatewayLogMapper mapper) { this.mapper = mapper; }

    /** 日志失败不得反向影响真实票源调用。 */
    public void record(TicketSourceCallResult<?> result) {
        if (result == null || result.getRequestId() == null) return;
        try {
            TicketSourceGatewayLog log = new TicketSourceGatewayLog();
            log.setRequestId(result.getRequestId());
            log.setProviderCode(result.getProviderCode());
            log.setAdapterCode(result.getAdapterCode());
            log.setOperationCode(result.getOperation() == null ? "UNKNOWN" : result.getOperation().name());
            log.setSuccess(result.isSuccess());
            log.setGatewayErrorCode(result.getErrorCode() == null ? null : result.getErrorCode().name());
            log.setProviderErrorCode(result.getProviderErrorCode());
            log.setMessage(trim(result.getMessage(), 500));
            log.setRetryable(result.isRetryable());
            log.setElapsedMs(result.getElapsedMs());
            log.setCallTime(result.getCallTime());
            mapper.insertLog(log);
        } catch (Exception ignored) {
            // 网关日志为旁路能力；数据库日志异常不应破坏订单、出票和退款调用。
        }
    }

    public List<TicketSourceGatewayLog> list(String providerCode, String operationCode,
                                              Boolean success, Integer rawLimit) {
        int limit = rawLimit == null ? 50 : Math.max(1, Math.min(rawLimit, 200));
        return mapper.selectLogs(normalize(providerCode), normalize(operationCode), success, limit);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String trim(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
