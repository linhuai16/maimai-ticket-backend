package com.example.maimaibackend.ticketsource.gateway;

import com.example.maimaibackend.mapper.ticketsource.TicketSourceProviderMapper;
import com.example.maimaibackend.ticketsource.domain.enums.TicketSourceProviderStatus;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProvider;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCallResult;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCancelOrderRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceConfirmPaymentRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCreateOrderRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceHealth;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceDelivery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceIssueRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourcePage;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProjectQuery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProviderOrder;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefund;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefundRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSku;
import com.example.maimaibackend.ticketsource.log.TicketSourceGatewayLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

@Service
public class DefaultTicketSourceGateway implements TicketSourceGateway {
    private final TicketSourceProviderMapper providerMapper;
    private final TicketSourceAdapterRegistry adapterRegistry;
    private final TicketSourceCallExecutor callExecutor;
    private final TicketSourceGatewayLogService gatewayLogService;

    public DefaultTicketSourceGateway(
            TicketSourceProviderMapper providerMapper,
            TicketSourceAdapterRegistry adapterRegistry,
            TicketSourceCallExecutor callExecutor,
            TicketSourceGatewayLogService gatewayLogService
    ) {
        this.providerMapper = providerMapper;
        this.adapterRegistry = adapterRegistry;
        this.callExecutor = callExecutor;
        this.gatewayLogService = gatewayLogService;
    }

    @Override
    public TicketSourceCallResult<TicketSourceHealth> health(String providerCode) {
        return invoke(providerCode, TicketSourceOperation.HEALTH,
                (adapter, context) -> adapter.health(context));
    }

    @Override
    public TicketSourceCallResult<TicketSourcePage<TicketSourceProject>> queryProjects(
            String providerCode,
            TicketSourceProjectQuery query
    ) {
        TicketSourceProjectQuery normalized = query == null
                ? new TicketSourceProjectQuery().normalized()
                : query.normalized();
        return invoke(providerCode, TicketSourceOperation.QUERY_PROJECTS,
                (adapter, context) -> adapter.queryProjects(context, normalized));
    }

    @Override
    public TicketSourceCallResult<TicketSourceProject> getProject(
            String providerCode,
            String providerProjectId
    ) {
        String normalizedId = normalizeRequiredId(providerProjectId);
        if (normalizedId == null) {
            return invalidRequest(providerCode, TicketSourceOperation.GET_PROJECT, "第三方项目ID不能为空");
        }
        return invoke(providerCode, TicketSourceOperation.GET_PROJECT,
                (adapter, context) -> adapter.getProject(context, normalizedId));
    }

    @Override
    public TicketSourceCallResult<List<TicketSourceSession>> querySessions(
            String providerCode,
            String providerProjectId
    ) {
        String normalizedId = normalizeRequiredId(providerProjectId);
        if (normalizedId == null) {
            return invalidRequest(providerCode, TicketSourceOperation.QUERY_SESSIONS, "第三方项目ID不能为空");
        }
        return invoke(providerCode, TicketSourceOperation.QUERY_SESSIONS,
                (adapter, context) -> adapter.querySessions(context, normalizedId));
    }

    @Override
    public TicketSourceCallResult<List<TicketSourceSku>> querySkus(
            String providerCode,
            String providerSessionId
    ) {
        String normalizedId = normalizeRequiredId(providerSessionId);
        if (normalizedId == null) {
            return invalidRequest(providerCode, TicketSourceOperation.QUERY_SKUS, "第三方场次ID不能为空");
        }
        return invoke(providerCode, TicketSourceOperation.QUERY_SKUS,
                (adapter, context) -> adapter.querySkus(context, normalizedId));
    }

    @Override
    public TicketSourceCallResult<TicketSourceInventory> queryInventory(
            String providerCode,
            String providerSkuId
    ) {
        String normalizedId = normalizeRequiredId(providerSkuId);
        if (normalizedId == null) {
            return invalidRequest(providerCode, TicketSourceOperation.QUERY_INVENTORY, "第三方票档ID不能为空");
        }
        return invoke(providerCode, TicketSourceOperation.QUERY_INVENTORY,
                (adapter, context) -> adapter.queryInventory(context, normalizedId));
    }

    @Override
    public TicketSourceCallResult<TicketSourceProviderOrder> createOrder(
            String providerCode,
            TicketSourceCreateOrderRequest request
    ) {
        if (request == null || normalizeRequiredId(request.getClientOrderNo()) == null
                || normalizeRequiredId(request.getProviderSkuId()) == null
                || request.getQuantity() == null || request.getQuantity() <= 0
                || normalizeRequiredId(request.getIdempotencyKey()) == null) {
            return invalidRequest(providerCode, TicketSourceOperation.CREATE_ORDER, "第三方订单参数不完整");
        }
        return invoke(providerCode, TicketSourceOperation.CREATE_ORDER,
                (adapter, context) -> adapter.createOrder(context, request));
    }

