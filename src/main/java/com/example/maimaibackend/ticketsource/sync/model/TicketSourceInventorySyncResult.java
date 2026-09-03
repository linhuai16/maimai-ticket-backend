package com.example.maimaibackend.ticketsource.sync.model;

import java.time.LocalDateTime;

public class TicketSourceInventorySyncResult {
    private boolean success;
    private String providerCode;
    private String providerSkuId;
    private Long skuMappingId;
    private Long skuId;
    private String inventoryMode;
    private Integer providerAvailableStock;
    private Integer localAvailableStock;
    private String sourceSaleStatus;
    private String localSkuStatus;
    private boolean stockApplied;
    private String message;
    private LocalDateTime syncTime;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public Long getSkuMappingId() { return skuMappingId; }
    public void setSkuMappingId(Long skuMappingId) { this.skuMappingId = skuMappingId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getInventoryMode() { return inventoryMode; }
    public void setInventoryMode(String inventoryMode) { this.inventoryMode = inventoryMode; }
    public Integer getProviderAvailableStock() { return providerAvailableStock; }
    public void setProviderAvailableStock(Integer providerAvailableStock) { this.providerAvailableStock = providerAvailableStock; }
    public Integer getLocalAvailableStock() { return localAvailableStock; }
    public void setLocalAvailableStock(Integer localAvailableStock) { this.localAvailableStock = localAvailableStock; }
    public String getSourceSaleStatus() { return sourceSaleStatus; }
    public void setSourceSaleStatus(String sourceSaleStatus) { this.sourceSaleStatus = sourceSaleStatus; }
    public String getLocalSkuStatus() { return localSkuStatus; }
    public void setLocalSkuStatus(String localSkuStatus) { this.localSkuStatus = localSkuStatus; }
    public boolean isStockApplied() { return stockApplied; }
    public void setStockApplied(boolean stockApplied) { this.stockApplied = stockApplied; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSyncTime() { return syncTime; }
    public void setSyncTime(LocalDateTime syncTime) { this.syncTime = syncTime; }
}
