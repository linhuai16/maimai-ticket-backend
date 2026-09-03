package com.example.maimaibackend.ticketsource.resource.provider.model;

import java.time.LocalDateTime;

public record V11CampaignSyncResult(
        boolean success,
        String providerCode,
        String cityCode,
        int receivedCount,
        int savedCount,
        LocalDateTime syncTime
) {}
