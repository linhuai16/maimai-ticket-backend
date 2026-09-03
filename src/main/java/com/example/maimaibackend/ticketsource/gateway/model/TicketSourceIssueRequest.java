package com.example.maimaibackend.ticketsource.gateway.model;

public class TicketSourceIssueRequest {
    private String clientOrderNo;
    private Integer expectedTicketCount;
    private String idempotencyKey;

    public String getClientOrderNo() { return clientOrderNo; }
    public void setClientOrderNo(String clientOrderNo) { this.clientOrderNo = clientOrderNo; }
    public Integer getExpectedTicketCount() { return expectedTicketCount; }
    public void setExpectedTicketCount(Integer expectedTicketCount) { this.expectedTicketCount = expectedTicketCount; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
