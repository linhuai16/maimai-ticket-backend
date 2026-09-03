package com.example.maimaibackend.ticketsource.gateway.model;

import java.math.BigDecimal;

public class TicketSourceRefundRequest {
    private String clientRefundNo;
    private BigDecimal refundAmount;
    private String currencyCode;
    private String reason;
    private String idempotencyKey;

    public String getClientRefundNo() { return clientRefundNo; }
    public void setClientRefundNo(String clientRefundNo) { this.clientRefundNo = clientRefundNo; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
