package com.example.maimaibackend.dto.admin;

public class AdminUpdateSkuStockRequest {
    private Integer stockAvailable;
    private Integer version;

    public Integer getStockAvailable() { return stockAvailable; }
    public void setStockAvailable(Integer stockAvailable) { this.stockAvailable = stockAvailable; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
