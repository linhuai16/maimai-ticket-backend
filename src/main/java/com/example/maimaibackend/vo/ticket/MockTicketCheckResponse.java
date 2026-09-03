package com.example.maimaibackend.vo.ticket;

import java.time.LocalDateTime;

public class MockTicketCheckResponse {
    private Boolean success;
    private Long ticketId;
    private Long orderId;
    private String ticketStatus;
    private String orderStatus;
    private Boolean orderFinished;
    private LocalDateTime checkTime;

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(String ticketStatus) { this.ticketStatus = ticketStatus; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public Boolean getOrderFinished() { return orderFinished; }
    public void setOrderFinished(Boolean orderFinished) { this.orderFinished = orderFinished; }
    public LocalDateTime getCheckTime() { return checkTime; }
    public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }
}
