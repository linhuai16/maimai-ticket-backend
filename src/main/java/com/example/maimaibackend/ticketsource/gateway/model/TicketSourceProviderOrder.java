package com.example.maimaibackend.ticketsource.gateway.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketSourceProviderOrder {
    private String providerOrderId;
    private String providerOrderNo;
    private String clientOrderNo;
    private String providerProjectId;
    private String providerSessionId;
    private String providerSkuId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String currencyCode;
    private String orderStatus;
    private Integer remainingStock;
    private LocalDateTime reservationExpireTime;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String dataVersion;

    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getProviderOrderNo() { return providerOrderNo; }
    public void setProviderOrderNo(String providerOrderNo) { this.providerOrderNo = providerOrderNo; }
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
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public Integer getRemainingStock() { return remainingStock; }
    public void setRemainingStock(Integer remainingStock) { this.remainingStock = remainingStock; }
    public LocalDateTime getReservationExpireTime() { return reservationExpireTime; }
    public void setReservationExpireTime(LocalDateTime reservationExpireTime) { this.reservationExpireTime = reservationExpireTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }
    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
}
