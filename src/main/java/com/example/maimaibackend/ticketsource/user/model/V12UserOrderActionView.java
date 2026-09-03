package com.example.maimaibackend.ticketsource.user.model;

import java.time.LocalDateTime;

public record V12UserOrderActionView(
        Long orderId,
        String orderNo,
        String orderStatus,
        String paymentStatus,
        int ticketCount,
        LocalDateTime operationTime,
        String message
) {}
