package com.example.maimaibackend.ticketsource.order.provider.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record V11OrderCreateResult(
        Long orderId,
        String orderNo,
        String orderStatus,
        String paymentStatus,
        String fulfillmentMode,
        String providerCode,
        String providerOrderId,
        String providerOrderStatus,
        int itemCount,
        int ticketCount,
        BigDecimal payAmount,
        LocalDateTime payExpireTime
) {}
