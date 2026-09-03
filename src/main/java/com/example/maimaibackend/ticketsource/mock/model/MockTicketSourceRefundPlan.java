package com.example.maimaibackend.ticketsource.mock.model;

import java.time.LocalDateTime;

public class MockTicketSourceRefundPlan {
    private String providerOrderId;
    private String refundMode;
    private LocalDateTime availableTime;
    private String dataVersion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getRefundMode() { return refundMode; }
    public void setRefundMode(String refundMode) { this.refundMode = refundMode; }
    public LocalDateTime getAvailableTime() { return availableTime; }
    public void setAvailableTime(LocalDateTime availableTime) { this.availableTime = availableTime; }
    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
