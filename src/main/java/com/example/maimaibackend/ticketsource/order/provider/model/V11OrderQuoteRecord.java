package com.example.maimaibackend.ticketsource.order.provider.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class V11OrderQuoteRecord {
    private String quoteId;
    private Long userId;
    private Long providerId;
    private String providerCode;
    private Long projectId;
    private Long sessionId;
    private String providerProjectId;
    private String providerSessionId;
    private String purchaseMode;
    private String ticketMode;
    private String deliveryMode;
    private Long addressId;
    private BigDecimal faceAmount;
    private BigDecimal ticketAmount;
    private BigDecimal providerTicketAmount;
    private BigDecimal providerDiscountAmount;
    private BigDecimal providerPayAmount;
    private BigDecimal settlementAmount;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFeeAmount;
    private BigDecimal serviceFeeAmount;
    private BigDecimal payAmount;
    private String providerDeliveryQuoteId;
    private String requestSnapshot;
    private String itemsSnapshot;
    private String promotionSnapshot;
    private LocalDateTime expireTime;
    private Long usedOrderId;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getPurchaseMode() { return purchaseMode; }
    public void setPurchaseMode(String purchaseMode) { this.purchaseMode = purchaseMode; }
    public String getTicketMode() { return ticketMode; }
    public void setTicketMode(String ticketMode) { this.ticketMode = ticketMode; }
    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public BigDecimal getFaceAmount() { return faceAmount; }
    public void setFaceAmount(BigDecimal faceAmount) { this.faceAmount = faceAmount; }
    public BigDecimal getTicketAmount() { return ticketAmount; }
    public void setTicketAmount(BigDecimal ticketAmount) { this.ticketAmount = ticketAmount; }
    public BigDecimal getProviderTicketAmount() { return providerTicketAmount; }
    public void setProviderTicketAmount(BigDecimal providerTicketAmount) { this.providerTicketAmount = providerTicketAmount; }
    public BigDecimal getProviderDiscountAmount() { return providerDiscountAmount; }
    public void setProviderDiscountAmount(BigDecimal providerDiscountAmount) { this.providerDiscountAmount = providerDiscountAmount; }
    public BigDecimal getProviderPayAmount() { return providerPayAmount; }
    public void setProviderPayAmount(BigDecimal providerPayAmount) { this.providerPayAmount = providerPayAmount; }
    public BigDecimal getSettlementAmount() { return settlementAmount; }
    public void setSettlementAmount(BigDecimal settlementAmount) { this.settlementAmount = settlementAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getDeliveryFeeAmount() { return deliveryFeeAmount; }
    public void setDeliveryFeeAmount(BigDecimal deliveryFeeAmount) { this.deliveryFeeAmount = deliveryFeeAmount; }
    public BigDecimal getServiceFeeAmount() { return serviceFeeAmount; }
    public void setServiceFeeAmount(BigDecimal serviceFeeAmount) { this.serviceFeeAmount = serviceFeeAmount; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getProviderDeliveryQuoteId() { return providerDeliveryQuoteId; }
    public void setProviderDeliveryQuoteId(String providerDeliveryQuoteId) { this.providerDeliveryQuoteId = providerDeliveryQuoteId; }
    public String getRequestSnapshot() { return requestSnapshot; }
    public void setRequestSnapshot(String requestSnapshot) { this.requestSnapshot = requestSnapshot; }
    public String getItemsSnapshot() { return itemsSnapshot; }
    public void setItemsSnapshot(String itemsSnapshot) { this.itemsSnapshot = itemsSnapshot; }
    public String getPromotionSnapshot() { return promotionSnapshot; }
    public void setPromotionSnapshot(String promotionSnapshot) { this.promotionSnapshot = promotionSnapshot; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public Long getUsedOrderId() { return usedOrderId; }
    public void setUsedOrderId(Long usedOrderId) { this.usedOrderId = usedOrderId; }
}
