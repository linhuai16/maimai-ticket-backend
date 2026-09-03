package com.example.maimaibackend.ticketsource.mock.dto;

public class MockRefundPlanUpdateRequest {
    private String refundMode;
    private Integer delaySeconds;

    public String getRefundMode() { return refundMode; }
    public void setRefundMode(String refundMode) { this.refundMode = refundMode; }
    public Integer getDelaySeconds() { return delaySeconds; }
    public void setDelaySeconds(Integer delaySeconds) { this.delaySeconds = delaySeconds; }
}
