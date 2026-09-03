package com.example.maimaibackend.ticketsource.resource.provider.model;

import java.time.LocalDateTime;
import java.util.List;

public class V11ResourceMappingSummary {
    private String providerCode;
    private String providerProjectId;
    private Long projectMappingId;
    private Long projectId;
    private String localProjectStatus;
    private Boolean autoPublishEnabled;
    private String sourceStatus;
    private String sourceStatusValue;
    private String sourceStatusText;
    private String sourceVersion;
    private LocalDateTime sourceUpdatedTime;
    private Integer boundSessionCount;
    private Integer boundTicketProductCount;
    private Integer noticeCount;
    private Integer serviceTagCount;
    private Integer promotionCount;
    private String refundType;
    private LocalDateTime lastSyncTime;
    private String lastSyncStatus;
    private String lastErrorCode;
    private String lastErrorMessage;
    private List<V11ResourceMappingDetail> details;

    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public Long getProjectMappingId() { return projectMappingId; }
    public void setProjectMappingId(Long projectMappingId) { this.projectMappingId = projectMappingId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getLocalProjectStatus() { return localProjectStatus; }
    public void setLocalProjectStatus(String localProjectStatus) { this.localProjectStatus = localProjectStatus; }
    public Boolean getAutoPublishEnabled() { return autoPublishEnabled; }
    public void setAutoPublishEnabled(Boolean autoPublishEnabled) { this.autoPublishEnabled = autoPublishEnabled; }
    public String getSourceStatus() { return sourceStatus; }
    public void setSourceStatus(String sourceStatus) { this.sourceStatus = sourceStatus; }
    public String getSourceStatusValue() { return sourceStatusValue; }
    public void setSourceStatusValue(String sourceStatusValue) { this.sourceStatusValue = sourceStatusValue; }
    public String getSourceStatusText() { return sourceStatusText; }
    public void setSourceStatusText(String sourceStatusText) { this.sourceStatusText = sourceStatusText; }
    public String getSourceVersion() { return sourceVersion; }
    public void setSourceVersion(String sourceVersion) { this.sourceVersion = sourceVersion; }
    public LocalDateTime getSourceUpdatedTime() { return sourceUpdatedTime; }
    public void setSourceUpdatedTime(LocalDateTime sourceUpdatedTime) { this.sourceUpdatedTime = sourceUpdatedTime; }
    public Integer getBoundSessionCount() { return boundSessionCount; }
    public void setBoundSessionCount(Integer boundSessionCount) { this.boundSessionCount = boundSessionCount; }
    public Integer getBoundTicketProductCount() { return boundTicketProductCount; }
    public void setBoundTicketProductCount(Integer boundTicketProductCount) { this.boundTicketProductCount = boundTicketProductCount; }
    public Integer getNoticeCount() { return noticeCount; }
    public void setNoticeCount(Integer noticeCount) { this.noticeCount = noticeCount; }
    public Integer getServiceTagCount() { return serviceTagCount; }
    public void setServiceTagCount(Integer serviceTagCount) { this.serviceTagCount = serviceTagCount; }
    public Integer getPromotionCount() { return promotionCount; }
    public void setPromotionCount(Integer promotionCount) { this.promotionCount = promotionCount; }
    public String getRefundType() { return refundType; }
    public void setRefundType(String refundType) { this.refundType = refundType; }
    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    public String getLastErrorCode() { return lastErrorCode; }
    public void setLastErrorCode(String lastErrorCode) { this.lastErrorCode = lastErrorCode; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
    public List<V11ResourceMappingDetail> getDetails() { return details; }
    public void setDetails(List<V11ResourceMappingDetail> details) { this.details = details; }
}
