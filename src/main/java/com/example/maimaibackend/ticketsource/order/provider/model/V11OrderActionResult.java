package com.example.maimaibackend.ticketsource.order.provider.model;

import java.time.LocalDateTime;

public record V11OrderActionResult(
        Long orderId,
        String orderNo,
        String orderStatus,
        String paymentStatus,
        String bridgeStatus,
        String providerOrderId,
        String providerOrderStatus,
        int ticketCount,
        LocalDateTime operationTime,
        String message
) {}
