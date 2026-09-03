package com.example.maimaibackend.ticketsource.refund.model;

import java.util.ArrayList;
import java.util.List;

public class TicketSourceRefundBatchResult {
    private Integer requestedCount;
    private Integer processedCount;
    private Integer successCount;
    private Integer pendingCount;
    private Integer failedCount;
    private List<Long> refundIds = new ArrayList<>();

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
    public List<Long> getRefundIds() { return refundIds; }
    public void setRefundIds(List<Long> refundIds) { this.refundIds = refundIds == null ? new ArrayList<>() : refundIds; }
}
