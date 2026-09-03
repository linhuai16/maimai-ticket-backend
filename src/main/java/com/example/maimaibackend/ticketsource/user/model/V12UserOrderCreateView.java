package com.example.maimaibackend.ticketsource.user.model;

import com.example.maimaibackend.ticketsource.provider.model.ProviderMoney;
import java.time.LocalDateTime;

public record V12UserOrderCreateView(
        Long orderId,
        String orderNo,
        String orderStatus,
        String paymentStatus,
        String fulfillmentMode,
        int ticketCount,
        ProviderMoney payAmount,
        LocalDateTime payExpireTime
) {}
