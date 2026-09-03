package com.example.maimaibackend.ticketsource.sync.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketSourceProjectSyncResult {
    private boolean success;
    private String providerCode;
    private String providerProjectId;
    private Long projectMappingId;
    private Long projectId;
    private boolean created;
    private boolean autoPublishEnabled;
    private String projectStatus;
    private int sessionCount;
    private int skuCount;
    private int inventoryAppliedCount;
    private int inventoryUnknownCount;
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
    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }
    public int getSessionCount() { return sessionCount; }
    public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }
    public int getSkuCount() { return skuCount; }
    public void setSkuCount(int skuCount) { this.skuCount = skuCount; }
    public int getInventoryAppliedCount() { return inventoryAppliedCount; }
    public void setInventoryAppliedCount(int inventoryAppliedCount) { this.inventoryAppliedCount = inventoryAppliedCount; }
    public int getInventoryUnknownCount() { return inventoryUnknownCount; }
    public void setInventoryUnknownCount(int inventoryUnknownCount) { this.inventoryUnknownCount = inventoryUnknownCount; }
    public LocalDateTime getSyncTime() { return syncTime; }
    public void setSyncTime(LocalDateTime syncTime) { this.syncTime = syncTime; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings == null ? new ArrayList<>() : warnings; }
}
