package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderFulfillmentTriggerRequest(
        String clientOrderNo,
        int expectedTicketCount,
        String idempotencyKey
) {
    public ProviderFulfillmentTriggerRequest {
        clientOrderNo = ModelSupport.required(clientOrderNo, "clientOrderNo");
        if (expectedTicketCount <= 0) throw new IllegalArgumentException("expectedTicketCount必须大于0");
        idempotencyKey = ModelSupport.required(idempotencyKey, "idempotencyKey");
    }
}
