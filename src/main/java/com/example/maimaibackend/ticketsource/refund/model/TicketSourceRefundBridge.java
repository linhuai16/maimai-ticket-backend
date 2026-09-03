package com.example.maimaibackend.ticketsource.refund.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketSourceRefundBridge {
    private Long bridgeId;
    private Long refundId;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private String localRefundStatus;
    private String localOrderStatus;
    private Long orderBridgeId;
    private Long providerId;
    private String providerCode;
    private String providerOrderId;
    private String providerRefundId;
    private String providerRefundNo;
    private String bridgeStatus;
    private String providerRefundStatus;
    private BigDecimal refundAmount;
    private BigDecimal feeAmount;
    private String currencyCode;
    private String requestIdempotencyKey;
    private Integer retryCount;
    private Integer maxRetryCount;
    private Boolean manualHold;
    private LocalDateTime nextAttemptTime;
    private LocalDateTime providerRequestTime;
    private LocalDateTime providerRefundTime;
    private String lastOperation;
    private String lastSyncStatus;
    private String lastErrorCode;
    private String lastErrorMessage;
    private Boolean lastErrorRetryable;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getBridgeId() { return bridgeId; }
    public void setBridgeId(Long bridgeId) { this.bridgeId = bridgeId; }
    public Long getRefundId() { return refundId; }
    public void setRefundId(Long refundId) { this.refundId = refundId; }
    public String getRefundNo() { return refundNo; }
    public void setRefundNo(String refundNo) { this.refundNo = refundNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getLocalRefundStatus() { return localRefundStatus; }
    public void setLocalRefundStatus(String localRefundStatus) { this.localRefundStatus = localRefundStatus; }
    public String getLocalOrderStatus() { return localOrderStatus; }
    public void setLocalOrderStatus(String localOrderStatus) { this.localOrderStatus = localOrderStatus; }
    public Long getOrderBridgeId() { return orderBridgeId; }
    public void setOrderBridgeId(Long orderBridgeId) { this.orderBridgeId = orderBridgeId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getProviderRefundId() { return providerRefundId; }
    public void setProviderRefundId(String providerRefundId) { this.providerRefundId = providerRefundId; }
    public String getProviderRefundNo() { return providerRefundNo; }
    public void setProviderRefundNo(String providerRefundNo) { this.providerRefundNo = providerRefundNo; }
    public String getBridgeStatus() { return bridgeStatus; }
    public void setBridgeStatus(String bridgeStatus) { this.bridgeStatus = bridgeStatus; }
    public String getProviderRefundStatus() { return providerRefundStatus; }
    public void setProviderRefundStatus(String providerRefundStatus) { this.providerRefundStatus = providerRefundStatus; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getRequestIdempotencyKey() { return requestIdempotencyKey; }
    public void setRequestIdempotencyKey(String requestIdempotencyKey) { this.requestIdempotencyKey = requestIdempotencyKey; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public Integer getMaxRetryCount() { return maxRetryCount; }
    public void setMaxRetryCount(Integer maxRetryCount) { this.maxRetryCount = maxRetryCount; }
    public Boolean getManualHold() { return manualHold; }
    public void setManualHold(Boolean manualHold) { this.manualHold = manualHold; }
    public LocalDateTime getNextAttemptTime() { return nextAttemptTime; }
    public void setNextAttemptTime(LocalDateTime nextAttemptTime) { this.nextAttemptTime = nextAttemptTime; }
    public LocalDateTime getProviderRequestTime() { return providerRequestTime; }
    public void setProviderRequestTime(LocalDateTime providerRequestTime) { this.providerRequestTime = providerRequestTime; }
    public LocalDateTime getProviderRefundTime() { return providerRefundTime; }
    public void setProviderRefundTime(LocalDateTime providerRefundTime) { this.providerRefundTime = providerRefundTime; }
    public String getLastOperation() { return lastOperation; }
    public void setLastOperation(String lastOperation) { this.lastOperation = lastOperation; }
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
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
