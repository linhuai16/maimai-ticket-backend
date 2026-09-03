package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminSkuItemVO {
    private Long skuId;
    private Long projectId;
    private Long sessionId;
    private String skuName;
    private String skuDesc;
    private BigDecimal price;
    private String priceMode;
    private Boolean sourceManaged;
    private String providerCode;
    private String providerSkuId;
    private String providerSkuName;
    private BigDecimal providerFacePrice;
    private BigDecimal providerSalePrice;
    private BigDecimal settlementPrice;
    private String mappingStatus;
    private String sourceSaleStatus;
    private String inventoryMode;
    private Integer availableStockSnapshot;
    private LocalDateTime sourceLastSyncTime;
    private Integer stockAvailable;
    private Integer stockLocked;
    private Integer soldCount;
    private String skuStatus;
    private String inventoryAuthority;
    private Integer sortOrder;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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
    public String getPriceMode() { return priceMode; }
    public void setPriceMode(String priceMode) { this.priceMode = priceMode; }
    public Boolean getSourceManaged() { return sourceManaged; }
    public void setSourceManaged(Boolean sourceManaged) { this.sourceManaged = sourceManaged; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public String getProviderSkuName() { return providerSkuName; }
    public void setProviderSkuName(String providerSkuName) { this.providerSkuName = providerSkuName; }
    public BigDecimal getProviderFacePrice() { return providerFacePrice; }
    public void setProviderFacePrice(BigDecimal providerFacePrice) { this.providerFacePrice = providerFacePrice; }
    public BigDecimal getProviderSalePrice() { return providerSalePrice; }
    public void setProviderSalePrice(BigDecimal providerSalePrice) { this.providerSalePrice = providerSalePrice; }
    public BigDecimal getSettlementPrice() { return settlementPrice; }
    public void setSettlementPrice(BigDecimal settlementPrice) { this.settlementPrice = settlementPrice; }
    public String getMappingStatus() { return mappingStatus; }
    public void setMappingStatus(String mappingStatus) { this.mappingStatus = mappingStatus; }
    public String getSourceSaleStatus() { return sourceSaleStatus; }
    public void setSourceSaleStatus(String sourceSaleStatus) { this.sourceSaleStatus = sourceSaleStatus; }
    public String getInventoryMode() { return inventoryMode; }
    public void setInventoryMode(String inventoryMode) { this.inventoryMode = inventoryMode; }
    public Integer getAvailableStockSnapshot() { return availableStockSnapshot; }
    public void setAvailableStockSnapshot(Integer availableStockSnapshot) { this.availableStockSnapshot = availableStockSnapshot; }
    public LocalDateTime getSourceLastSyncTime() { return sourceLastSyncTime; }
    public void setSourceLastSyncTime(LocalDateTime sourceLastSyncTime) { this.sourceLastSyncTime = sourceLastSyncTime; }
    public Integer getStockAvailable() { return stockAvailable; }
    public void setStockAvailable(Integer stockAvailable) { this.stockAvailable = stockAvailable; }
    public Integer getStockLocked() { return stockLocked; }
    public void setStockLocked(Integer stockLocked) { this.stockLocked = stockLocked; }
    public Integer getSoldCount() { return soldCount; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }
    public String getSkuStatus() { return skuStatus; }
    public void setSkuStatus(String skuStatus) { this.skuStatus = skuStatus; }
    public String getInventoryAuthority() { return inventoryAuthority; }
    public void setInventoryAuthority(String inventoryAuthority) { this.inventoryAuthority = inventoryAuthority; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
