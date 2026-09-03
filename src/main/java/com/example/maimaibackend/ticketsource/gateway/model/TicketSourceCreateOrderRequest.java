package com.example.maimaibackend.ticketsource.gateway.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TicketSourceCreateOrderRequest {
    private String clientOrderNo;
    private String providerProjectId;
    private String providerSessionId;
    private String providerSkuId;
    private Integer quantity;
    private BigDecimal expectedUnitPrice;
    private BigDecimal payAmount;
    private String currencyCode;
    private LocalDateTime reservationExpireTime;
    private String idempotencyKey;
    private List<TicketSourceOrderAudience> audiences;

    public String getClientOrderNo() { return clientOrderNo; }
    public void setClientOrderNo(String clientOrderNo) { this.clientOrderNo = clientOrderNo; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getExpectedUnitPrice() { return expectedUnitPrice; }
    public void setExpectedUnitPrice(BigDecimal expectedUnitPrice) { this.expectedUnitPrice = expectedUnitPrice; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public LocalDateTime getReservationExpireTime() { return reservationExpireTime; }
    public void setReservationExpireTime(LocalDateTime reservationExpireTime) { this.reservationExpireTime = reservationExpireTime; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public List<TicketSourceOrderAudience> getAudiences() { return audiences; }
    public void setAudiences(List<TicketSourceOrderAudience> audiences) { this.audiences = audiences; }
}
