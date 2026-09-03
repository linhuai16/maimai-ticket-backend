package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.ProviderRefundStatus;
import java.time.OffsetDateTime;

public record ProviderRefund(
        String providerRefundId,
        String providerRefundNo,
        String providerOrderId,
        String clientRefundNo,
        ProviderStatusValue<ProviderRefundStatus> refundStatus,
        ProviderMoney refundAmount,
        ProviderMoney feeAmount,
        ProviderMoney refundedDeliveryFee,
        OffsetDateTime nextPollAt,
        OffsetDateTime refundedAt,
        String errorCode,
        String errorMessage,
        String version
) {
    public ProviderRefund {
        providerRefundId = ModelSupport.required(providerRefundId, "providerRefundId");
        if (refundStatus == null) throw new IllegalArgumentException("refundStatus不能为空");
    }
}
