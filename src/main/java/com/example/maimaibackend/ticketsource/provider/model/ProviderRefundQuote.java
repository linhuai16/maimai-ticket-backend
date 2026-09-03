package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.RefundScope;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** V1.2 整单退款试算。逐票明细只用于解释/对账，不能用于选择部分退款。 */
public record ProviderRefundQuote(
        String providerOrderId,
        RefundScope refundScope,
        boolean refundable,
        ProviderMoney orderAmount,
        ProviderMoney refundableAmount,
        ProviderMoney serviceFee,
        ProviderMoney refundableDeliveryFee,
        ProviderMoney nonRefundableDeliveryFee,
        ProviderMoney promotionRollbackAmount,
        List<Map<String, Object>> ticketDetails,
        String unavailableReason,
        OffsetDateTime quoteExpireAt,
        String quoteId,
        String version
) {
    public ProviderRefundQuote {
        providerOrderId = ModelSupport.required(providerOrderId, "providerOrderId");
        if (refundScope != RefundScope.FULL_ORDER) throw new IllegalArgumentException("V1.2当前只支持FULL_ORDER整单退款");
        ticketDetails = ModelSupport.list(ticketDetails);
    }

    /** 兼容旧业务代码的命名。 */
    public ProviderMoney feeAmount() { return serviceFee; }
}
