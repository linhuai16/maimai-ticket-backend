package com.example.maimaibackend.ticketsource.provider.adapter;

import com.example.maimaibackend.ticketsource.gateway.TicketSourceProviderContext;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderCode;
import com.example.maimaibackend.ticketsource.provider.model.*;
import java.util.List;

/**
 * V1.1 平台适配器统一边界。当前只允许 MOCK_DAMAI 实现。
 *
 * <p>第一批不替换旧 TicketSourceAdapter；后续批次逐项迁移后再切换网关。</p>
 */
public interface TicketSourceV11Adapter {
    ProviderCode providerCode();

    default String adapterCode() { return providerCode().name(); }

    ProviderHealth health(TicketSourceProviderContext context);
    ProviderCapabilities capabilities(TicketSourceProviderContext context);
    ProviderPage<ProviderProjectSummary> queryProjects(TicketSourceProviderContext context, ProviderProjectQuery query);
    ProviderProjectDetail getProject(TicketSourceProviderContext context, String projectId);
    List<ProviderSession> querySessions(TicketSourceProviderContext context, String projectId);
    List<ProviderTicketProduct> queryTicketProducts(TicketSourceProviderContext context, String sessionId);
    ProviderInventory queryInventory(TicketSourceProviderContext context, String ticketProductId);

    default ProviderVenue getVenue(TicketSourceProviderContext context, String venueId) { throw unsupported("GET_VENUE"); }
    default List<ProviderPromotionRule> queryPromotionRules(TicketSourceProviderContext context, String projectId) { throw unsupported("QUERY_PROMOTION_RULES"); }
    default List<ProviderCampaignAsset> queryCampaignAssets(TicketSourceProviderContext context, String cityCode) { throw unsupported("QUERY_CAMPAIGN_ASSETS"); }
    default ProviderDeliveryQuote quoteDelivery(TicketSourceProviderContext context, ProviderDeliveryQuoteRequest request) { throw unsupported("QUOTE_DELIVERY"); }
    default ProviderOrder createOrder(TicketSourceProviderContext context, ProviderOrderCreateRequest request) { throw unsupported("CREATE_ORDER"); }
    default ProviderOrder confirmPayment(TicketSourceProviderContext context, String providerOrderId, ProviderConfirmPaymentRequest request) { throw unsupported("CONFIRM_PAYMENT"); }
    default ProviderOrder cancelOrder(TicketSourceProviderContext context, String providerOrderId, ProviderCancelOrderRequest request) { throw unsupported("CANCEL_ORDER"); }
    default ProviderOrder getOrder(TicketSourceProviderContext context, String providerOrderId) { throw unsupported("GET_ORDER"); }
    /** R5：createOrder 结果不确定时，按商户订单号/创建幂等键安全补查，禁止再次 create。 */
    default ProviderOrder findOrder(TicketSourceProviderContext context, ProviderOrderLookupRequest request) { throw unsupported("FIND_ORDER"); }
    default ProviderTicketDelivery triggerFulfillment(TicketSourceProviderContext context, String providerOrderId, ProviderFulfillmentTriggerRequest request) { throw unsupported("TRIGGER_FULFILLMENT"); }
    default ProviderTicketDelivery getTickets(TicketSourceProviderContext context, String providerOrderId) { throw unsupported("GET_TICKETS"); }
    default ProviderDynamicCredential refreshDynamicCredential(TicketSourceProviderContext context, String providerTicketId, String currentVersion) { throw unsupported("REFRESH_DYNAMIC_CREDENTIAL"); }
    default ProviderShipment getShipment(TicketSourceProviderContext context, String providerOrderId) { throw unsupported("GET_SHIPMENT"); }
    default ProviderRefundQuote quoteRefund(TicketSourceProviderContext context, String providerOrderId) { throw unsupported("QUOTE_REFUND"); }
    default ProviderRefund requestRefund(TicketSourceProviderContext context, String providerOrderId, ProviderRefundRequest request) { throw unsupported("REQUEST_REFUND"); }
    default ProviderRefund getRefund(TicketSourceProviderContext context, String providerRefundId) { throw unsupported("GET_REFUND"); }

    private V11AdapterException unsupported(String operation) {
        return new V11AdapterException(
                V11ErrorCode.UNSUPPORTED_OPERATION,
                "UNSUPPORTED_" + operation,
                "当前票源适配器不支持操作: " + operation,
                false
        );
    }
}
