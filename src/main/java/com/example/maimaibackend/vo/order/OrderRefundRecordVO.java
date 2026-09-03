package com.example.maimaibackend.vo.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderRefundRecordVO {

    private Long refundId;
    private String refundNo;
    private Long orderId;
    private String refundTypeSnapshot;
    private BigDecimal feeRateSnapshot;
    private LocalDateTime applyTime;
    private String reason;
    private BigDecimal refundAmount;
    private BigDecimal feeAmount;
    private String refundStatus;
    private LocalDateTime refundTime;
    private String failReason;

    public Long getRefundId() {
        return refundId;
    }

    public void setRefundId(Long refundId) {
        this.refundId = refundId;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getRefundTypeSnapshot() {
        return refundTypeSnapshot;
    }

    public void setRefundTypeSnapshot(String refundTypeSnapshot) {
        this.refundTypeSnapshot = refundTypeSnapshot;
    }

    public BigDecimal getFeeRateSnapshot() {
        return feeRateSnapshot;
    }

    public void setFeeRateSnapshot(BigDecimal feeRateSnapshot) {
        this.feeRateSnapshot = feeRateSnapshot;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}
