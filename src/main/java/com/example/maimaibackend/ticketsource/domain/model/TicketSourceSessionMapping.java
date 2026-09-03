package com.example.maimaibackend.ticketsource.domain.model;

import java.time.LocalDateTime;

/**
 * 本地演出场次与第三方场次的映射及最近同步快照。
 */
public class TicketSourceSessionMapping {
    private Long mappingId;
    private Long providerId;
    private Long projectMappingId;
    private Long sessionId;
    private String providerSessionId;
    private String providerSessionName;
    private String mappingStatus;
    private String sourceSaleStatus;
    private String sourceDataVersion;
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
    public Long getProjectMappingId() { return projectMappingId; }
    public void setProjectMappingId(Long projectMappingId) { this.projectMappingId = projectMappingId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getProviderSessionName() { return providerSessionName; }
    public void setProviderSessionName(String providerSessionName) { this.providerSessionName = providerSessionName; }
    public String getMappingStatus() { return mappingStatus; }
    public void setMappingStatus(String mappingStatus) { this.mappingStatus = mappingStatus; }
    public String getSourceSaleStatus() { return sourceSaleStatus; }
    public void setSourceSaleStatus(String sourceSaleStatus) { this.sourceSaleStatus = sourceSaleStatus; }
    public String getSourceDataVersion() { return sourceDataVersion; }
    public void setSourceDataVersion(String sourceDataVersion) { this.sourceDataVersion = sourceDataVersion; }
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