    @Override
    public TicketSourceCallResult<TicketSourceProviderOrder> confirmPayment(
            String providerCode,
            String providerOrderId,
            TicketSourceConfirmPaymentRequest request
    ) {
        String normalizedId = normalizeRequiredId(providerOrderId);
        if (normalizedId == null || request == null
                || normalizeRequiredId(request.getIdempotencyKey()) == null) {
            return invalidRequest(providerCode, TicketSourceOperation.CONFIRM_PAYMENT, "第三方支付确认参数不完整");
        }
        return invoke(providerCode, TicketSourceOperation.CONFIRM_PAYMENT,
                (adapter, context) -> adapter.confirmPayment(context, normalizedId, request));
    }

    @Override
    public TicketSourceCallResult<TicketSourceProviderOrder> cancelOrder(
            String providerCode,
            String providerOrderId,
            TicketSourceCancelOrderRequest request
    ) {
        String normalizedId = normalizeRequiredId(providerOrderId);
        if (normalizedId == null || request == null
                || normalizeRequiredId(request.getIdempotencyKey()) == null) {
            return invalidRequest(providerCode, TicketSourceOperation.CANCEL_ORDER, "第三方取消订单参数不完整");
        }
        return invoke(providerCode, TicketSourceOperation.CANCEL_ORDER,
                (adapter, context) -> adapter.cancelOrder(context, normalizedId, request));
    }

    @Override
    public TicketSourceCallResult<TicketSourceProviderOrder> getOrder(
            String providerCode,
            String providerOrderId
    ) {
        String normalizedId = normalizeRequiredId(providerOrderId);
        if (normalizedId == null) {
            return invalidRequest(providerCode, TicketSourceOperation.GET_ORDER, "第三方订单ID不能为空");
        }
        return invoke(providerCode, TicketSourceOperation.GET_ORDER,
                (adapter, context) -> adapter.getOrder(context, normalizedId));
    }

    @Override
    public TicketSourceCallResult<TicketSourceDelivery> requestTickets(
            String providerCode, String providerOrderId, TicketSourceIssueRequest request
    ) {
        String normalizedId = normalizeRequiredId(providerOrderId);
        if (normalizedId == null || request == null
                || normalizeRequiredId(request.getIdempotencyKey()) == null
                || request.getExpectedTicketCount() == null || request.getExpectedTicketCount() <= 0) {
            return invalidRequest(providerCode, TicketSourceOperation.REQUEST_TICKETS, "第三方出票请求参数不完整");
        }
        return invoke(providerCode, TicketSourceOperation.REQUEST_TICKETS,
                (adapter, context) -> adapter.requestTickets(context, normalizedId, request));
    }

    @Override
    public TicketSourceCallResult<TicketSourceDelivery> getTickets(
            String providerCode, String providerOrderId
    ) {
        String normalizedId = normalizeRequiredId(providerOrderId);
        if (normalizedId == null) {
            return invalidRequest(providerCode, TicketSourceOperation.GET_TICKETS, "第三方订单ID不能为空");
        }
        return invoke(providerCode, TicketSourceOperation.GET_TICKETS,
                (adapter, context) -> adapter.getTickets(context, normalizedId));
    }

    @Override
    public TicketSourceCallResult<TicketSourceRefund> requestRefund(
            String providerCode,
            String providerOrderId,
            TicketSourceRefundRequest request
    ) {
        String normalizedOrderId = normalizeRequiredId(providerOrderId);
        if (normalizedOrderId == null || request == null
                || normalizeRequiredId(request.getClientRefundNo()) == null
                || normalizeRequiredId(request.getIdempotencyKey()) == null
                || request.getRefundAmount() == null || request.getRefundAmount().signum() <= 0) {
            return invalidRequest(providerCode, TicketSourceOperation.REQUEST_REFUND, "第三方退款参数不完整");
        }
        return invoke(providerCode, TicketSourceOperation.REQUEST_REFUND,
                (adapter, context) -> adapter.requestRefund(context, normalizedOrderId, request));
    }

    @Override
    public TicketSourceCallResult<TicketSourceRefund> getRefund(
            String providerCode,
            String providerRefundId
    ) {
        String normalizedRefundId = normalizeRequiredId(providerRefundId);
        if (normalizedRefundId == null) {
            return invalidRequest(providerCode, TicketSourceOperation.GET_REFUND, "第三方退款ID不能为空");
        }
        return invoke(providerCode, TicketSourceOperation.GET_REFUND,
                (adapter, context) -> adapter.getRefund(context, normalizedRefundId));
    }

