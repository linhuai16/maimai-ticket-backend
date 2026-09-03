package com.example.maimaibackend.ticketsource.fulfillment.shipment.model;

import java.math.BigDecimal;

public class V11ShipmentContext {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long projectId;
    private String orderStatus;
    private String paymentStatus;
    private String deliveryType;
    private String deliveryMode;
    private BigDecimal deliveryFeeAmount;
    private Long bridgeId;
    private Long providerId;
    private String providerCode;
    private String providerOrderId;
    private String bridgeStatus;
    private Boolean deliveryFeeRuleRefundable;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }
    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }
    public BigDecimal getDeliveryFeeAmount() { return deliveryFeeAmount; }
    public void setDeliveryFeeAmount(BigDecimal deliveryFeeAmount) { this.deliveryFeeAmount = deliveryFeeAmount; }
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
    public Boolean getDeliveryFeeRuleRefundable() { return deliveryFeeRuleRefundable; }
    public void setDeliveryFeeRuleRefundable(Boolean deliveryFeeRuleRefundable) { this.deliveryFeeRuleRefundable = deliveryFeeRuleRefundable; }
}
