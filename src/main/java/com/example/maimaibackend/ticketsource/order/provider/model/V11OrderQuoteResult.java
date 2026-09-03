package com.example.maimaibackend.ticketsource.order.provider.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record V11OrderQuoteResult(
        String quoteId,
        Long userId,
        Long projectId,
        Long sessionId,
        String providerCode,
        String providerProjectId,
        String providerSessionId,
        String purchaseMode,
        String ticketMode,
        String deliveryMode,
        Long addressId,
        V11OrderQuoteItem item,
        List<V11AppliedPromotion> promotions,
        int totalTicketCount,
        BigDecimal faceAmount,
        BigDecimal ticketAmount,
        BigDecimal settlementAmount,
        BigDecimal discountAmount,
        BigDecimal deliveryFeeAmount,
        BigDecimal serviceFeeAmount,
        BigDecimal payAmount,
        String providerDeliveryQuoteId,
        LocalDateTime expireTime,
        List<String> warnings
) {}
