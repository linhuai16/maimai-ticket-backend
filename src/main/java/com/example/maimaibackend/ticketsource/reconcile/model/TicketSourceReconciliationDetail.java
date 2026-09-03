package com.example.maimaibackend.ticketsource.reconcile.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketSourceReconciliationDetail {
    private Long detailId;
    private Long batchId;
    private Long orderId;
    private String orderNo;
    private String providerOrderId;
    private String compareStatus;
    private String differenceTypes;
    private String localOrderStatus;
    private String providerOrderStatus;
    private BigDecimal localPayAmount;
    private BigDecimal providerPayAmount;
    private String localRefundStatus;
    private String providerRefundStatus;
    private BigDecimal localRefundAmount;
    private BigDecimal providerRefundAmount;
    private Integer localValidTicketCount;
    private Integer providerValidTicketCount;
    private Integer localTicketTotal;
    private Integer providerTicketTotal;
    private String errorCode;
    private String errorMessage;
    private String snapshotText;
    private LocalDateTime createTime;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getCompareStatus() { return compareStatus; }
    public void setCompareStatus(String compareStatus) { this.compareStatus = compareStatus; }
    public String getDifferenceTypes() { return differenceTypes; }
    public void setDifferenceTypes(String differenceTypes) { this.differenceTypes = differenceTypes; }
    public String getLocalOrderStatus() { return localOrderStatus; }
    public void setLocalOrderStatus(String localOrderStatus) { this.localOrderStatus = localOrderStatus; }
    public String getProviderOrderStatus() { return providerOrderStatus; }
    public void setProviderOrderStatus(String providerOrderStatus) { this.providerOrderStatus = providerOrderStatus; }
    public BigDecimal getLocalPayAmount() { return localPayAmount; }
    public void setLocalPayAmount(BigDecimal localPayAmount) { this.localPayAmount = localPayAmount; }
    public BigDecimal getProviderPayAmount() { return providerPayAmount; }
    public void setProviderPayAmount(BigDecimal providerPayAmount) { this.providerPayAmount = providerPayAmount; }
    public String getLocalRefundStatus() { return localRefundStatus; }
    public void setLocalRefundStatus(String localRefundStatus) { this.localRefundStatus = localRefundStatus; }
    public String getProviderRefundStatus() { return providerRefundStatus; }
    public void setProviderRefundStatus(String providerRefundStatus) { this.providerRefundStatus = providerRefundStatus; }
    public BigDecimal getLocalRefundAmount() { return localRefundAmount; }
    public void setLocalRefundAmount(BigDecimal localRefundAmount) { this.localRefundAmount = localRefundAmount; }
    public BigDecimal getProviderRefundAmount() { return providerRefundAmount; }
    public void setProviderRefundAmount(BigDecimal providerRefundAmount) { this.providerRefundAmount = providerRefundAmount; }
    public Integer getLocalValidTicketCount() { return localValidTicketCount; }
    public void setLocalValidTicketCount(Integer localValidTicketCount) { this.localValidTicketCount = localValidTicketCount; }
    public Integer getProviderValidTicketCount() { return providerValidTicketCount; }
    public void setProviderValidTicketCount(Integer providerValidTicketCount) { this.providerValidTicketCount = providerValidTicketCount; }
    public Integer getLocalTicketTotal() { return localTicketTotal; }
    public void setLocalTicketTotal(Integer localTicketTotal) { this.localTicketTotal = localTicketTotal; }
    public Integer getProviderTicketTotal() { return providerTicketTotal; }
    public void setProviderTicketTotal(Integer providerTicketTotal) { this.providerTicketTotal = providerTicketTotal; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getSnapshotText() { return snapshotText; }
    public void setSnapshotText(String snapshotText) { this.snapshotText = snapshotText; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
