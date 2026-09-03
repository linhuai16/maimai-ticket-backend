package com.example.maimaibackend.ticketsource.order.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketSourceOrderBridge {
    private Long bridgeId;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String localOrderStatus;
    private String paymentStatus;
    private Long providerId;
    private String providerCode;
    private Long skuMappingId;
    private Long skuId;
    private String providerOrderId;
    private String providerOrderNo;
    private String providerProjectId;
    private String providerSessionId;
    private String providerSkuId;
    private String bridgeStatus;
    private String providerOrderStatus;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal payAmount;
    private String currencyCode;
    private String createIdempotencyKey;
    private String paymentIdempotencyKey;
    private String cancelIdempotencyKey;
    private LocalDateTime reservationExpireTime;
    private LocalDateTime providerCreateTime;
    private LocalDateTime providerPayTime;
    private LocalDateTime providerCancelTime;
    private String lastOperation;
    private String lastSyncStatus;
    private String lastErrorCode;
    private String lastErrorMessage;
    private Boolean lastErrorRetryable;
    private LocalDateTime unknownResultSince;
    private Integer createRecoveryAttempts;
    private LocalDateTime lastRecoveryTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getBridgeId() { return bridgeId; }
    public void setBridgeId(Long bridgeId) { this.bridgeId = bridgeId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLocalOrderStatus() { return localOrderStatus; }
    public void setLocalOrderStatus(String localOrderStatus) { this.localOrderStatus = localOrderStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public Long getSkuMappingId() { return skuMappingId; }
    public void setSkuMappingId(Long skuMappingId) { this.skuMappingId = skuMappingId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getProviderOrderNo() { return providerOrderNo; }
    public void setProviderOrderNo(String providerOrderNo) { this.providerOrderNo = providerOrderNo; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public String getBridgeStatus() { return bridgeStatus; }
    public void setBridgeStatus(String bridgeStatus) { this.bridgeStatus = bridgeStatus; }
    public String getProviderOrderStatus() { return providerOrderStatus; }
    public void setProviderOrderStatus(String providerOrderStatus) { this.providerOrderStatus = providerOrderStatus; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getCreateIdempotencyKey() { return createIdempotencyKey; }
    public void setCreateIdempotencyKey(String createIdempotencyKey) { this.createIdempotencyKey = createIdempotencyKey; }
    public String getPaymentIdempotencyKey() { return paymentIdempotencyKey; }
    public void setPaymentIdempotencyKey(String paymentIdempotencyKey) { this.paymentIdempotencyKey = paymentIdempotencyKey; }
    public String getCancelIdempotencyKey() { return cancelIdempotencyKey; }
    public void setCancelIdempotencyKey(String cancelIdempotencyKey) { this.cancelIdempotencyKey = cancelIdempotencyKey; }
    public LocalDateTime getReservationExpireTime() { return reservationExpireTime; }
    public void setReservationExpireTime(LocalDateTime reservationExpireTime) { this.reservationExpireTime = reservationExpireTime; }
    public LocalDateTime getProviderCreateTime() { return providerCreateTime; }
    public void setProviderCreateTime(LocalDateTime providerCreateTime) { this.providerCreateTime = providerCreateTime; }
    public LocalDateTime getProviderPayTime() { return providerPayTime; }
    public void setProviderPayTime(LocalDateTime providerPayTime) { this.providerPayTime = providerPayTime; }
    public LocalDateTime getProviderCancelTime() { return providerCancelTime; }
    public void setProviderCancelTime(LocalDateTime providerCancelTime) { this.providerCancelTime = providerCancelTime; }
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
    public LocalDateTime getUnknownResultSince() { return unknownResultSince; }
    public void setUnknownResultSince(LocalDateTime unknownResultSince) { this.unknownResultSince = unknownResultSince; }
    public Integer getCreateRecoveryAttempts() { return createRecoveryAttempts; }
    public void setCreateRecoveryAttempts(Integer createRecoveryAttempts) { this.createRecoveryAttempts = createRecoveryAttempts; }
    public LocalDateTime getLastRecoveryTime() { return lastRecoveryTime; }
    public void setLastRecoveryTime(LocalDateTime lastRecoveryTime) { this.lastRecoveryTime = lastRecoveryTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
