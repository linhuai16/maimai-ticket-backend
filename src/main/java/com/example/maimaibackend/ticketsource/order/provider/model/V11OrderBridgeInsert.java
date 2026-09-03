package com.example.maimaibackend.ticketsource.order.provider.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class V11OrderBridgeInsert {
    private Long bridgeId;
    private Long orderId;
    private Long providerId;
    private String providerProjectId;
    private String providerSessionId;
    private Long skuMappingId;
    private String providerSkuId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal payAmount;
    private String createIdempotencyKey;
    private String paymentIdempotencyKey;
    private String cancelIdempotencyKey;
    private LocalDateTime reservationExpireTime;
    private String requestSnapshot;
    private String quoteId;

    public Long getBridgeId() { return bridgeId; }
    public void setBridgeId(Long bridgeId) { this.bridgeId = bridgeId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public Long getSkuMappingId() { return skuMappingId; }
    public void setSkuMappingId(Long skuMappingId) { this.skuMappingId = skuMappingId; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getCreateIdempotencyKey() { return createIdempotencyKey; }
    public void setCreateIdempotencyKey(String createIdempotencyKey) { this.createIdempotencyKey = createIdempotencyKey; }
    public String getPaymentIdempotencyKey() { return paymentIdempotencyKey; }
    public void setPaymentIdempotencyKey(String paymentIdempotencyKey) { this.paymentIdempotencyKey = paymentIdempotencyKey; }
    public String getCancelIdempotencyKey() { return cancelIdempotencyKey; }
    public void setCancelIdempotencyKey(String cancelIdempotencyKey) { this.cancelIdempotencyKey = cancelIdempotencyKey; }
    public LocalDateTime getReservationExpireTime() { return reservationExpireTime; }
    public void setReservationExpireTime(LocalDateTime reservationExpireTime) { this.reservationExpireTime = reservationExpireTime; }
    public String getRequestSnapshot() { return requestSnapshot; }
    public void setRequestSnapshot(String requestSnapshot) { this.requestSnapshot = requestSnapshot; }
    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
}
