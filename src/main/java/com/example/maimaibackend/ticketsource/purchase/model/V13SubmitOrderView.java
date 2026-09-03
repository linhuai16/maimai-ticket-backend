package com.example.maimaibackend.ticketsource.purchase.model;

import java.time.LocalDateTime;

/**
 * V1.3 提交订单结果；不得包含 providerOrderId/providerSkuId 等第三方内部字段。
 * priceStatus=CHANGED 时订单尚未创建，orderId/orderNo/reservationExpireAt 为空，
 * ticketAmount/deliveryFee/payAmount 返回本次实时确认后的用户侧金额，供鸿蒙原页二次确认。
 */
public record V13SubmitOrderView(
        Long orderId,
        String orderNo,
        String orderStatus,
        String priceStatus,
        V13MoneyView ticketAmount,
        V13MoneyView deliveryFee,
        V13MoneyView payAmount,
        LocalDateTime reservationExpireAt,
        String paymentButtonText,
        String userMessage
) {}
