package com.example.maimaibackend.vo.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MockPaySuccessResponse {

    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private BigDecimal payAmount;
    private LocalDateTime payTime;
    private String fulfillmentMode;
    private String paymentStatus;
    private String sourceOrderStatus;
    private String providerOrderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }
    public String getFulfillmentMode() { return fulfillmentMode; }
    public void setFulfillmentMode(String fulfillmentMode) { this.fulfillmentMode = fulfillmentMode; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getSourceOrderStatus() { return sourceOrderStatus; }
    public void setSourceOrderStatus(String sourceOrderStatus) { this.sourceOrderStatus = sourceOrderStatus; }
    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
}

