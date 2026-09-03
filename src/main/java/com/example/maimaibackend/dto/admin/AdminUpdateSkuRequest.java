package com.example.maimaibackend.dto.admin;

import java.math.BigDecimal;

public class AdminUpdateSkuRequest {
    private String skuName;
    private String skuDesc;
    private BigDecimal price;
    private String skuStatus;
    private Integer sortOrder;

    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public String getSkuDesc() { return skuDesc; }
    public void setSkuDesc(String skuDesc) { this.skuDesc = skuDesc; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getSkuStatus() { return skuStatus; }
    public void setSkuStatus(String skuStatus) { this.skuStatus = skuStatus; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
