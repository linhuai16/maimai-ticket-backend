package com.example.maimaibackend.ticketsource.provider.model;

import java.util.List;

public record ProviderRefundPolicy(
        String refundType,
        boolean consumerEntryEnabled,
        String feeRuleMode,
        boolean deliveryFeeRefundable,
        String paperTicketReturnRule,
        List<ProviderRefundTier> tiers,
        String sourceRuleText
) {
    public ProviderRefundPolicy { tiers = ModelSupport.list(tiers); }
}
