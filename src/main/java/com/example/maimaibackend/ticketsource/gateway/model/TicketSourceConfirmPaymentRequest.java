package com.example.maimaibackend.ticketsource.gateway.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketSourceConfirmPaymentRequest {
    private String clientOrderNo;
    private BigDecimal payAmount;
    private String currencyCode;
    private String payMethod;
    private LocalDateTime payTime;
    private String idempotencyKey;

    public String getClientOrderNo() { return clientOrderNo; }
    public void setClientOrderNo(String clientOrderNo) { this.clientOrderNo = clientOrderNo; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
