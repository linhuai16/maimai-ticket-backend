package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.TicketDeliveryStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record ProviderTicketDelivery(
        String providerOrderId,
        ProviderStatusValue<TicketDeliveryStatus> deliveryStatus,
        int expectedTicketCount,
        int issuedCount,
        int failedCount,
        OffsetDateTime nextPollAt,
        List<ProviderTicketCredential> tickets,
        String version
) {
    public ProviderTicketDelivery {
        providerOrderId = ModelSupport.required(providerOrderId, "providerOrderId");
        if (deliveryStatus == null) throw new IllegalArgumentException("deliveryStatus不能为空");
        if (expectedTicketCount < 0 || issuedCount < 0 || failedCount < 0) throw new IllegalArgumentException("票数量不能为负数");
        tickets = ModelSupport.list(tickets);
    }
}
