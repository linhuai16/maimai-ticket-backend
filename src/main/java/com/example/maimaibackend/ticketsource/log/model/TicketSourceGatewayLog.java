package com.example.maimaibackend.ticketsource.log.model;

import java.time.LocalDateTime;

public class TicketSourceGatewayLog {
    private Long logId;
    private String requestId;
    private String providerCode;
    private String adapterCode;
    private String operationCode;
    private Boolean success;
    private String gatewayErrorCode;
    private String providerErrorCode;
    private String message;
    private Boolean retryable;
    private Long elapsedMs;
    private LocalDateTime callTime;
    private LocalDateTime createTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getAdapterCode() { return adapterCode; }
    public void setAdapterCode(String adapterCode) { this.adapterCode = adapterCode; }
    public String getOperationCode() { return operationCode; }
    public void setOperationCode(String operationCode) { this.operationCode = operationCode; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getGatewayErrorCode() { return gatewayErrorCode; }
    public void setGatewayErrorCode(String gatewayErrorCode) { this.gatewayErrorCode = gatewayErrorCode; }
    public String getProviderErrorCode() { return providerErrorCode; }
    public void setProviderErrorCode(String providerErrorCode) { this.providerErrorCode = providerErrorCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getRetryable() { return retryable; }
    public void setRetryable(Boolean retryable) { this.retryable = retryable; }
    public Long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(Long elapsedMs) { this.elapsedMs = elapsedMs; }
    public LocalDateTime getCallTime() { return callTime; }
    public void setCallTime(LocalDateTime callTime) { this.callTime = callTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
