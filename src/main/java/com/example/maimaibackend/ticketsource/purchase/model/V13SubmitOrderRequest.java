package com.example.maimaibackend.ticketsource.purchase.model;

import java.util.List;

/** V1.3 用户点击提交订单的唯一正式入口；不接收provider*字段。 */
public record V13SubmitOrderRequest(
        Long userId,
        Long projectId,
        Long sessionId,
        Long skuId,
        Integer quantity,
        List<V13TicketSubmitLine> tickets,
        String fulfillmentOptionCode,
        Long addressId,
        String clientSubmitNo,
        Long expectedPayAmountMinor
) {}
