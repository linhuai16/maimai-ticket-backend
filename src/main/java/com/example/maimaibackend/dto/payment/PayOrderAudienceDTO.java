package com.example.maimaibackend.dto.payment;

public class PayOrderAudienceDTO {

    private Long orderAudienceId;
    private Long orderId;

    public Long getOrderAudienceId() {
        return orderAudienceId;
    }

    public void setOrderAudienceId(Long orderAudienceId) {
        this.orderAudienceId = orderAudienceId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
