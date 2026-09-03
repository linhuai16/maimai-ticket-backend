package com.example.maimaibackend.ticketsource.gateway.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketSourceRefund {
    private String providerRefundId;
    private String providerRefundNo;
    private String providerOrderId;
    private String clientRefundNo;
    private String refundStatus;
    private BigDecimal refundAmount;
    private BigDecimal feeAmount;
    private String currencyCode;
    private LocalDateTime nextPollTime;
    private LocalDateTime refundTime;
    private String errorCode;
    private String errorMessage;
    private String dataVersion;

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
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public LocalDateTime getNextPollTime() { return nextPollTime; }
    public void setNextPollTime(LocalDateTime nextPollTime) { this.nextPollTime = nextPollTime; }
    public LocalDateTime getRefundTime() { return refundTime; }
    public void setRefundTime(LocalDateTime refundTime) { this.refundTime = refundTime; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
}
