package com.example.maimaibackend.ticketsource.sync.model;

public class TicketSourceSkuBinding {
    private Long providerId;
    private String providerCode;
    private Long projectMappingId;
    private Long sessionMappingId;
    private Long skuMappingId;
    private Long projectId;
    private Long sessionId;
    private Long skuId;
    private String providerProjectId;
    private String providerSessionId;
    private String providerSkuId;
    private Boolean autoPublishEnabled;
    private Integer stockLocked;
    private Integer stockAvailable;

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
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public Boolean getAutoPublishEnabled() { return autoPublishEnabled; }
    public void setAutoPublishEnabled(Boolean autoPublishEnabled) { this.autoPublishEnabled = autoPublishEnabled; }
    public Integer getStockLocked() { return stockLocked; }
    public void setStockLocked(Integer stockLocked) { this.stockLocked = stockLocked; }
    public Integer getStockAvailable() { return stockAvailable; }
    public void setStockAvailable(Integer stockAvailable) { this.stockAvailable = stockAvailable; }
}
