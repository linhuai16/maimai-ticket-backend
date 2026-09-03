package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderTicketAssignmentRequest(
        String clientTicketNo,
        String holderRef
) {
    public ProviderTicketAssignmentRequest {
        clientTicketNo = ModelSupport.required(clientTicketNo, "clientTicketNo");
        holderRef = ModelSupport.required(holderRef, "holderRef");
    }
}
