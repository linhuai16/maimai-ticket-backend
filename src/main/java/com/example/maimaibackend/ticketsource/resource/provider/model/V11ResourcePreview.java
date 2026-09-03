package com.example.maimaibackend.ticketsource.resource.provider.model;

import com.example.maimaibackend.ticketsource.provider.model.ProviderCapabilities;
import com.example.maimaibackend.ticketsource.provider.model.ProviderProjectDetail;
import java.util.ArrayList;
import java.util.List;

public class V11ResourcePreview {
    private String providerCode;
    private ProviderCapabilities capabilities;
    private ProviderProjectDetail project;
    private int sessionCount;
    private int ticketProductCount;
    private int noticeCount;
    private int capabilityCount;
    private int refundTierCount;
    private int promotionCount;
    private int campaignAssetCount;
    private boolean alreadyBound;
    private Long localProjectId;
    private Boolean autoPublishEnabled;
    private List<String> warnings = new ArrayList<>();

    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public ProviderCapabilities getCapabilities() { return capabilities; }
    public void setCapabilities(ProviderCapabilities capabilities) { this.capabilities = capabilities; }
    public ProviderProjectDetail getProject() { return project; }
    public void setProject(ProviderProjectDetail project) { this.project = project; }
    public int getSessionCount() { return sessionCount; }
    public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }
    public int getTicketProductCount() { return ticketProductCount; }
    public void setTicketProductCount(int ticketProductCount) { this.ticketProductCount = ticketProductCount; }
    public int getNoticeCount() { return noticeCount; }
    public void setNoticeCount(int noticeCount) { this.noticeCount = noticeCount; }
    public int getCapabilityCount() { return capabilityCount; }
    public void setCapabilityCount(int capabilityCount) { this.capabilityCount = capabilityCount; }
    public int getRefundTierCount() { return refundTierCount; }
    public void setRefundTierCount(int refundTierCount) { this.refundTierCount = refundTierCount; }
    public int getPromotionCount() { return promotionCount; }
    public void setPromotionCount(int promotionCount) { this.promotionCount = promotionCount; }
    public int getCampaignAssetCount() { return campaignAssetCount; }
    public void setCampaignAssetCount(int campaignAssetCount) { this.campaignAssetCount = campaignAssetCount; }
    public boolean isAlreadyBound() { return alreadyBound; }
    public void setAlreadyBound(boolean alreadyBound) { this.alreadyBound = alreadyBound; }
    public Long getLocalProjectId() { return localProjectId; }
    public void setLocalProjectId(Long localProjectId) { this.localProjectId = localProjectId; }
    public Boolean getAutoPublishEnabled() { return autoPublishEnabled; }
    public void setAutoPublishEnabled(Boolean autoPublishEnabled) { this.autoPublishEnabled = autoPublishEnabled; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings == null ? new ArrayList<>() : warnings; }
}
