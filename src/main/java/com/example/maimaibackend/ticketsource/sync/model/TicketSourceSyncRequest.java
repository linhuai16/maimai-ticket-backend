package com.example.maimaibackend.ticketsource.sync.model;

public class TicketSourceSyncRequest {
    private Boolean autoPublish;
    private Boolean syncInventory;

    public Boolean getAutoPublish() { return autoPublish; }
    public void setAutoPublish(Boolean autoPublish) { this.autoPublish = autoPublish; }
    public Boolean getSyncInventory() { return syncInventory; }
    public void setSyncInventory(Boolean syncInventory) { this.syncInventory = syncInventory; }

    public boolean autoPublishOrDefault() {
        return Boolean.TRUE.equals(autoPublish);
    }

    public boolean syncInventoryOrDefault() {
        return syncInventory == null || Boolean.TRUE.equals(syncInventory);
    }
}
