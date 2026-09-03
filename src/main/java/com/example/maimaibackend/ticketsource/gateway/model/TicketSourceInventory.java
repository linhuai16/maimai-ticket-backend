package com.example.maimaibackend.ticketsource.gateway.model;

import java.time.LocalDateTime;

public class TicketSourceInventory {
    private String providerSkuId;
    private String inventoryMode;
    private Integer availableStock;
    private String saleStatus;
    private String dataVersion;
    private LocalDateTime providerUpdateTime;

    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public String getInventoryMode() { return inventoryMode; }
    public void setInventoryMode(String inventoryMode) { this.inventoryMode = inventoryMode; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public String getSaleStatus() { return saleStatus; }
    public void setSaleStatus(String saleStatus) { this.saleStatus = saleStatus; }
    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
    public LocalDateTime getProviderUpdateTime() { return providerUpdateTime; }
    public void setProviderUpdateTime(LocalDateTime providerUpdateTime) { this.providerUpdateTime = providerUpdateTime; }
}
