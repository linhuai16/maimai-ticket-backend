package com.example.maimaibackend.ticketsource.gateway;

import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCallResult;
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

import java.util.List;

/**
 * @deprecated V1.1迁移兼容接口。新平台适配器应实现 ticketsource.v11.adapter.TicketSourceV11Adapter。
 */
@Deprecated(forRemoval = false)
public interface TicketSourceGateway {
    TicketSourceCallResult<TicketSourceHealth> health(String providerCode);

    TicketSourceCallResult<TicketSourcePage<TicketSourceProject>> queryProjects(
            String providerCode,
            TicketSourceProjectQuery query
    );

    TicketSourceCallResult<TicketSourceProject> getProject(String providerCode, String providerProjectId);

    TicketSourceCallResult<List<TicketSourceSession>> querySessions(String providerCode, String providerProjectId);

    TicketSourceCallResult<List<TicketSourceSku>> querySkus(String providerCode, String providerSessionId);

    TicketSourceCallResult<TicketSourceInventory> queryInventory(String providerCode, String providerSkuId);

    TicketSourceCallResult<TicketSourceProviderOrder> createOrder(
            String providerCode,
            TicketSourceCreateOrderRequest request
    );

    TicketSourceCallResult<TicketSourceProviderOrder> confirmPayment(
            String providerCode,
            String providerOrderId,
            TicketSourceConfirmPaymentRequest request
    );

    TicketSourceCallResult<TicketSourceProviderOrder> cancelOrder(
            String providerCode,
            String providerOrderId,
            TicketSourceCancelOrderRequest request
    );

    TicketSourceCallResult<TicketSourceProviderOrder> getOrder(
            String providerCode,
            String providerOrderId
    );

    TicketSourceCallResult<TicketSourceDelivery> requestTickets(
            String providerCode,
            String providerOrderId,
            TicketSourceIssueRequest request
    );

    TicketSourceCallResult<TicketSourceDelivery> getTickets(
            String providerCode,
            String providerOrderId
    );

    TicketSourceCallResult<TicketSourceRefund> requestRefund(
            String providerCode,
            String providerOrderId,
            TicketSourceRefundRequest request
    );

    TicketSourceCallResult<TicketSourceRefund> getRefund(
            String providerCode,
            String providerRefundId
    );
}
