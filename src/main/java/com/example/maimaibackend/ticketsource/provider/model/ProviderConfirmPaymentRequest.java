package com.example.maimaibackend.ticketsource.provider.model;

import java.time.OffsetDateTime;

public record ProviderConfirmPaymentRequest(
        String clientOrderNo,
        ProviderMoney payAmount,
        String payMethod,
        OffsetDateTime paidAt,
        String idempotencyKey
) {
    public ProviderConfirmPaymentRequest {
        clientOrderNo = ModelSupport.required(clientOrderNo, "clientOrderNo");
        if (payAmount == null) throw new IllegalArgumentException("payAmount不能为空");
        idempotencyKey = ModelSupport.required(idempotencyKey, "idempotencyKey");
    }
}
