package com.example.maimaibackend.ticketsource.resource.provider.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class V11ResourceSyncResult {
    private boolean success;
    private String providerCode;
    private String providerProjectId;
    private Long projectMappingId;
    private Long projectId;
    private boolean created;
    private boolean autoPublishEnabled;
    private String localProjectStatus;
    private int sessionCount;
    private int ticketProductCount;
    private int inventoryAppliedCount;
    private int inventoryUnknownCount;
    private int noticeCount;
    private int serviceTagCount;
    private int refundTierCount;
    private int promotionCount;
    private int campaignAssetCount;
    private LocalDateTime syncTime;
    private List<String> warnings = new ArrayList<>();

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public Long getProjectMappingId() { return projectMappingId; }
    public void setProjectMappingId(Long projectMappingId) { this.projectMappingId = projectMappingId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public boolean isCreated() { return created; }
    public void setCreated(boolean created) { this.created = created; }
    public boolean isAutoPublishEnabled() { return autoPublishEnabled; }
    public void setAutoPublishEnabled(boolean autoPublishEnabled) { this.autoPublishEnabled = autoPublishEnabled; }
    public String getLocalProjectStatus() { return localProjectStatus; }
    public void setLocalProjectStatus(String localProjectStatus) { this.localProjectStatus = localProjectStatus; }
    public int getSessionCount() { return sessionCount; }
    public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }
    public int getTicketProductCount() { return ticketProductCount; }
    public void setTicketProductCount(int ticketProductCount) { this.ticketProductCount = ticketProductCount; }
    public int getInventoryAppliedCount() { return inventoryAppliedCount; }
    public void setInventoryAppliedCount(int inventoryAppliedCount) { this.inventoryAppliedCount = inventoryAppliedCount; }
    public int getInventoryUnknownCount() { return inventoryUnknownCount; }
    public void setInventoryUnknownCount(int inventoryUnknownCount) { this.inventoryUnknownCount = inventoryUnknownCount; }
    public int getNoticeCount() { return noticeCount; }
    public void setNoticeCount(int noticeCount) { this.noticeCount = noticeCount; }
    public int getServiceTagCount() { return serviceTagCount; }
    public void setServiceTagCount(int serviceTagCount) { this.serviceTagCount = serviceTagCount; }
    public int getRefundTierCount() { return refundTierCount; }
    public void setRefundTierCount(int refundTierCount) { this.refundTierCount = refundTierCount; }
    public int getPromotionCount() { return promotionCount; }
    public void setPromotionCount(int promotionCount) { this.promotionCount = promotionCount; }
    public int getCampaignAssetCount() { return campaignAssetCount; }
    public void setCampaignAssetCount(int campaignAssetCount) { this.campaignAssetCount = campaignAssetCount; }
    public LocalDateTime getSyncTime() { return syncTime; }
    public void setSyncTime(LocalDateTime syncTime) { this.syncTime = syncTime; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings == null ? new ArrayList<>() : warnings; }
}
