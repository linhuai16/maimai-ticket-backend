package com.example.maimaibackend.vo.performance;

import java.math.BigDecimal;

public class TicketSkuVO {
    private Long skuId;
    private Long projectId;
    private Long sessionId;
    private String skuName;
    private String skuDesc;
    private BigDecimal price;
    private Integer stockAvailable;
    private Integer stockLocked;
    private Integer soldCount;
    private String skuStatus;
    private Integer sortOrder;
    private Integer maxSelectableQuantity;

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public String getSkuDesc() { return skuDesc; }
    public void setSkuDesc(String skuDesc) { this.skuDesc = skuDesc; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockAvailable() { return stockAvailable; }
    public void setStockAvailable(Integer stockAvailable) { this.stockAvailable = stockAvailable; }
    public Integer getStockLocked() { return stockLocked; }
    public void setStockLocked(Integer stockLocked) { this.stockLocked = stockLocked; }
    public Integer getSoldCount() { return soldCount; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }
    public String getSkuStatus() { return skuStatus; }
    public void setSkuStatus(String skuStatus) { this.skuStatus = skuStatus; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getMaxSelectableQuantity() { return maxSelectableQuantity; }
    public void setMaxSelectableQuantity(Integer maxSelectableQuantity) { this.maxSelectableQuantity = maxSelectableQuantity; }
}
