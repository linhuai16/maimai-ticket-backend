package com.example.maimaibackend.ticketsource.order.provider.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class V11LocalOrderContext {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String orderStatus;
    private String paymentStatus;
    private String fulfillmentMode;
    private BigDecimal payAmount;
    private BigDecimal providerPayAmount;
    private LocalDateTime payExpireTime;
    private Long bridgeId;
    private Long providerId;
    private String providerCode;
    private String providerOrderId;
    private String bridgeStatus;
    private String providerOrderStatus;
    private String providerProjectId;
    private String providerSessionId;
    private String createIdempotencyKey;
    private String paymentIdempotencyKey;
    private Integer createRecoveryAttempts;
    private LocalDateTime unknownResultSince;
    private LocalDateTime lastRecoveryTime;
    private String cancelIdempotencyKey;
    private String lastOperation;
    private String deliveryMode;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getFulfillmentMode() { return fulfillmentMode; }
    public void setFulfillmentMode(String fulfillmentMode) { this.fulfillmentMode = fulfillmentMode; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public BigDecimal getProviderPayAmount() { return providerPayAmount; }
    public void setProviderPayAmount(BigDecimal providerPayAmount) { this.providerPayAmount = providerPayAmount; }
    public LocalDateTime getPayExpireTime() { return payExpireTime; }
    public void setPayExpireTime(LocalDateTime payExpireTime) { this.payExpireTime = payExpireTime; }
    public Long getBridgeId() { return bridgeId; }
    public void setBridgeId(Long bridgeId) { this.bridgeId = bridgeId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getBridgeStatus() { return bridgeStatus; }
    public void setBridgeStatus(String bridgeStatus) { this.bridgeStatus = bridgeStatus; }
    public String getProviderOrderStatus() { return providerOrderStatus; }
    public void setProviderOrderStatus(String providerOrderStatus) { this.providerOrderStatus = providerOrderStatus; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getCreateIdempotencyKey() { return createIdempotencyKey; }
    public void setCreateIdempotencyKey(String createIdempotencyKey) { this.createIdempotencyKey = createIdempotencyKey; }
    public Integer getCreateRecoveryAttempts() { return createRecoveryAttempts; }
    public void setCreateRecoveryAttempts(Integer createRecoveryAttempts) { this.createRecoveryAttempts = createRecoveryAttempts; }
    public LocalDateTime getUnknownResultSince() { return unknownResultSince; }
    public void setUnknownResultSince(LocalDateTime unknownResultSince) { this.unknownResultSince = unknownResultSince; }
    public LocalDateTime getLastRecoveryTime() { return lastRecoveryTime; }
    public void setLastRecoveryTime(LocalDateTime lastRecoveryTime) { this.lastRecoveryTime = lastRecoveryTime; }
    public String getPaymentIdempotencyKey() { return paymentIdempotencyKey; }
    public void setPaymentIdempotencyKey(String paymentIdempotencyKey) { this.paymentIdempotencyKey = paymentIdempotencyKey; }
    public String getCancelIdempotencyKey() { return cancelIdempotencyKey; }
    public void setCancelIdempotencyKey(String cancelIdempotencyKey) { this.cancelIdempotencyKey = cancelIdempotencyKey; }
    public String getLastOperation() { return lastOperation; }
    public void setLastOperation(String lastOperation) { this.lastOperation = lastOperation; }
    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }
}
