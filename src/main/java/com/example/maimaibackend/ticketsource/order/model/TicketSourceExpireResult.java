package com.example.maimaibackend.ticketsource.order.model;

import java.util.ArrayList;
import java.util.List;

public class TicketSourceExpireResult {
    private int scannedCount;
    private int expiredCount;
    private int failedCount;
    private List<Long> expiredOrderIds = new ArrayList<>();
    private List<Long> failedOrderIds = new ArrayList<>();

    public int getScannedCount() { return scannedCount; }
    public void setScannedCount(int scannedCount) { this.scannedCount = scannedCount; }
    public int getExpiredCount() { return expiredCount; }
    public void setExpiredCount(int expiredCount) { this.expiredCount = expiredCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public List<Long> getExpiredOrderIds() { return expiredOrderIds; }
    public void setExpiredOrderIds(List<Long> expiredOrderIds) { this.expiredOrderIds = expiredOrderIds; }
    public List<Long> getFailedOrderIds() { return failedOrderIds; }
    public void setFailedOrderIds(List<Long> failedOrderIds) { this.failedOrderIds = failedOrderIds; }
}
