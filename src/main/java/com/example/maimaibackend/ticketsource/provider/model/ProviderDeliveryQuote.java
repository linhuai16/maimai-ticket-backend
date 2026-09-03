package com.example.maimaibackend.ticketsource.provider.model;

import java.time.OffsetDateTime;

public record ProviderDeliveryQuote(
        boolean deliveryAvailable,
        ProviderMoney deliveryFee,
        String quoteId,
        OffsetDateTime expiresAt,
        OffsetDateTime estimatedShipAt,
        OffsetDateTime estimatedArrivalAt,
        String unavailableReason
) {
    public ProviderDeliveryQuote {
        quoteId = ModelSupport.required(quoteId, "quoteId");
        if (deliveryFee == null || expiresAt == null) throw new IllegalArgumentException("运费和报价过期时间不能为空");
    }
}
