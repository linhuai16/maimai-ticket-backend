package com.example.maimaibackend.ticketsource.issue.model;

import java.util.ArrayList;
import java.util.List;

public class TicketSourceIssueBatchResult {
    private Integer requestedCount;
    private Integer processedCount;
    private Integer successCount;
    private Integer pendingCount;
    private Integer failedCount;
    private List<Long> orderIds = new ArrayList<>();

    public Integer getRequestedCount() { return requestedCount; }
    public void setRequestedCount(Integer requestedCount) { this.requestedCount = requestedCount; }
    public Integer getProcessedCount() { return processedCount; }
    public void setProcessedCount(Integer processedCount) { this.processedCount = processedCount; }
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public Integer getPendingCount() { return pendingCount; }
    public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
    public List<Long> getOrderIds() { return orderIds; }
    public void setOrderIds(List<Long> orderIds) { this.orderIds = orderIds; }
}
