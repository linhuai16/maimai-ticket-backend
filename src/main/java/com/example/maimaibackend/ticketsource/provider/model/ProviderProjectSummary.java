package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.ProjectStatus;
import java.time.OffsetDateTime;

public record ProviderProjectSummary(
        String projectId,
        String projectName,
        ProviderStatusValue<ProjectStatus> projectStatus,
        String projectType,
        String categoryCode,
        String categoryName,
        String cityCode,
        String cityName,
        ProviderVenue venue,
        String posterUrl,
        String showTimeText,
        OffsetDateTime saleStartAt,
        OffsetDateTime saleEndAt,
        ProviderMoney minPrice,
        ProviderMoney maxPrice,
        boolean hasReservedSeat,
        boolean testData,
        String version,
        OffsetDateTime updatedAt
) {
    public ProviderProjectSummary {
        projectId = ModelSupport.required(projectId, "projectId");
        projectName = ModelSupport.required(projectName, "projectName");
        if (projectStatus == null) throw new IllegalArgumentException("projectStatus不能为空");
        version = ModelSupport.required(version, "version");
        if (updatedAt == null) throw new IllegalArgumentException("updatedAt不能为空");
    }
}
