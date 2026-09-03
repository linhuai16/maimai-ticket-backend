package com.example.maimaibackend.ticketsource.provider.mock.dto;

import com.example.maimaibackend.ticketsource.provider.enums.CallbackEventType;

public record MockV11CallbackRequest(
        CallbackEventType eventType,
        String resourceType,
        String providerResourceId,
        String version
) {}
