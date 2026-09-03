package com.example.maimaibackend.ticketsource.user.model;

import com.example.maimaibackend.ticketsource.provider.model.ProviderMoney;
import java.time.OffsetDateTime;

/** 用户侧整单退款试算；不返回 providerOrderId/provider quoteId/version/逐票第三方明细。 */
public record V12UserRefundQuoteView(
        String refundScope,
        boolean refundable,
        ProviderMoney orderAmount,
        ProviderMoney refundableAmount,
        ProviderMoney serviceFee,
        ProviderMoney refundableDeliveryFee,
        ProviderMoney nonRefundableDeliveryFee,
        ProviderMoney promotionRollbackAmount,
        String unavailableReason,
        OffsetDateTime quoteExpireAt
) {}
