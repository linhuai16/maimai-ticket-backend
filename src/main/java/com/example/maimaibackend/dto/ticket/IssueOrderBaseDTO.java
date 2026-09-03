package com.example.maimaibackend.dto.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IssueOrderBaseDTO {

    private Long orderId;
    private String orderStatus;
    private String fulfillmentMode;
    private BigDecimal payAmount;
    private LocalDateTime ticketIssuedTime;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getFulfillmentMode() { return fulfillmentMode; }
    public void setFulfillmentMode(String fulfillmentMode) { this.fulfillmentMode = fulfillmentMode; }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public LocalDateTime getTicketIssuedTime() {
        return ticketIssuedTime;
    }

    public void setTicketIssuedTime(LocalDateTime ticketIssuedTime) {
        this.ticketIssuedTime = ticketIssuedTime;
    }
}
