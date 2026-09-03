package com.example.maimaibackend.ticketsource.provider.model;

public record CallbackCapabilities(
        boolean resource,
        boolean inventory,
        boolean order,
        boolean ticket,
        boolean refund,
        boolean shipment
) {
    public static CallbackCapabilities none() {
        return new CallbackCapabilities(false, false, false, false, false, false);
    }
}
