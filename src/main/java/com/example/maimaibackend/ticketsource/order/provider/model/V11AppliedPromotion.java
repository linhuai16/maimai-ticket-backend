package com.example.maimaibackend.ticketsource.order.provider.model;

import java.math.BigDecimal;

public record V11AppliedPromotion(
        String promotionId,
        String promotionType,
        String title,
        BigDecimal discountAmount
) {}
