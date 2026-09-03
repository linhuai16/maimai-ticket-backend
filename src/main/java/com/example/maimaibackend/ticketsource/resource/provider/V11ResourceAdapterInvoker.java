package com.example.maimaibackend.ticketsource.resource.provider;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceProviderMapper;
import com.example.maimaibackend.ticketsource.domain.enums.TicketSourceProviderStatus;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProvider;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceGatewayErrorCode;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceOperation;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceProviderContext;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCallResult;
import com.example.maimaibackend.ticketsource.log.TicketSourceGatewayLogService;
import com.example.maimaibackend.ticketsource.provider.adapter.TicketSourceV11Adapter;
import com.example.maimaibackend.ticketsource.provider.adapter.TicketSourceV11AdapterRegistry;
import com.example.maimaibackend.ticketsource.provider.adapter.V11AdapterException;
import com.example.maimaibackend.ticketsource.provider.adapter.V11ErrorCode;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderCode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * V1.1 Adapter 统一调用入口。
 *
 * <p>R5 起，正式 V11 业务调用必须经过 invoke()，统一生成 requestId 并写入
 * ticket_source_gateway_log。旧 context() 仅保留兼容，新的业务代码不得绕过 invoke()。
 * 日志是旁路能力，日志失败不影响真实票源调用。</p>
 */
@Component
public class V11ResourceAdapterInvoker {
    private final TicketSourceProviderMapper providerMapper;
    private final TicketSourceV11AdapterRegistry registry;
    private final TicketSourceGatewayLogService gatewayLogService;

    public V11ResourceAdapterInvoker(TicketSourceProviderMapper providerMapper,
                                     TicketSourceV11AdapterRegistry registry,
                                     TicketSourceGatewayLogService gatewayLogService) {
        this.providerMapper = providerMapper;
        this.registry = registry;
        this.gatewayLogService = gatewayLogService;
    }

    public Target requireEnabled(String rawProviderCode) {
        String normalized = rawProviderCode == null ? null : rawProviderCode.trim().toUpperCase(Locale.ROOT);
        if (normalized == null || normalized.isBlank()) throw new BusinessException("票源编码不能为空");
        ProviderCode code;
        try { code = ProviderCode.valueOf(normalized); }
        catch (IllegalArgumentException e) { throw new BusinessException("仅支持MOCK_DAMAI: " + normalized); }
        TicketSourceProvider provider = providerMapper.selectByCode(code.name());
        if (provider == null) throw new BusinessException("票源配置不存在: " + code);
        if (!TicketSourceProviderStatus.ENABLED.name().equals(provider.getProviderStatus())) {
            throw new BusinessException("票源未启用: " + code);
        }
        TicketSourceV11Adapter adapter = registry.find(code)
                .orElseThrow(() -> new BusinessException("V1.1适配器未注册: " + code));
        return new Target(provider, adapter);
    }

    /**
     * R5 V11 Gateway：统一 requestId、统一成功/失败日志，同时保留 Adapter 原始异常供业务层判断。
     */
    public <T> T invoke(Target target, TicketSourceOperation operation, AdapterCall<T> call) {
        if (target == null || operation == null || call == null) throw new BusinessException("V11 Gateway调用参数不完整");
        String requestId = "V11-" + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime callTime = LocalDateTime.now();
        long start = System.nanoTime();
        TicketSourceProviderContext context = new TicketSourceProviderContext(target.provider(), requestId, operation);
        try {
            T data = call.call(target.adapter(), context);
            if (data == null) {
                throw new V11AdapterException(V11ErrorCode.INVALID_PROVIDER_RESPONSE,
                        "V11_NULL_RESPONSE", "票源返回空响应", false);
            }
            gatewayLogService.record(TicketSourceCallResult.success(
                    requestId, target.provider().getProviderCode(), target.adapter().adapterCode(), operation,
                    data, callTime, elapsedMs(start)));
            return data;
        } catch (V11AdapterException ex) {
            recordFailure(target, operation, requestId, callTime, start, ex);
            throw ex;
        } catch (RuntimeException ex) {
            gatewayLogService.record(TicketSourceCallResult.failure(
                    requestId, target.provider().getProviderCode(), target.adapter().adapterCode(), operation,
                    TicketSourceGatewayErrorCode.INTERNAL_ERROR, null, safeMessage(ex), true,
                    callTime, elapsedMs(start)));
            throw ex;
        }
    }

    /** 兼容旧代码；R5 新增/修改调用请使用 invoke()。 */
    public TicketSourceProviderContext context(Target target, TicketSourceOperation operation) {
        return new TicketSourceProviderContext(target.provider(), "V11-LEGACY-" + UUID.randomUUID(), operation);
    }

    public BusinessException translate(String action, RuntimeException e) {
        if (e instanceof BusinessException businessException) return businessException;
        if (e instanceof V11AdapterException adapterException) {
            String message = action + "失败: " + adapterException.getErrorCode()
                    + (adapterException.getSourceErrorCode() == null ? "" : "/" + adapterException.getSourceErrorCode())
                    + " - " + adapterException.getMessage();
            return new BusinessException(adapterException.isRetryable() ? 503 : 502, message);
        }
        return new BusinessException(500, action + "异常: " + safeMessage(e));
    }

    private void recordFailure(Target target, TicketSourceOperation operation, String requestId,
                               LocalDateTime callTime, long start, V11AdapterException ex) {
        gatewayLogService.record(TicketSourceCallResult.failure(
                requestId, target.provider().getProviderCode(), target.adapter().adapterCode(), operation,
                gatewayError(ex.getErrorCode()), ex.getSourceErrorCode(), safeMessage(ex), ex.isRetryable(),
                callTime, elapsedMs(start)));
    }

    private TicketSourceGatewayErrorCode gatewayError(V11ErrorCode code) {
        if (code == null) return TicketSourceGatewayErrorCode.REMOTE_ERROR;
        return switch (code) {
            case INVALID_REQUEST -> TicketSourceGatewayErrorCode.INVALID_REQUEST;
            case RESOURCE_NOT_FOUND -> TicketSourceGatewayErrorCode.REMOTE_NOT_FOUND;
            case TIMEOUT -> TicketSourceGatewayErrorCode.TIMEOUT;
            case INVALID_PROVIDER_RESPONSE -> TicketSourceGatewayErrorCode.INVALID_RESPONSE;
            default -> TicketSourceGatewayErrorCode.REMOTE_ERROR;
        };
    }

    private long elapsedMs(long start) { return Math.max(0L, (System.nanoTime() - start) / 1_000_000L); }

    private String safeMessage(Throwable e) {
        String value = e == null ? null : e.getMessage();
        if (value == null || value.isBlank()) return "未知错误";
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    @FunctionalInterface
    public interface AdapterCall<T> {
        T call(TicketSourceV11Adapter adapter, TicketSourceProviderContext context);
    }

    public record Target(TicketSourceProvider provider, TicketSourceV11Adapter adapter) {}
}
