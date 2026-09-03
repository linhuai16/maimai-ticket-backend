package com.example.maimaibackend.ticketsource.mock.model;

import java.math.BigDecimal;

public class MockTicketSourceOrderSku {
    private String sourceProjectId;
    private String sourceSessionId;
    private String sourceSkuId;
    private BigDecimal salePrice;
    private String currencyCode;
    private String saleStatus;
    private Integer availableStock;
    private Boolean enabled;

    public String getSourceProjectId() { return sourceProjectId; }
    public void setSourceProjectId(String sourceProjectId) { this.sourceProjectId = sourceProjectId; }
    public String getSourceSessionId() { return sourceSessionId; }
    public void setSourceSessionId(String sourceSessionId) { this.sourceSessionId = sourceSessionId; }
    public String getSourceSkuId() { return sourceSkuId; }
    public void setSourceSkuId(String sourceSkuId) { this.sourceSkuId = sourceSkuId; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getSaleStatus() { return saleStatus; }
    public void setSaleStatus(String saleStatus) { this.saleStatus = saleStatus; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
