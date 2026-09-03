package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.HealthStatus;
import java.time.OffsetDateTime;

public record ProviderHealth(
        HealthStatus status,
        String message,
        OffsetDateTime providerTime,
        OffsetDateTime checkedAt
) {
    public ProviderHealth {
        if (status == null) throw new IllegalArgumentException("health status不能为空");
    }
}
