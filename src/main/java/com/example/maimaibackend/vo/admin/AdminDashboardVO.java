package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;

public class AdminDashboardVO {
    private Integer totalProjectCount;
    private Integer onSaleProjectCount;
    private Integer comingSoonProjectCount;
    private Integer sessionCount;
    private Integer onSaleSessionCount;
    private Integer skuCount;
    private Integer stockAvailableCount;
    private Integer stockLockedCount;
    private Integer soldCount;
    private Integer orderCount;
    private Integer waitPayOrderCount;
    private Integer waitUseOrderCount;
    private Integer finishedOrderCount;
    private Integer refundingOrderCount;
    private Integer refundSuccessOrderCount;
    private Integer userCount;
    private Integer todayOrderCount;
    private BigDecimal paidOrderAmount;
    private BigDecimal todayPaidAmount;

    public Integer getTotalProjectCount() { return totalProjectCount; }
    public void setTotalProjectCount(Integer totalProjectCount) { this.totalProjectCount = totalProjectCount; }
    public Integer getOnSaleProjectCount() { return onSaleProjectCount; }
    public void setOnSaleProjectCount(Integer onSaleProjectCount) { this.onSaleProjectCount = onSaleProjectCount; }
    public Integer getComingSoonProjectCount() { return comingSoonProjectCount; }
    public void setComingSoonProjectCount(Integer comingSoonProjectCount) { this.comingSoonProjectCount = comingSoonProjectCount; }
    public Integer getSessionCount() { return sessionCount; }
    public void setSessionCount(Integer sessionCount) { this.sessionCount = sessionCount; }
    public Integer getOnSaleSessionCount() { return onSaleSessionCount; }
    public void setOnSaleSessionCount(Integer onSaleSessionCount) { this.onSaleSessionCount = onSaleSessionCount; }
    public Integer getSkuCount() { return skuCount; }
    public void setSkuCount(Integer skuCount) { this.skuCount = skuCount; }
    public Integer getStockAvailableCount() { return stockAvailableCount; }
    public void setStockAvailableCount(Integer stockAvailableCount) { this.stockAvailableCount = stockAvailableCount; }
    public Integer getStockLockedCount() { return stockLockedCount; }
    public void setStockLockedCount(Integer stockLockedCount) { this.stockLockedCount = stockLockedCount; }
    public Integer getSoldCount() { return soldCount; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }
    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    public Integer getWaitPayOrderCount() { return waitPayOrderCount; }
    public void setWaitPayOrderCount(Integer waitPayOrderCount) { this.waitPayOrderCount = waitPayOrderCount; }
    public Integer getWaitUseOrderCount() { return waitUseOrderCount; }
    public void setWaitUseOrderCount(Integer waitUseOrderCount) { this.waitUseOrderCount = waitUseOrderCount; }
    public Integer getFinishedOrderCount() { return finishedOrderCount; }
    public void setFinishedOrderCount(Integer finishedOrderCount) { this.finishedOrderCount = finishedOrderCount; }
    public Integer getRefundingOrderCount() { return refundingOrderCount; }
    public void setRefundingOrderCount(Integer refundingOrderCount) { this.refundingOrderCount = refundingOrderCount; }
    public Integer getRefundSuccessOrderCount() { return refundSuccessOrderCount; }
    public void setRefundSuccessOrderCount(Integer refundSuccessOrderCount) { this.refundSuccessOrderCount = refundSuccessOrderCount; }
    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }
    public Integer getTodayOrderCount() { return todayOrderCount; }
    public void setTodayOrderCount(Integer todayOrderCount) { this.todayOrderCount = todayOrderCount; }
    public BigDecimal getPaidOrderAmount() { return paidOrderAmount; }
    public void setPaidOrderAmount(BigDecimal paidOrderAmount) { this.paidOrderAmount = paidOrderAmount; }
    public BigDecimal getTodayPaidAmount() { return todayPaidAmount; }
    public void setTodayPaidAmount(BigDecimal todayPaidAmount) { this.todayPaidAmount = todayPaidAmount; }
}
