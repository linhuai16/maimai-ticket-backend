package com.example.maimaibackend.ticketsource.gateway.model;

import java.time.LocalDateTime;

public class TicketSourceHealth {
    private boolean available;
    private String providerTime;
    private int projectCount;
    private int sessionCount;
    private int skuCount;
    private LocalDateTime checkedAt;

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getProviderTime() { return providerTime; }
    public void setProviderTime(String providerTime) { this.providerTime = providerTime; }
    public int getProjectCount() { return projectCount; }
    public void setProjectCount(int projectCount) { this.projectCount = projectCount; }
    public int getSessionCount() { return sessionCount; }
    public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }
    public int getSkuCount() { return skuCount; }
    public void setSkuCount(int skuCount) { this.skuCount = skuCount; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
}
