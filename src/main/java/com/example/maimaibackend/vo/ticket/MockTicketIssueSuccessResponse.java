package com.example.maimaibackend.vo.ticket;

import java.time.LocalDateTime;

public class MockTicketIssueSuccessResponse {

    private Boolean success;
    private Long orderId;
    private String orderStatus;
    private Integer issuedCount;
    private LocalDateTime ticketIssuedTime;

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

    public Integer getIssuedCount() {
        return issuedCount;
    }

    public void setIssuedCount(Integer issuedCount) {
        this.issuedCount = issuedCount;
    }

    public LocalDateTime getTicketIssuedTime() {
        return ticketIssuedTime;
    }

    public void setTicketIssuedTime(LocalDateTime ticketIssuedTime) {
        this.ticketIssuedTime = ticketIssuedTime;
    }
}
