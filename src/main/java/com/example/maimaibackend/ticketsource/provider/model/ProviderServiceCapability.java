package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderServiceCapability(
        String capabilityCode,
        boolean enabled,
        String displayText,
        String sourceCode
) {}
