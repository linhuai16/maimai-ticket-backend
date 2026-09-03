package com.example.maimaibackend.vo.refund;

import java.math.BigDecimal;

public class RefundOrderResponse {

    private Long refundId;
    private String status;
    private BigDecimal expectedRefundAmount;

    public Long getRefundId() {
        return refundId;
    }

    public void setRefundId(Long refundId) {
        this.refundId = refundId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getExpectedRefundAmount() {
        return expectedRefundAmount;
    }

    public void setExpectedRefundAmount(BigDecimal expectedRefundAmount) {
        this.expectedRefundAmount = expectedRefundAmount;
    }
}
