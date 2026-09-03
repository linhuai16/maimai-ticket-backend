package com.example.maimaibackend.ticketsource.mock;

import com.example.maimaibackend.ticketsource.gateway.TicketSourceAdapter;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceAdapterException;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceGatewayErrorCode;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceProviderContext;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCancelOrderRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceConfirmPaymentRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCreateOrderRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceHealth;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceDelivery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceIssueRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourcePage;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProjectQuery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProviderOrder;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefund;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefundRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSku;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalMockTicketSourceAdapter implements TicketSourceAdapter {
    public static final String ADAPTER_CODE = "mock-damai";

    private final LocalMockTicketSourceService mockService;

    public LocalMockTicketSourceAdapter(LocalMockTicketSourceService mockService) {
        this.mockService = mockService;
    }

    @Override
    public String adapterCode() { return ADAPTER_CODE; }

    @Override
    public TicketSourceHealth health(TicketSourceProviderContext context) {
        validateContext(context);
        return mockService.health();
    }

    @Override
    public TicketSourcePage<TicketSourceProject> queryProjects(
            TicketSourceProviderContext context,
            TicketSourceProjectQuery query
    ) {
        validateContext(context);
        return mockService.queryProjects(query);
    }

    @Override
    public TicketSourceProject getProject(TicketSourceProviderContext context, String providerProjectId) {
        validateContext(context);
        return mockService.getProject(providerProjectId);
    }

    @Override
    public List<TicketSourceSession> querySessions(TicketSourceProviderContext context, String providerProjectId) {
        validateContext(context);
        return mockService.querySessions(providerProjectId);
    }

    @Override
    public List<TicketSourceSku> querySkus(TicketSourceProviderContext context, String providerSessionId) {
        validateContext(context);
        return mockService.querySkus(providerSessionId);
    }

    @Override
    public TicketSourceInventory queryInventory(TicketSourceProviderContext context, String providerSkuId) {
        validateContext(context);
        return mockService.queryInventory(providerSkuId);
    }

    @Override
    public TicketSourceProviderOrder createOrder(
            TicketSourceProviderContext context,
            TicketSourceCreateOrderRequest request
    ) {
        validateContext(context);
        return mockService.createOrder(request);
    }

    @Override
    public TicketSourceProviderOrder confirmPayment(
            TicketSourceProviderContext context,
            String providerOrderId,
            TicketSourceConfirmPaymentRequest request
    ) {
        validateContext(context);
        return mockService.confirmPayment(providerOrderId, request);
    }

    @Override
    public TicketSourceProviderOrder cancelOrder(
            TicketSourceProviderContext context,
            String providerOrderId,
            TicketSourceCancelOrderRequest request
    ) {
        validateContext(context);
        return mockService.cancelOrder(providerOrderId, request);
    }

    @Override
    public TicketSourceProviderOrder getOrder(TicketSourceProviderContext context, String providerOrderId) {
        validateContext(context);
        return mockService.getOrder(providerOrderId);
    }

    @Override
    public TicketSourceDelivery requestTickets(
            TicketSourceProviderContext context,
            String providerOrderId,
            TicketSourceIssueRequest request
    ) {
        validateContext(context);
        return mockService.requestTickets(providerOrderId, request);
    }

    @Override
    public TicketSourceDelivery getTickets(
            TicketSourceProviderContext context,
            String providerOrderId
    ) {
        validateContext(context);
        return mockService.getTickets(providerOrderId);
    }

    @Override
    public TicketSourceRefund requestRefund(
            TicketSourceProviderContext context,
            String providerOrderId,
            TicketSourceRefundRequest request
    ) {
        validateContext(context);
        return mockService.requestRefund(providerOrderId, request);
    }

    @Override
    public TicketSourceRefund getRefund(
            TicketSourceProviderContext context,
            String providerRefundId
    ) {
        validateContext(context);
        return mockService.getRefund(providerRefundId);
    }

    private void validateContext(TicketSourceProviderContext context) {
        if (context == null || context.getProvider() == null) {
            throw new TicketSourceAdapterException(
                    TicketSourceGatewayErrorCode.INVALID_REQUEST,
                    "MOCK_CONTEXT_MISSING",
                    "模拟票源调用上下文缺失",
                    false
            );
        }
    }
}
