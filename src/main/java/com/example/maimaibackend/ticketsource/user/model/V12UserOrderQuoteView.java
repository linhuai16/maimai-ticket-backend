package com.example.maimaibackend.ticketsource.user.model;

import com.example.maimaibackend.ticketsource.provider.model.ProviderMoney;
import java.time.LocalDateTime;
import java.util.List;

/** 麦麦用户侧 V1.2 单票档计价视图；不暴露 provider* 标识和第三方结算成本。 */
public record V12UserOrderQuoteView(
        String quoteId,
        Long userId,
        Long projectId,
        Long sessionId,
        String purchaseMode,
        String ticketMode,
        String deliveryMode,
        Long addressId,
        Item item,
        List<Promotion> promotions,
        int totalTicketCount,
        ProviderMoney faceAmount,
        ProviderMoney ticketAmount,
        ProviderMoney discountAmount,
        ProviderMoney deliveryFeeAmount,
        ProviderMoney serviceFeeAmount,
        ProviderMoney payAmount,
        LocalDateTime expireTime,
        List<String> warnings
) {
    public record Ticket(String clientTicketNo, Long audienceId) {}
    public record Item(
            Long skuId,
            String skuName,
            int quantity,
            ProviderMoney faceUnitPrice,
            ProviderMoney saleUnitPrice,
            ProviderMoney subtotalAmount,
            List<Ticket> tickets
    ) {}
    public record Promotion(String promotionType, String title, ProviderMoney discountAmount) {}
}
