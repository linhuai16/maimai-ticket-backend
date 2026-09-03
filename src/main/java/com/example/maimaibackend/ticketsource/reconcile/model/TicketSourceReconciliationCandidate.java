package com.example.maimaibackend.ticketsource.reconcile.model;

import java.math.BigDecimal;

public class TicketSourceReconciliationCandidate {
    private Long orderId;
    private String orderNo;
    private Long providerId;
    private String providerCode;
    private String providerOrderId;
    private String localOrderStatus;
    private String paymentStatus;
    private BigDecimal localPayAmount;
    private Long refundId;
    private String localRefundStatus;
    private BigDecimal localRefundAmount;
    private String providerRefundId;
    private Integer localValidTicketCount;
    private Integer localTicketTotal;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getLocalOrderStatus() { return localOrderStatus; }
    public void setLocalOrderStatus(String localOrderStatus) { this.localOrderStatus = localOrderStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public BigDecimal getLocalPayAmount() { return localPayAmount; }
    public void setLocalPayAmount(BigDecimal localPayAmount) { this.localPayAmount = localPayAmount; }
    public Long getRefundId() { return refundId; }
    public void setRefundId(Long refundId) { this.refundId = refundId; }
    public String getLocalRefundStatus() { return localRefundStatus; }
    public void setLocalRefundStatus(String localRefundStatus) { this.localRefundStatus = localRefundStatus; }
    public BigDecimal getLocalRefundAmount() { return localRefundAmount; }
    public void setLocalRefundAmount(BigDecimal localRefundAmount) { this.localRefundAmount = localRefundAmount; }
    public String getProviderRefundId() { return providerRefundId; }
    public void setProviderRefundId(String providerRefundId) { this.providerRefundId = providerRefundId; }
    public Integer getLocalValidTicketCount() { return localValidTicketCount; }
    public void setLocalValidTicketCount(Integer localValidTicketCount) { this.localValidTicketCount = localValidTicketCount; }
    public Integer getLocalTicketTotal() { return localTicketTotal; }
    public void setLocalTicketTotal(Integer localTicketTotal) { this.localTicketTotal = localTicketTotal; }
}
