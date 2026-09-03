package com.example.maimaibackend.ticketsource.sync.model;

import java.time.LocalDateTime;

public class TicketSourceMappingSummary {
    private Long providerId;
    private String providerCode;
    private Long projectMappingId;
    private Long projectId;
    private String providerProjectId;
    private String mappingStatus;
    private String sourceSaleStatus;
    private Boolean autoPublishEnabled;
    private String localProjectStatus;
    private Integer boundSessionCount;
    private Integer boundSkuCount;
    private Integer providerSnapshotSkuCount;
    private Integer providerRealtimeSkuCount;
    private LocalDateTime lastSyncTime;
    private String lastSyncStatus;
    private String lastErrorCode;
    private String lastErrorMessage;

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public Long getProjectMappingId() { return projectMappingId; }
    public void setProjectMappingId(Long projectMappingId) { this.projectMappingId = projectMappingId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getMappingStatus() { return mappingStatus; }
    public void setMappingStatus(String mappingStatus) { this.mappingStatus = mappingStatus; }
    public String getSourceSaleStatus() { return sourceSaleStatus; }
    public void setSourceSaleStatus(String sourceSaleStatus) { this.sourceSaleStatus = sourceSaleStatus; }
    public Boolean getAutoPublishEnabled() { return autoPublishEnabled; }
    public void setAutoPublishEnabled(Boolean autoPublishEnabled) { this.autoPublishEnabled = autoPublishEnabled; }
    public String getLocalProjectStatus() { return localProjectStatus; }
    public void setLocalProjectStatus(String localProjectStatus) { this.localProjectStatus = localProjectStatus; }
    public Integer getBoundSessionCount() { return boundSessionCount; }
    public void setBoundSessionCount(Integer boundSessionCount) { this.boundSessionCount = boundSessionCount; }
    public Integer getBoundSkuCount() { return boundSkuCount; }
    public void setBoundSkuCount(Integer boundSkuCount) { this.boundSkuCount = boundSkuCount; }
    public Integer getProviderSnapshotSkuCount() { return providerSnapshotSkuCount; }
    public void setProviderSnapshotSkuCount(Integer providerSnapshotSkuCount) { this.providerSnapshotSkuCount = providerSnapshotSkuCount; }
    public Integer getProviderRealtimeSkuCount() { return providerRealtimeSkuCount; }
    public void setProviderRealtimeSkuCount(Integer providerRealtimeSkuCount) { this.providerRealtimeSkuCount = providerRealtimeSkuCount; }
    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    public String getLastErrorCode() { return lastErrorCode; }
    public void setLastErrorCode(String lastErrorCode) { this.lastErrorCode = lastErrorCode; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
}
