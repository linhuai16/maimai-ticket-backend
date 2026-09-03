package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderCancelOrderRequest(String clientOrderNo, String reason, String idempotencyKey) {
    public ProviderCancelOrderRequest {
        clientOrderNo = ModelSupport.required(clientOrderNo, "clientOrderNo");
        idempotencyKey = ModelSupport.required(idempotencyKey, "idempotencyKey");
    }
}
