package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.ProviderOrderStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record ProviderOrder(
        String providerOrderId,
        String providerOrderNo,
        String clientOrderNo,
        String projectId,
        String sessionId,
        ProviderStatusValue<ProviderOrderStatus> orderStatus,
        ProviderOrderPriceBreakdown price,
        List<ProviderTicketUnit> tickets,
        OffsetDateTime reservationExpireAt,
        OffsetDateTime createdAt,
        OffsetDateTime paidAt,
        OffsetDateTime cancelledAt,
        String version
) {
    public ProviderOrder {
        providerOrderId = ModelSupport.required(providerOrderId, "providerOrderId");
        clientOrderNo = ModelSupport.required(clientOrderNo, "clientOrderNo");
        if (orderStatus == null) throw new IllegalArgumentException("orderStatus不能为空");
        tickets = ModelSupport.list(tickets);
        version = ModelSupport.required(version, "version");
    }
}
