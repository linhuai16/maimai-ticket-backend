package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderTicketUnit(
        String clientTicketNo,
        String holderRef,
        String ticketProductId,
        String providerSubOrderId,
        String providerTicketId
) {}
