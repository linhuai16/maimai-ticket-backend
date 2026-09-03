package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderDeliveryQuoteItem(String ticketProductId, int quantity) {
    public ProviderDeliveryQuoteItem {
        ticketProductId = ModelSupport.required(ticketProductId, "ticketProductId");
        if (quantity <= 0) throw new IllegalArgumentException("quantity必须大于0");
    }
}
