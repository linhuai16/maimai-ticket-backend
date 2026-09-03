package com.example.maimaibackend.ticketsource.provider.compat;

import com.example.maimaibackend.ticketsource.gateway.TicketSourceAdapter;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceProviderContext;
import com.example.maimaibackend.ticketsource.gateway.model.*;
import com.example.maimaibackend.ticketsource.provider.adapter.*;
import com.example.maimaibackend.ticketsource.provider.enums.*;
import com.example.maimaibackend.ticketsource.provider.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 迁移期只读/单票档兼容桥。
 *
 * <p>该类故意不标记为 Spring Component，不会改变现有网关路由。后续批次可在测试中显式使用。</p>
 */
public final class LegacyTicketSourceV11AdapterBridge implements TicketSourceV11Adapter {
    private final ProviderCode providerCode;
    private final TicketSourceAdapter legacy;

    public LegacyTicketSourceV11AdapterBridge(ProviderCode providerCode, TicketSourceAdapter legacy) {
        if (providerCode == null || legacy == null) throw new IllegalArgumentException("providerCode/legacy不能为空");
        this.providerCode = providerCode;
        this.legacy = legacy;
    }

    @Override public ProviderCode providerCode() { return providerCode; }

    @Override
    public ProviderHealth health(TicketSourceProviderContext context) {
        return LegacyTicketSourceV11Mapper.health(legacy.health(context));
    }

    @Override
    public ProviderCapabilities capabilities(TicketSourceProviderContext context) {
        return new ProviderCapabilities(
                providerCode, providerCode, "1.1-legacy-compat",
                false, false, false, false, false, false,
                true, false, IssueTriggerMode.EXPLICIT_TRIGGER_REQUIRED,
                true, false, null, false, RefundCapabilityScope.FULL_ORDER_ONLY,
                false, false, CallbackCapabilities.none()
        );
    }

    @Override
    public ProviderPage<ProviderProjectSummary> queryProjects(TicketSourceProviderContext context, ProviderProjectQuery query) {
        TicketSourceProjectQuery oldQuery = new TicketSourceProjectQuery();
        oldQuery.setKeyword(query == null ? null : query.keyword());
        oldQuery.setCityName(query == null ? null : query.cityCode());
        oldQuery.setPageNo(query == null ? 1 : query.pageNo());
        oldQuery.setPageSize(query == null ? 20 : query.pageSize());
        TicketSourcePage<TicketSourceProject> page = legacy.queryProjects(context, oldQuery);
        List<ProviderProjectSummary> records = page.getRecords().stream().map(LegacyTicketSourceV11Mapper::project).toList();
        return new ProviderPage<>(records, page.getTotal(), page.getPageNo(), page.getPageSize());
    }

    @Override
    public ProviderProjectDetail getProject(TicketSourceProviderContext context, String projectId) {
        ProviderProjectSummary summary = LegacyTicketSourceV11Mapper.project(legacy.getProject(context, projectId));
        return new ProviderProjectDetail(summary, null, null, null, List.of(), List.of(), List.of(), List.of(), List.of(), null, null);
    }

    @Override
    public List<ProviderSession> querySessions(TicketSourceProviderContext context, String projectId) {
        return legacy.querySessions(context, projectId).stream().map(LegacyTicketSourceV11Mapper::session).toList();
    }

    @Override
    public List<ProviderTicketProduct> queryTicketProducts(TicketSourceProviderContext context, String sessionId) {
        List<ProviderTicketProduct> result = new ArrayList<>();
        for (TicketSourceSku sku : legacy.querySkus(context, sessionId)) {
            result.add(LegacyTicketSourceV11Mapper.ticketProduct(sku, "LEGACY_UNKNOWN_PROJECT"));
        }
        return result;
    }

    @Override
    public ProviderInventory queryInventory(TicketSourceProviderContext context, String ticketProductId) {
        return LegacyTicketSourceV11Mapper.inventory(legacy.queryInventory(context, ticketProductId));
    }

