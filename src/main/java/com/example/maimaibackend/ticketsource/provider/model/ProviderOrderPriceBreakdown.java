package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderOrderPriceBreakdown(
        ProviderMoney faceAmount,
        ProviderMoney providerSaleAmount,
        ProviderMoney settlementAmount,
        ProviderMoney providerDiscountAmount,
        ProviderMoney deliveryFee,
        ProviderMoney serviceFee,
        ProviderMoney totalAmount,
        ProviderMoney payAmount
) {}
