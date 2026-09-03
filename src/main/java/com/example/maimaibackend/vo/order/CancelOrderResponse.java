package com.example.maimaibackend.vo.order;

import java.time.LocalDateTime;

public class CancelOrderResponse {

    private Boolean success;
    private Long orderId;
    private String orderStatus;
    private LocalDateTime cancelTime;
    private String fulfillmentMode;
    private String sourceOrderStatus;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }
    public String getFulfillmentMode() { return fulfillmentMode; }
    public void setFulfillmentMode(String fulfillmentMode) { this.fulfillmentMode = fulfillmentMode; }
    public String getSourceOrderStatus() { return sourceOrderStatus; }
    public void setSourceOrderStatus(String sourceOrderStatus) { this.sourceOrderStatus = sourceOrderStatus; }
}

