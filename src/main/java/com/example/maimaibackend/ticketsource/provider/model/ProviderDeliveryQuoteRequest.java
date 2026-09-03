package com.example.maimaibackend.ticketsource.provider.model;

import java.util.List;

public record ProviderDeliveryQuoteRequest(
        String projectId,
        String sessionId,
        List<ProviderDeliveryQuoteItem> items,
        ProviderAddress address
) {
    public ProviderDeliveryQuoteRequest {
        projectId = ModelSupport.required(projectId, "projectId");
        sessionId = ModelSupport.required(sessionId, "sessionId");
        items = ModelSupport.list(items);
        if (items.size() != 1 || address == null) throw new IllegalArgumentException("V1.2运费试算必须且只能包含一个票档和一个地址");
    }
}
