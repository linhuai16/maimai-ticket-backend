package com.example.maimaibackend.ticketsource.resource.provider.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理后台资源映射可观测明细。
 * 仅用于管理员诊断，不进入鸿蒙用户端 VO。
 */
public class V11ResourceMappingDetail {
    private Long projectId;
    private Long sessionId;
    private Long skuId;
    private String skuName;
    private String providerCode;
    private String providerProjectId;
    private String providerSessionId;
    private String providerSkuId;
    private String providerSkuName;
    private String projectMappingStatus;
    private String sessionMappingStatus;
    private String skuMappingStatus;
    private String sourceSaleStatus;
    private String inventoryMode;
    private Integer availableStockSnapshot;
    private BigDecimal providerFacePrice;
    private BigDecimal providerSalePrice;
    private BigDecimal settlementPrice;
    private BigDecimal platformPrice;
    private String priceMode;
    private String deliveryType;
    private String localSkuStatus;
    private String lastSyncStatus;
    private LocalDateTime lastSyncTime;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public String getProviderSkuName() { return providerSkuName; }
    public void setProviderSkuName(String providerSkuName) { this.providerSkuName = providerSkuName; }
    public String getProjectMappingStatus() { return projectMappingStatus; }
    public void setProjectMappingStatus(String projectMappingStatus) { this.projectMappingStatus = projectMappingStatus; }
    public String getSessionMappingStatus() { return sessionMappingStatus; }
    public void setSessionMappingStatus(String sessionMappingStatus) { this.sessionMappingStatus = sessionMappingStatus; }
    public String getSkuMappingStatus() { return skuMappingStatus; }
    public void setSkuMappingStatus(String skuMappingStatus) { this.skuMappingStatus = skuMappingStatus; }
    public String getSourceSaleStatus() { return sourceSaleStatus; }
    public void setSourceSaleStatus(String sourceSaleStatus) { this.sourceSaleStatus = sourceSaleStatus; }
    public String getInventoryMode() { return inventoryMode; }
    public void setInventoryMode(String inventoryMode) { this.inventoryMode = inventoryMode; }
    public Integer getAvailableStockSnapshot() { return availableStockSnapshot; }
    public void setAvailableStockSnapshot(Integer availableStockSnapshot) { this.availableStockSnapshot = availableStockSnapshot; }
    public BigDecimal getProviderFacePrice() { return providerFacePrice; }
    public void setProviderFacePrice(BigDecimal providerFacePrice) { this.providerFacePrice = providerFacePrice; }
    public BigDecimal getProviderSalePrice() { return providerSalePrice; }
    public void setProviderSalePrice(BigDecimal providerSalePrice) { this.providerSalePrice = providerSalePrice; }
    public BigDecimal getSettlementPrice() { return settlementPrice; }
    public void setSettlementPrice(BigDecimal settlementPrice) { this.settlementPrice = settlementPrice; }
    public BigDecimal getPlatformPrice() { return platformPrice; }
    public void setPlatformPrice(BigDecimal platformPrice) { this.platformPrice = platformPrice; }
    public String getPriceMode() { return priceMode; }
    public void setPriceMode(String priceMode) { this.priceMode = priceMode; }
    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }
    public String getLocalSkuStatus() { return localSkuStatus; }
    public void setLocalSkuStatus(String localSkuStatus) { this.localSkuStatus = localSkuStatus; }
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
}
