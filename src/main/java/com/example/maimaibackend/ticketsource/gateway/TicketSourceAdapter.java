package com.example.maimaibackend.ticketsource.gateway;

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
public interface TicketSourceAdapter {
    String adapterCode();

    TicketSourceHealth health(TicketSourceProviderContext context);

    TicketSourcePage<TicketSourceProject> queryProjects(
            TicketSourceProviderContext context,
            TicketSourceProjectQuery query
    );

    TicketSourceProject getProject(TicketSourceProviderContext context, String providerProjectId);

    List<TicketSourceSession> querySessions(TicketSourceProviderContext context, String providerProjectId);

    List<TicketSourceSku> querySkus(TicketSourceProviderContext context, String providerSessionId);

    TicketSourceInventory queryInventory(TicketSourceProviderContext context, String providerSkuId);

    default TicketSourceProviderOrder createOrder(
            TicketSourceProviderContext context,
            TicketSourceCreateOrderRequest request
    ) {
        throw orderOperationNotSupported();
    }

    default TicketSourceProviderOrder confirmPayment(
            TicketSourceProviderContext context,
            String providerOrderId,
            TicketSourceConfirmPaymentRequest request
    ) {
        throw orderOperationNotSupported();
    }

    default TicketSourceProviderOrder cancelOrder(
            TicketSourceProviderContext context,
            String providerOrderId,
            TicketSourceCancelOrderRequest request
    ) {
        throw orderOperationNotSupported();
    }

    default TicketSourceProviderOrder getOrder(TicketSourceProviderContext context, String providerOrderId) {
        throw orderOperationNotSupported();
    }


    default TicketSourceDelivery requestTickets(
            TicketSourceProviderContext context,
            String providerOrderId,
            TicketSourceIssueRequest request
    ) {
        throw issueOperationNotSupported();
    }

    default TicketSourceDelivery getTickets(
            TicketSourceProviderContext context,
            String providerOrderId
    ) {
        throw issueOperationNotSupported();
    }

    default TicketSourceRefund requestRefund(
            TicketSourceProviderContext context,
            String providerOrderId,
            TicketSourceRefundRequest request
    ) {
        throw refundOperationNotSupported();
    }

    default TicketSourceRefund getRefund(
            TicketSourceProviderContext context,
            String providerRefundId
    ) {
        throw refundOperationNotSupported();
    }

    private TicketSourceAdapterException refundOperationNotSupported() {
        return new TicketSourceAdapterException(
                TicketSourceGatewayErrorCode.REMOTE_ERROR,
                "REFUND_OPERATION_NOT_SUPPORTED",
                "当前票源适配器尚未实现退款操作",
                false
        );
    }

    private TicketSourceAdapterException issueOperationNotSupported() {
        return new TicketSourceAdapterException(
                TicketSourceGatewayErrorCode.REMOTE_ERROR,
                "ISSUE_OPERATION_NOT_SUPPORTED",
                "当前票源适配器尚未实现出票操作",
                false
        );
    }

    private TicketSourceAdapterException orderOperationNotSupported() {
        return new TicketSourceAdapterException(
                TicketSourceGatewayErrorCode.REMOTE_ERROR,
                "ORDER_OPERATION_NOT_SUPPORTED",
                "当前票源适配器尚未实现订单操作",
                false
        );
    }
}
