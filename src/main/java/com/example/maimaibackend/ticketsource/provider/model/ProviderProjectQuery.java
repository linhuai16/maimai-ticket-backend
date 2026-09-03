package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.ProjectStatus;
import java.time.OffsetDateTime;

public record ProviderProjectQuery(
        String keyword,
        String cityCode,
        ProjectStatus status,
        OffsetDateTime updatedAfter,
        int pageNo,
        int pageSize
) {
    public ProviderProjectQuery {
        pageNo = Math.max(1, pageNo);
        pageSize = Math.min(100, Math.max(1, pageSize));
        keyword = normalize(keyword);
        cityCode = normalize(cityCode);
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