    private <T> TicketSourceCallResult<T> invoke(
            String rawProviderCode,
            TicketSourceOperation operation,
            AdapterInvocation<T> invocation
    ) {
        LocalDateTime callTime = LocalDateTime.now();
        long startNanos = System.nanoTime();
        String requestId = UUID.randomUUID().toString().replace("-", "");
        String providerCode = normalizeProviderCode(rawProviderCode);
        if (providerCode == null) {
            return failure(requestId, null, null, operation,
                    TicketSourceGatewayErrorCode.INVALID_REQUEST, null,
                    "票源编码不能为空", false, callTime, startNanos);
        }

        TicketSourceProvider provider;
        try {
            provider = providerMapper.selectByCode(providerCode);
        } catch (Exception e) {
            return failure(requestId, providerCode, null, operation,
                    TicketSourceGatewayErrorCode.INTERNAL_ERROR, null,
                    "读取票源配置失败", true, callTime, startNanos);
        }
        if (provider == null) {
            return failure(requestId, providerCode, null, operation,
                    TicketSourceGatewayErrorCode.PROVIDER_NOT_FOUND, null,
                    "票源不存在: " + providerCode, false, callTime, startNanos);
        }
        String adapterCode = provider.getAdapterCode();
        if (!TicketSourceProviderStatus.ENABLED.name().equals(provider.getProviderStatus())) {
            return failure(requestId, providerCode, adapterCode, operation,
                    TicketSourceGatewayErrorCode.PROVIDER_DISABLED, null,
                    "票源未启用: " + providerCode, false, callTime, startNanos);
        }

        TicketSourceAdapter adapter = adapterRegistry.find(adapterCode).orElse(null);
        if (adapter == null) {
            return failure(requestId, providerCode, adapterCode, operation,
                    TicketSourceGatewayErrorCode.ADAPTER_NOT_FOUND, null,
                    "未找到票源适配器: " + adapterCode, false, callTime, startNanos);
        }

        TicketSourceProviderContext context = new TicketSourceProviderContext(provider, requestId, operation);
        try {
            T data = callExecutor.execute(
                    () -> invocation.call(adapter, context),
                    provider.getReadTimeoutMs()
            );
            if (data == null) {
                return failure(requestId, providerCode, adapterCode, operation,
                        TicketSourceGatewayErrorCode.INVALID_RESPONSE, null,
                        "票源返回空响应", false, callTime, startNanos);
            }
            return record(TicketSourceCallResult.success(
                    requestId,
                    providerCode,
                    adapterCode,
                    operation,
                    data,
                    callTime,
                    elapsedMs(startNanos)
            ));
        } catch (RejectedExecutionException e) {
            return failure(requestId, providerCode, adapterCode, operation,
                    TicketSourceGatewayErrorCode.GATEWAY_BUSY, null,
                    "票源网关任务队列已满", true, callTime, startNanos);
        } catch (TimeoutException e) {
            return failure(requestId, providerCode, adapterCode, operation,
                    TicketSourceGatewayErrorCode.TIMEOUT, null,
                    "票源调用超时", true, callTime, startNanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return failure(requestId, providerCode, adapterCode, operation,
                    TicketSourceGatewayErrorCode.INTERNAL_ERROR, null,
                    "票源调用线程被中断", true, callTime, startNanos);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof TicketSourceAdapterException adapterException) {
                return failure(requestId, providerCode, adapterCode, operation,
                        adapterException.getGatewayErrorCode(),
                        adapterException.getProviderErrorCode(),
                        adapterException.getMessage(),
                        adapterException.isRetryable(),
                        callTime,
                        startNanos);
            }
            return failure(requestId, providerCode, adapterCode, operation,
                    TicketSourceGatewayErrorCode.INTERNAL_ERROR, null,
                    "票源适配器执行异常", true, callTime, startNanos);
        } catch (Exception e) {
            return failure(requestId, providerCode, adapterCode, operation,
                    TicketSourceGatewayErrorCode.INTERNAL_ERROR, null,
                    "票源网关执行异常", true, callTime, startNanos);
        }
    }

    private <T> TicketSourceCallResult<T> invalidRequest(
            String providerCode,
            TicketSourceOperation operation,
            String message
    ) {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        return record(TicketSourceCallResult.failure(
                requestId,
                normalizeProviderCode(providerCode),
                null,
                operation,
                TicketSourceGatewayErrorCode.INVALID_REQUEST,
                null,
                message,
                false,
                LocalDateTime.now(),
                0
        ));
    }

    private <T> TicketSourceCallResult<T> failure(
            String requestId,
            String providerCode,
            String adapterCode,
            TicketSourceOperation operation,
            TicketSourceGatewayErrorCode errorCode,
            String providerErrorCode,
            String message,
            boolean retryable,
            LocalDateTime callTime,
            long startNanos
    ) {
        return record(TicketSourceCallResult.failure(
                requestId,
                providerCode,
                adapterCode,
                operation,
                errorCode,
                providerErrorCode,
                message,
                retryable,
                callTime,
                elapsedMs(startNanos)
        ));
    }

    private <T> TicketSourceCallResult<T> record(TicketSourceCallResult<T> result) {
        gatewayLogService.record(result);
        return result;
    }

    private String normalizeProviderCode(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRequiredId(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private long elapsedMs(long startNanos) {
        return Math.max(0, (System.nanoTime() - startNanos) / 1_000_000L);
    }

    @FunctionalInterface
    private interface AdapterInvocation<T> {
        T call(TicketSourceAdapter adapter, TicketSourceProviderContext context);
    }
}
