package com.example.maimaibackend.ticketsource.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 本地票档与第三方票品的映射。库存字段是第三方快照，不是本地可独立扣减的库存账本。
 */
public class TicketSourceSkuMapping {
    private Long mappingId;
    private Long providerId;
    private Long sessionMappingId;
    private Long skuId;
    private String providerSkuId;
    private String providerSkuName;
    private String mappingStatus;
    private String sourceSaleStatus;
    private String inventoryMode;
    private Integer availableStockSnapshot;
    private BigDecimal facePrice;
    private BigDecimal salePrice;
    private BigDecimal settlementPrice;
    private String currencyCode;
    private String sourceDataVersion;
    private String lastSyncStatus;
    private LocalDateTime lastSyncTime;
    private LocalDateTime lastInventorySyncTime;
    private String lastErrorCode;
    private String lastErrorMessage;
    private String sourcePayloadSnapshot;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getMappingId() { return mappingId; }
    public void setMappingId(Long mappingId) { this.mappingId = mappingId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public Long getSessionMappingId() { return sessionMappingId; }
    public void setSessionMappingId(Long sessionMappingId) { this.sessionMappingId = sessionMappingId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public String getProviderSkuName() { return providerSkuName; }
    public void setProviderSkuName(String providerSkuName) { this.providerSkuName = providerSkuName; }
    public String getMappingStatus() { return mappingStatus; }
    public void setMappingStatus(String mappingStatus) { this.mappingStatus = mappingStatus; }
    public String getSourceSaleStatus() { return sourceSaleStatus; }
    public void setSourceSaleStatus(String sourceSaleStatus) { this.sourceSaleStatus = sourceSaleStatus; }
    public String getInventoryMode() { return inventoryMode; }
    public void setInventoryMode(String inventoryMode) { this.inventoryMode = inventoryMode; }
    public Integer getAvailableStockSnapshot() { return availableStockSnapshot; }
    public void setAvailableStockSnapshot(Integer availableStockSnapshot) { this.availableStockSnapshot = availableStockSnapshot; }
    public BigDecimal getFacePrice() { return facePrice; }
    public void setFacePrice(BigDecimal facePrice) { this.facePrice = facePrice; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public BigDecimal getSettlementPrice() { return settlementPrice; }
    public void setSettlementPrice(BigDecimal settlementPrice) { this.settlementPrice = settlementPrice; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getSourceDataVersion() { return sourceDataVersion; }
    public void setSourceDataVersion(String sourceDataVersion) { this.sourceDataVersion = sourceDataVersion; }
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    public LocalDateTime getLastInventorySyncTime() { return lastInventorySyncTime; }
    public void setLastInventorySyncTime(LocalDateTime lastInventorySyncTime) { this.lastInventorySyncTime = lastInventorySyncTime; }
    public String getLastErrorCode() { return lastErrorCode; }
    public void setLastErrorCode(String lastErrorCode) { this.lastErrorCode = lastErrorCode; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
    public String getSourcePayloadSnapshot() { return sourcePayloadSnapshot; }
    public void setSourcePayloadSnapshot(String sourcePayloadSnapshot) { this.sourcePayloadSnapshot = sourcePayloadSnapshot; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