    @Override
    public ProviderOrder createOrder(TicketSourceProviderContext context, ProviderOrderCreateRequest request) {
        TicketSourceCreateOrderRequest old = new TicketSourceCreateOrderRequest();
        old.setClientOrderNo(request.clientOrderNo());
        old.setProviderProjectId(request.projectId());
        old.setProviderSessionId(request.sessionId());
        old.setProviderSkuId(request.ticketProductId());
        old.setQuantity(request.quantity());
        old.setExpectedUnitPrice(request.expectedUnitPrice().toMajor());
        old.setPayAmount(request.expectedPayAmount() == null ? null : request.expectedPayAmount().toMajor());
        old.setCurrencyCode(request.expectedUnitPrice().currency());
        old.setReservationExpireTime(request.reservationExpireAt() == null ? null : request.reservationExpireAt().toLocalDateTime());
        old.setIdempotencyKey(request.idempotencyKey());
        List<TicketSourceOrderAudience> audiences = new ArrayList<>();
        for (ProviderTicketAssignmentRequest assignment : request.tickets()) {
            ProviderPerson holder = request.holders().get(assignment.holderRef());
            audiences.add(new TicketSourceOrderAudience(holder.name(), holder.certificateType(), holder.certificateNo(), holder.phone()));
        }
        old.setAudiences(audiences);
        return LegacyTicketSourceV11Mapper.order(legacy.createOrder(context, old));
    }

    @Override
    public ProviderOrder confirmPayment(TicketSourceProviderContext context, String providerOrderId, ProviderConfirmPaymentRequest request) {
        TicketSourceConfirmPaymentRequest old = new TicketSourceConfirmPaymentRequest();
        old.setClientOrderNo(request.clientOrderNo());
        old.setPayAmount(request.payAmount().toMajor());
        old.setCurrencyCode(request.payAmount().currency());
        old.setPayMethod(request.payMethod());
        old.setPayTime(request.paidAt() == null ? null : request.paidAt().toLocalDateTime());
        old.setIdempotencyKey(request.idempotencyKey());
        return LegacyTicketSourceV11Mapper.order(legacy.confirmPayment(context, providerOrderId, old));
    }

    @Override
    public ProviderOrder cancelOrder(TicketSourceProviderContext context, String providerOrderId, ProviderCancelOrderRequest request) {
        TicketSourceCancelOrderRequest old = new TicketSourceCancelOrderRequest();
        old.setClientOrderNo(request.clientOrderNo());
        old.setReason(request.reason());
        old.setIdempotencyKey(request.idempotencyKey());
        return LegacyTicketSourceV11Mapper.order(legacy.cancelOrder(context, providerOrderId, old));
    }

    @Override
    public ProviderOrder getOrder(TicketSourceProviderContext context, String providerOrderId) {
        return LegacyTicketSourceV11Mapper.order(legacy.getOrder(context, providerOrderId));
    }

    @Override
    public ProviderTicketDelivery triggerFulfillment(TicketSourceProviderContext context, String providerOrderId, ProviderFulfillmentTriggerRequest request) {
        TicketSourceIssueRequest old = new TicketSourceIssueRequest();
        old.setClientOrderNo(request.clientOrderNo());
        old.setExpectedTicketCount(request.expectedTicketCount());
        old.setIdempotencyKey(request.idempotencyKey());
        return LegacyTicketSourceV11Mapper.delivery(legacy.requestTickets(context, providerOrderId, old));
    }

    @Override
    public ProviderTicketDelivery getTickets(TicketSourceProviderContext context, String providerOrderId) {
        return LegacyTicketSourceV11Mapper.delivery(legacy.getTickets(context, providerOrderId));
    }

    @Override
    public ProviderRefund requestRefund(TicketSourceProviderContext context, String providerOrderId, ProviderRefundRequest request) {
        TicketSourceRefundRequest old = new TicketSourceRefundRequest();
        old.setClientRefundNo(request.clientRefundNo());
        old.setRefundAmount(request.expectedRefundAmount() == null ? null : request.expectedRefundAmount().toMajor());
        old.setCurrencyCode(request.expectedRefundAmount() == null ? "CNY" : request.expectedRefundAmount().currency());
        old.setReason(request.reason());
        old.setIdempotencyKey(request.idempotencyKey());
        return LegacyTicketSourceV11Mapper.refund(legacy.requestRefund(context, providerOrderId, old));
    }

    @Override
    public ProviderRefund getRefund(TicketSourceProviderContext context, String providerRefundId) {
        return LegacyTicketSourceV11Mapper.refund(legacy.getRefund(context, providerRefundId));
    }

    private V11AdapterException unsupported(String message) {
        return new V11AdapterException(V11ErrorCode.UNSUPPORTED_OPERATION, "LEGACY_COMPAT_LIMIT", message, false);
    }
}
