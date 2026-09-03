package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderSeatAssignment(
        String floor,
        String stand,
        String zone,
        String row,
        String seat,
        String fullText,
        String entrance
) {}
