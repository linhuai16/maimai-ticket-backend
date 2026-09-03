package com.example.maimaibackend.ticketsource.mock.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MockTicketSourceOrder {
    private String providerOrderId;
    private String providerOrderNo;
    private String clientOrderNo;
    private String sourceProjectId;
    private String sourceSessionId;
    private String sourceSkuId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String currencyCode;
    private String orderStatus;
    private String createIdempotencyKey;
    private String paymentIdempotencyKey;
    private String cancelIdempotencyKey;
    private LocalDateTime reservationExpireTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String dataVersion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getProviderOrderNo() { return providerOrderNo; }
    public void setProviderOrderNo(String providerOrderNo) { this.providerOrderNo = providerOrderNo; }
    public String getClientOrderNo() { return clientOrderNo; }
    public void setClientOrderNo(String clientOrderNo) { this.clientOrderNo = clientOrderNo; }
    public String getSourceProjectId() { return sourceProjectId; }
    public void setSourceProjectId(String sourceProjectId) { this.sourceProjectId = sourceProjectId; }
    public String getSourceSessionId() { return sourceSessionId; }
    public void setSourceSessionId(String sourceSessionId) { this.sourceSessionId = sourceSessionId; }
    public String getSourceSkuId() { return sourceSkuId; }
    public void setSourceSkuId(String sourceSkuId) { this.sourceSkuId = sourceSkuId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getCreateIdempotencyKey() { return createIdempotencyKey; }
    public void setCreateIdempotencyKey(String createIdempotencyKey) { this.createIdempotencyKey = createIdempotencyKey; }
    public String getPaymentIdempotencyKey() { return paymentIdempotencyKey; }
    public void setPaymentIdempotencyKey(String paymentIdempotencyKey) { this.paymentIdempotencyKey = paymentIdempotencyKey; }
    public String getCancelIdempotencyKey() { return cancelIdempotencyKey; }
    public void setCancelIdempotencyKey(String cancelIdempotencyKey) { this.cancelIdempotencyKey = cancelIdempotencyKey; }
    public LocalDateTime getReservationExpireTime() { return reservationExpireTime; }
    public void setReservationExpireTime(LocalDateTime reservationExpireTime) { this.reservationExpireTime = reservationExpireTime; }
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
