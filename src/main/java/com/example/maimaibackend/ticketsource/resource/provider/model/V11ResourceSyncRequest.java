package com.example.maimaibackend.ticketsource.resource.provider.model;

public class V11ResourceSyncRequest {
    private Boolean autoPublish;
    private Boolean syncInventory;
    private Boolean syncCampaignAssets;

    public Boolean getAutoPublish() { return autoPublish; }
    public void setAutoPublish(Boolean autoPublish) { this.autoPublish = autoPublish; }
    public Boolean getSyncInventory() { return syncInventory; }
    public void setSyncInventory(Boolean syncInventory) { this.syncInventory = syncInventory; }
    public Boolean getSyncCampaignAssets() { return syncCampaignAssets; }
    public void setSyncCampaignAssets(Boolean syncCampaignAssets) { this.syncCampaignAssets = syncCampaignAssets; }

    public boolean syncInventoryOrDefault() { return syncInventory == null || Boolean.TRUE.equals(syncInventory); }
    public boolean syncCampaignAssetsOrDefault() { return syncCampaignAssets == null || Boolean.TRUE.equals(syncCampaignAssets); }
}
