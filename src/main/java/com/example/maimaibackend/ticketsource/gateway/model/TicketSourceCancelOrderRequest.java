package com.example.maimaibackend.ticketsource.gateway.model;

public class TicketSourceCancelOrderRequest {
    private String clientOrderNo;
    private String reason;
    private String idempotencyKey;

    public String getClientOrderNo() { return clientOrderNo; }
    public void setClientOrderNo(String clientOrderNo) { this.clientOrderNo = clientOrderNo; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
