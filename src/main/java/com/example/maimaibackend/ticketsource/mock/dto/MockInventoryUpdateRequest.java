package com.example.maimaibackend.ticketsource.mock.dto;

public class MockInventoryUpdateRequest {
    private String inventoryMode;
    private Integer availableStock;
    private String saleStatus;

    public String getInventoryMode() { return inventoryMode; }
    public void setInventoryMode(String inventoryMode) { this.inventoryMode = inventoryMode; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public String getSaleStatus() { return saleStatus; }
    public void setSaleStatus(String saleStatus) { this.saleStatus = saleStatus; }
}
