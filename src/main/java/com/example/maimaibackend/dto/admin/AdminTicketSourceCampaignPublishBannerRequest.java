package com.example.maimaibackend.dto.admin;

import java.time.LocalDateTime;

public record AdminTicketSourceCampaignPublishBannerRequest(
        String bannerTitle,
        String imageUrl,
        Long targetProjectId,
        Long targetSessionId,
        String enableStatus,
        Integer sortOrder,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
