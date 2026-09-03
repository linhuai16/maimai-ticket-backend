package com.example.maimaibackend.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayOrderBaseDTO {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private String orderStatus;
    private BigDecimal payAmount;
    private LocalDateTime payExpireTime;
    private LocalDateTime payTime;
    private String fulfillmentMode;
    private String paymentStatus;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public LocalDateTime getPayExpireTime() {
        return payExpireTime;
    }

    public void setPayExpireTime(LocalDateTime payExpireTime) {
        this.payExpireTime = payExpireTime;
    }
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public String getFulfillmentMode() { return fulfillmentMode; }
    public void setFulfillmentMode(String fulfillmentMode) { this.fulfillmentMode = fulfillmentMode; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}

