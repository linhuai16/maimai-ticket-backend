package com.example.maimaibackend.ticketsource.gateway.model;

import com.example.maimaibackend.ticketsource.gateway.TicketSourceGatewayErrorCode;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceOperation;

import java.time.LocalDateTime;

/**
 * 统一网关调用结果。HTTP 控制器、同步任务和后续订单桥接均复用此结构。
 */
public class TicketSourceCallResult<T> {
    private boolean success;
    private String requestId;
    private String providerCode;
    private String adapterCode;
    private TicketSourceOperation operation;
    private TicketSourceGatewayErrorCode errorCode;
    private String providerErrorCode;
    private String message;
    private boolean retryable;
    private T data;
    private LocalDateTime callTime;
    private long elapsedMs;

    public static <T> TicketSourceCallResult<T> success(
            String requestId,
            String providerCode,
            String adapterCode,
            TicketSourceOperation operation,
            T data,
            LocalDateTime callTime,
            long elapsedMs
    ) {
        TicketSourceCallResult<T> result = new TicketSourceCallResult<>();
        result.success = true;
        result.requestId = requestId;
        result.providerCode = providerCode;
        result.adapterCode = adapterCode;
        result.operation = operation;
        result.errorCode = TicketSourceGatewayErrorCode.SUCCESS;
        result.message = "success";
        result.data = data;
        result.callTime = callTime;
        result.elapsedMs = elapsedMs;
        return result;
    }

    public static <T> TicketSourceCallResult<T> failure(
            String requestId,
            String providerCode,
            String adapterCode,
            TicketSourceOperation operation,
            TicketSourceGatewayErrorCode errorCode,
            String providerErrorCode,
            String message,
            boolean retryable,
            LocalDateTime callTime,
            long elapsedMs
    ) {
        TicketSourceCallResult<T> result = new TicketSourceCallResult<>();
        result.success = false;
        result.requestId = requestId;
        result.providerCode = providerCode;
        result.adapterCode = adapterCode;
        result.operation = operation;
        result.errorCode = errorCode;
        result.providerErrorCode = providerErrorCode;
        result.message = message;
        result.retryable = retryable;
        result.callTime = callTime;
        result.elapsedMs = elapsedMs;
        return result;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getAdapterCode() { return adapterCode; }
    public void setAdapterCode(String adapterCode) { this.adapterCode = adapterCode; }
    public TicketSourceOperation getOperation() { return operation; }
    public void setOperation(TicketSourceOperation operation) { this.operation = operation; }
    public TicketSourceGatewayErrorCode getErrorCode() { return errorCode; }
    public void setErrorCode(TicketSourceGatewayErrorCode errorCode) { this.errorCode = errorCode; }
    public String getProviderErrorCode() { return providerErrorCode; }
    public void setProviderErrorCode(String providerErrorCode) { this.providerErrorCode = providerErrorCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRetryable() { return retryable; }
    public void setRetryable(boolean retryable) { this.retryable = retryable; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public LocalDateTime getCallTime() { return callTime; }
    public void setCallTime(LocalDateTime callTime) { this.callTime = callTime; }
    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }
}
