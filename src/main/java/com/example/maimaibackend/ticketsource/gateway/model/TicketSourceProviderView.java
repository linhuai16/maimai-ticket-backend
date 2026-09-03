package com.example.maimaibackend.ticketsource.gateway.model;

import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProvider;

public class TicketSourceProviderView {
    private Long providerId;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String accessMode;
    private String adapterCode;
    private String providerStatus;
    private Integer priority;
    private String baseUrl;
    private Integer connectTimeoutMs;
    private Integer readTimeoutMs;
    private String remark;

    public static TicketSourceProviderView from(TicketSourceProvider provider) {
        TicketSourceProviderView view = new TicketSourceProviderView();
        view.providerId = provider.getProviderId();
        view.providerCode = provider.getProviderCode();
        view.providerName = provider.getProviderName();
        view.providerType = provider.getProviderType();
        view.accessMode = provider.getAccessMode();
        view.adapterCode = provider.getAdapterCode();
        view.providerStatus = provider.getProviderStatus();
        view.priority = provider.getPriority();
        view.baseUrl = provider.getBaseUrl();
        view.connectTimeoutMs = provider.getConnectTimeoutMs();
        view.readTimeoutMs = provider.getReadTimeoutMs();
        view.remark = provider.getRemark();
        return view;
    }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }
    public String getAccessMode() { return accessMode; }
    public void setAccessMode(String accessMode) { this.accessMode = accessMode; }
    public String getAdapterCode() { return adapterCode; }
    public void setAdapterCode(String adapterCode) { this.adapterCode = adapterCode; }
    public String getProviderStatus() { return providerStatus; }
    public void setProviderStatus(String providerStatus) { this.providerStatus = providerStatus; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Integer getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(Integer connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public Integer getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(Integer readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
