package com.example.maimaibackend.ticketsource.mock.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MockTicketSourceRefund {
    private String providerRefundId;
    private String providerRefundNo;
    private String providerOrderId;
    private String clientRefundNo;
    private String refundStatus;
    private String refundMode;
    private BigDecimal refundAmount;
    private BigDecimal feeAmount;
    private String currencyCode;
    private String reason;
    private String requestIdempotencyKey;
    private LocalDateTime availableTime;
    private LocalDateTime refundTime;
    private String errorCode;
    private String errorMessage;
    private Boolean inventoryRestored;
    private String dataVersion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getProviderRefundId() { return providerRefundId; }
    public void setProviderRefundId(String providerRefundId) { this.providerRefundId = providerRefundId; }
    public String getProviderRefundNo() { return providerRefundNo; }
    public void setProviderRefundNo(String providerRefundNo) { this.providerRefundNo = providerRefundNo; }
    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getClientRefundNo() { return clientRefundNo; }
    public void setClientRefundNo(String clientRefundNo) { this.clientRefundNo = clientRefundNo; }
    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
    public String getRefundMode() { return refundMode; }
    public void setRefundMode(String refundMode) { this.refundMode = refundMode; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRequestIdempotencyKey() { return requestIdempotencyKey; }
    public void setRequestIdempotencyKey(String requestIdempotencyKey) { this.requestIdempotencyKey = requestIdempotencyKey; }
    public LocalDateTime getAvailableTime() { return availableTime; }
    public void setAvailableTime(LocalDateTime availableTime) { this.availableTime = availableTime; }
    public LocalDateTime getRefundTime() { return refundTime; }
    public void setRefundTime(LocalDateTime refundTime) { this.refundTime = refundTime; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Boolean getInventoryRestored() { return inventoryRestored; }
    public void setInventoryRestored(Boolean inventoryRestored) { this.inventoryRestored = inventoryRestored; }
    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
