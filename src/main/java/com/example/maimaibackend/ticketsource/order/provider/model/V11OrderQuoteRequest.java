package com.example.maimaibackend.ticketsource.order.provider.model;

import java.util.List;

/**
 * V1.1 下单前第三方计价请求。
 * 一个订单只选择一个场次下的一个票档，票数由 tickets.size() 决定。
 */
public record V11OrderQuoteRequest(
        Long userId,
        Long projectId,
        Long sessionId,
        Long skuId,
        List<V11TicketSelection> tickets,
        String purchaseMode,
        String ticketMode,
        String deliveryMode,
        Long addressId
) {}
