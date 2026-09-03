package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.CallbackEventType;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderCode;
import java.time.OffsetDateTime;

public record ProviderCallbackEvent(
        String eventId,
        CallbackEventType eventType,
        ProviderCode providerCode,
        String resourceType,
        String providerResourceId,
        String version,
        OffsetDateTime occurredAt
) {
    public ProviderCallbackEvent {
        eventId = ModelSupport.required(eventId, "eventId");
        if (eventType == null || providerCode == null) throw new IllegalArgumentException("eventType/providerCode不能为空");
        resourceType = ModelSupport.required(resourceType, "resourceType");
        providerResourceId = ModelSupport.required(providerResourceId, "providerResourceId");
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt不能为空");
    }
}
