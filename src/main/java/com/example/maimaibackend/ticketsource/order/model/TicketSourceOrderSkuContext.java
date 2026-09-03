package com.example.maimaibackend.ticketsource.order.model;

import java.math.BigDecimal;

public class TicketSourceOrderSkuContext {
    private Long providerId;
    private String providerCode;
    private String providerStatus;
    private Long projectMappingId;
    private Long sessionMappingId;
    private Long skuMappingId;
    private String projectMappingStatus;
    private String sessionMappingStatus;
    private String skuMappingStatus;
    private String providerProjectId;
    private String providerSessionId;
    private String providerSkuId;
    private String sourceSaleStatus;
    private String inventoryMode;
    private Integer availableStockSnapshot;
    private BigDecimal salePrice;
    private String currencyCode;

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderStatus() { return providerStatus; }
    public void setProviderStatus(String providerStatus) { this.providerStatus = providerStatus; }
    public Long getProjectMappingId() { return projectMappingId; }
    public void setProjectMappingId(Long projectMappingId) { this.projectMappingId = projectMappingId; }
    public Long getSessionMappingId() { return sessionMappingId; }
    public void setSessionMappingId(Long sessionMappingId) { this.sessionMappingId = sessionMappingId; }
    public Long getSkuMappingId() { return skuMappingId; }
    public void setSkuMappingId(Long skuMappingId) { this.skuMappingId = skuMappingId; }
    public String getProjectMappingStatus() { return projectMappingStatus; }
    public void setProjectMappingStatus(String projectMappingStatus) { this.projectMappingStatus = projectMappingStatus; }
    public String getSessionMappingStatus() { return sessionMappingStatus; }
    public void setSessionMappingStatus(String sessionMappingStatus) { this.sessionMappingStatus = sessionMappingStatus; }
    public String getSkuMappingStatus() { return skuMappingStatus; }
    public void setSkuMappingStatus(String skuMappingStatus) { this.skuMappingStatus = skuMappingStatus; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public String getSourceSaleStatus() { return sourceSaleStatus; }
    public void setSourceSaleStatus(String sourceSaleStatus) { this.sourceSaleStatus = sourceSaleStatus; }
    public String getInventoryMode() { return inventoryMode; }
    public void setInventoryMode(String inventoryMode) { this.inventoryMode = inventoryMode; }
    public Integer getAvailableStockSnapshot() { return availableStockSnapshot; }
    public void setAvailableStockSnapshot(Integer availableStockSnapshot) { this.availableStockSnapshot = availableStockSnapshot; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
}
