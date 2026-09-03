package com.example.maimaibackend.ticketsource.sync.model;

import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;

import java.util.ArrayList;
import java.util.List;

public class TicketSourceResourcePreview {
    private String providerCode;
    private TicketSourceProject project;
    private Integer sessionCount;
    private Integer skuCount;
    private Boolean alreadyBound;
    private Long localProjectId;
    private Boolean autoPublishEnabled;
    private List<String> warnings = new ArrayList<>();

    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public TicketSourceProject getProject() { return project; }
    public void setProject(TicketSourceProject project) { this.project = project; }
    public Integer getSessionCount() { return sessionCount; }
    public void setSessionCount(Integer sessionCount) { this.sessionCount = sessionCount; }
    public Integer getSkuCount() { return skuCount; }
    public void setSkuCount(Integer skuCount) { this.skuCount = skuCount; }
    public Boolean getAlreadyBound() { return alreadyBound; }
    public void setAlreadyBound(Boolean alreadyBound) { this.alreadyBound = alreadyBound; }
    public Long getLocalProjectId() { return localProjectId; }
    public void setLocalProjectId(Long localProjectId) { this.localProjectId = localProjectId; }
    public Boolean getAutoPublishEnabled() { return autoPublishEnabled; }
    public void setAutoPublishEnabled(Boolean autoPublishEnabled) { this.autoPublishEnabled = autoPublishEnabled; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings == null ? new ArrayList<>() : warnings; }
}
