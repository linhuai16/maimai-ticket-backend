package com.example.maimaibackend.ticketsource.issue.model;

import java.time.LocalDateTime;

public class TicketSourceIssueTask {
    private Long taskId;
    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private String paymentStatus;
    private Long bridgeId;
    private Long providerId;
    private String providerCode;
    private String providerOrderId;
    private String taskStatus;
    private String providerDeliveryStatus;
    private Integer expectedTicketCount;
    private Integer issuedCount;
    private Integer failedCount;
    private Integer retryCount;
    private Integer maxRetryCount;
    private String issueIdempotencyKey;
    private Boolean requestSent;
    private Boolean manualHold;
    private LocalDateTime nextAttemptTime;
    private LocalDateTime lastAttemptTime;
    private LocalDateTime completeTime;
    private String providerDeliveryVersion;
    private String lastOperation;
    private String lastErrorCode;
    private String lastErrorMessage;
    private Boolean lastErrorRetryable;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Long getBridgeId() { return bridgeId; }
    public void setBridgeId(Long bridgeId) { this.bridgeId = bridgeId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    public String getProviderDeliveryStatus() { return providerDeliveryStatus; }
    public void setProviderDeliveryStatus(String providerDeliveryStatus) { this.providerDeliveryStatus = providerDeliveryStatus; }
    public Integer getExpectedTicketCount() { return expectedTicketCount; }
    public void setExpectedTicketCount(Integer expectedTicketCount) { this.expectedTicketCount = expectedTicketCount; }
    public Integer getIssuedCount() { return issuedCount; }
    public void setIssuedCount(Integer issuedCount) { this.issuedCount = issuedCount; }
    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public Integer getMaxRetryCount() { return maxRetryCount; }
    public void setMaxRetryCount(Integer maxRetryCount) { this.maxRetryCount = maxRetryCount; }
    public String getIssueIdempotencyKey() { return issueIdempotencyKey; }
    public void setIssueIdempotencyKey(String issueIdempotencyKey) { this.issueIdempotencyKey = issueIdempotencyKey; }
    public Boolean getRequestSent() { return requestSent; }
    public void setRequestSent(Boolean requestSent) { this.requestSent = requestSent; }
    public Boolean getManualHold() { return manualHold; }
    public void setManualHold(Boolean manualHold) { this.manualHold = manualHold; }
    public LocalDateTime getNextAttemptTime() { return nextAttemptTime; }
    public void setNextAttemptTime(LocalDateTime nextAttemptTime) { this.nextAttemptTime = nextAttemptTime; }
    public LocalDateTime getLastAttemptTime() { return lastAttemptTime; }
    public void setLastAttemptTime(LocalDateTime lastAttemptTime) { this.lastAttemptTime = lastAttemptTime; }
    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }
    public String getProviderDeliveryVersion() { return providerDeliveryVersion; }
    public void setProviderDeliveryVersion(String providerDeliveryVersion) { this.providerDeliveryVersion = providerDeliveryVersion; }
    public String getLastOperation() { return lastOperation; }
    public void setLastOperation(String lastOperation) { this.lastOperation = lastOperation; }
    public String getLastErrorCode() { return lastErrorCode; }
    public void setLastErrorCode(String lastErrorCode) { this.lastErrorCode = lastErrorCode; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
    public Boolean getLastErrorRetryable() { return lastErrorRetryable; }
    public void setLastErrorRetryable(Boolean lastErrorRetryable) { this.lastErrorRetryable = lastErrorRetryable; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
