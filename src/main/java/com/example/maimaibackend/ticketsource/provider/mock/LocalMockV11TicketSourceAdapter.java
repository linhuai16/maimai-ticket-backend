package com.example.maimaibackend.ticketsource.provider.mock;

import com.example.maimaibackend.ticketsource.gateway.TicketSourceProviderContext;
import com.example.maimaibackend.ticketsource.provider.adapter.TicketSourceV11Adapter;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderCode;
import com.example.maimaibackend.ticketsource.provider.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/** V1.1 MOCK_DAMAI Adapter；不再通过旧单票档模型降级。 */
@Component
public class LocalMockV11TicketSourceAdapter implements TicketSourceV11Adapter {
    private final LocalMockV11TicketSourceService service;

    public LocalMockV11TicketSourceAdapter(LocalMockV11TicketSourceService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    @Override public ProviderCode providerCode() { return ProviderCode.MOCK_DAMAI; }
    @Override public ProviderHealth health(TicketSourceProviderContext context) { return service.health(); }
    @Override public ProviderCapabilities capabilities(TicketSourceProviderContext context) { return service.capabilities(); }
    @Override public ProviderPage<ProviderProjectSummary> queryProjects(TicketSourceProviderContext context, ProviderProjectQuery query) { return service.queryProjects(query); }
    @Override public ProviderProjectDetail getProject(TicketSourceProviderContext context, String projectId) { return service.getProject(projectId); }
    @Override public List<ProviderSession> querySessions(TicketSourceProviderContext context, String projectId) { return service.querySessions(projectId); }
    @Override public List<ProviderTicketProduct> queryTicketProducts(TicketSourceProviderContext context, String sessionId) { return service.queryTicketProducts(sessionId); }
    @Override public ProviderInventory queryInventory(TicketSourceProviderContext context, String ticketProductId) { return service.queryInventory(ticketProductId); }
    @Override public ProviderVenue getVenue(TicketSourceProviderContext context, String venueId) { return service.getVenue(venueId); }
    @Override public List<ProviderPromotionRule> queryPromotionRules(TicketSourceProviderContext context, String projectId) { return service.queryPromotionRules(projectId); }
    @Override public List<ProviderCampaignAsset> queryCampaignAssets(TicketSourceProviderContext context, String cityCode) { return service.queryCampaignAssets(cityCode); }
    @Override public ProviderDeliveryQuote quoteDelivery(TicketSourceProviderContext context, ProviderDeliveryQuoteRequest request) { return service.quoteDelivery(request); }
    @Override public ProviderOrder createOrder(TicketSourceProviderContext context, ProviderOrderCreateRequest request) { return service.createOrder(request); }
    @Override public ProviderOrder confirmPayment(TicketSourceProviderContext context, String providerOrderId, ProviderConfirmPaymentRequest request) { return service.confirmPayment(providerOrderId, request); }
    @Override public ProviderOrder cancelOrder(TicketSourceProviderContext context, String providerOrderId, ProviderCancelOrderRequest request) { return service.cancelOrder(providerOrderId, request); }
    @Override public ProviderOrder getOrder(TicketSourceProviderContext context, String providerOrderId) { return service.getOrder(providerOrderId); }
    @Override public ProviderOrder findOrder(TicketSourceProviderContext context, ProviderOrderLookupRequest request) { return service.findOrder(request); }
    @Override public ProviderTicketDelivery triggerFulfillment(TicketSourceProviderContext context, String providerOrderId, ProviderFulfillmentTriggerRequest request) { return service.triggerFulfillment(providerOrderId, request); }
    @Override public ProviderTicketDelivery getTickets(TicketSourceProviderContext context, String providerOrderId) { return service.getTickets(providerOrderId); }
    @Override public ProviderDynamicCredential refreshDynamicCredential(TicketSourceProviderContext context, String providerTicketId, String currentVersion) { return service.refreshDynamicCredential(providerTicketId, currentVersion); }
    @Override public ProviderShipment getShipment(TicketSourceProviderContext context, String providerOrderId) { return service.getShipment(providerOrderId); }
    @Override public ProviderRefundQuote quoteRefund(TicketSourceProviderContext context, String providerOrderId) { return service.quoteRefund(providerOrderId); }
    @Override public ProviderRefund requestRefund(TicketSourceProviderContext context, String providerOrderId, ProviderRefundRequest request) { return service.requestRefund(providerOrderId, request); }
    @Override public ProviderRefund getRefund(TicketSourceProviderContext context, String providerRefundId) { return service.getRefund(providerRefundId); }
}
