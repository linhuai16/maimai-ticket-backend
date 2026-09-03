package com.example.maimaibackend.ticketsource.domain.model;

import java.time.LocalDateTime;

/**
 * 本地演出项目与第三方项目的映射及最近同步快照。
 */
public class TicketSourceProjectMapping {
    private Long mappingId;
    private Long providerId;
    private Long projectId;
    private String providerProjectId;
    private String providerProjectName;
    private String mappingStatus;
    private String sourceSaleStatus;
    private String sourceDataVersion;
    private Boolean autoPublishEnabled;
    private String lastSyncStatus;
    private LocalDateTime lastSyncTime;
    private String lastErrorCode;
    private String lastErrorMessage;
    private String sourcePayloadSnapshot;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getMappingId() { return mappingId; }
    public void setMappingId(Long mappingId) { this.mappingId = mappingId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderProjectName() { return providerProjectName; }
    public void setProviderProjectName(String providerProjectName) { this.providerProjectName = providerProjectName; }
    public String getMappingStatus() { return mappingStatus; }
    public void setMappingStatus(String mappingStatus) { this.mappingStatus = mappingStatus; }
    public String getSourceSaleStatus() { return sourceSaleStatus; }
    public void setSourceSaleStatus(String sourceSaleStatus) { this.sourceSaleStatus = sourceSaleStatus; }
    public String getSourceDataVersion() { return sourceDataVersion; }
    public void setSourceDataVersion(String sourceDataVersion) { this.sourceDataVersion = sourceDataVersion; }
    public Boolean getAutoPublishEnabled() { return autoPublishEnabled; }
    public void setAutoPublishEnabled(Boolean autoPublishEnabled) { this.autoPublishEnabled = autoPublishEnabled; }
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    public String getLastErrorCode() { return lastErrorCode; }
    public void setLastErrorCode(String lastErrorCode) { this.lastErrorCode = lastErrorCode; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
    public String getSourcePayloadSnapshot() { return sourcePayloadSnapshot; }
    public void setSourcePayloadSnapshot(String sourcePayloadSnapshot) { this.sourcePayloadSnapshot = sourcePayloadSnapshot; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
