package com.example.maimaibackend.ticketsource.order.provider.model;

import java.math.BigDecimal;

/** 本地票档与 V1.1 第三方映射的下单上下文。 */
public class V11OrderSkuContext {
    private Long projectId;
    private Long sessionId;
    private Long skuId;
    private String projectStatus;
    private String sessionStatus;
    private String skuStatus;
    private String deliveryType;
    private Integer limitPerOrder;
    private String skuName;
    private BigDecimal localUnitPrice;
    private String priceMode;
    private Integer localStockAvailable;
    private String inventoryAuthority;
    private Long providerId;
    private String providerCode;
    private Long projectMappingId;
    private Long sessionMappingId;
    private Long skuMappingId;
    private String providerProjectId;
    private String providerSessionId;
    private String providerSkuId;
    private String inventoryMode;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }
    public String getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }
    public String getSkuStatus() { return skuStatus; }
    public void setSkuStatus(String skuStatus) { this.skuStatus = skuStatus; }
    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }
    public Integer getLimitPerOrder() { return limitPerOrder; }
    public void setLimitPerOrder(Integer limitPerOrder) { this.limitPerOrder = limitPerOrder; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public BigDecimal getLocalUnitPrice() { return localUnitPrice; }
    public void setLocalUnitPrice(BigDecimal localUnitPrice) { this.localUnitPrice = localUnitPrice; }
    public String getPriceMode() { return priceMode; }
    public void setPriceMode(String priceMode) { this.priceMode = priceMode; }
    public Integer getLocalStockAvailable() { return localStockAvailable; }
    public void setLocalStockAvailable(Integer localStockAvailable) { this.localStockAvailable = localStockAvailable; }
    public String getInventoryAuthority() { return inventoryAuthority; }
    public void setInventoryAuthority(String inventoryAuthority) { this.inventoryAuthority = inventoryAuthority; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public Long getProjectMappingId() { return projectMappingId; }
    public void setProjectMappingId(Long projectMappingId) { this.projectMappingId = projectMappingId; }
    public Long getSessionMappingId() { return sessionMappingId; }
    public void setSessionMappingId(Long sessionMappingId) { this.sessionMappingId = sessionMappingId; }
    public Long getSkuMappingId() { return skuMappingId; }
    public void setSkuMappingId(Long skuMappingId) { this.skuMappingId = skuMappingId; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public String getInventoryMode() { return inventoryMode; }
    public void setInventoryMode(String inventoryMode) { this.inventoryMode = inventoryMode; }
}
