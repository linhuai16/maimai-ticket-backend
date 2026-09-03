package com.example.maimaibackend.ticketsource.resource.provider.model;

import com.example.maimaibackend.ticketsource.provider.model.*;
import java.util.ArrayList;
import java.util.List;

public class V11ResourceBundle {
    private ProviderCapabilities capabilities;
    private ProviderProjectDetail project;
    private ProviderVenue venue;
    private List<SessionBundle> sessions = new ArrayList<>();
    private List<ProviderPromotionRule> promotionRules = new ArrayList<>();
    private List<ProviderCampaignAsset> campaignAssets = new ArrayList<>();
    private boolean inventoryQueryFailed;

    public ProviderCapabilities getCapabilities() { return capabilities; }
    public void setCapabilities(ProviderCapabilities capabilities) { this.capabilities = capabilities; }
    public ProviderProjectDetail getProject() { return project; }
    public void setProject(ProviderProjectDetail project) { this.project = project; }
    public ProviderVenue getVenue() { return venue; }
    public void setVenue(ProviderVenue venue) { this.venue = venue; }
    public List<SessionBundle> getSessions() { return sessions; }
    public void setSessions(List<SessionBundle> sessions) { this.sessions = sessions == null ? new ArrayList<>() : sessions; }
    public List<ProviderPromotionRule> getPromotionRules() { return promotionRules; }
    public void setPromotionRules(List<ProviderPromotionRule> promotionRules) { this.promotionRules = promotionRules == null ? new ArrayList<>() : promotionRules; }
    public List<ProviderCampaignAsset> getCampaignAssets() { return campaignAssets; }
    public void setCampaignAssets(List<ProviderCampaignAsset> campaignAssets) { this.campaignAssets = campaignAssets == null ? new ArrayList<>() : campaignAssets; }
    public boolean isInventoryQueryFailed() { return inventoryQueryFailed; }
    public void setInventoryQueryFailed(boolean inventoryQueryFailed) { this.inventoryQueryFailed = inventoryQueryFailed; }
    public int sessionCount() { return sessions.size(); }
    public int ticketProductCount() { return sessions.stream().mapToInt(s -> s.ticketProducts.size()).sum(); }

    public static class SessionBundle {
        private ProviderSession session;
        private List<ProviderTicketProduct> ticketProducts = new ArrayList<>();
        private List<ProviderInventory> inventories = new ArrayList<>();
        public ProviderSession getSession() { return session; }
        public void setSession(ProviderSession session) { this.session = session; }
        public List<ProviderTicketProduct> getTicketProducts() { return ticketProducts; }
        public void setTicketProducts(List<ProviderTicketProduct> ticketProducts) { this.ticketProducts = ticketProducts == null ? new ArrayList<>() : ticketProducts; }
        public List<ProviderInventory> getInventories() { return inventories; }
        public void setInventories(List<ProviderInventory> inventories) { this.inventories = inventories == null ? new ArrayList<>() : inventories; }
        public ProviderInventory inventory(String ticketProductId) {
            return inventories.stream().filter(i -> i.ticketProductId().equals(ticketProductId)).findFirst().orElse(null);
        }
    }
}
