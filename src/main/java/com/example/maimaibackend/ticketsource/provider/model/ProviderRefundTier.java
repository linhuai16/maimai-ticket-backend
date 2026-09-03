package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderRefundTier(
        Long startOffsetMinutes,
        Long endOffsetMinutes,
        String result,
        String feePercent,
        ProviderMoney feeFixed
) {}
