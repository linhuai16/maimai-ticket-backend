package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.ProjectStatus;
import com.example.maimaibackend.ticketsource.provider.enums.SeatMode;
import com.example.maimaibackend.ticketsource.provider.enums.SessionType;
import java.time.OffsetDateTime;
import java.util.List;

public record ProviderSession(
        String sessionId,
        String projectId,
        String sessionName,
        ProviderStatusValue<ProjectStatus> sessionStatus,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        OffsetDateTime saleStartAt,
        OffsetDateTime saleEndAt,
        SessionType sessionType,
        SeatMode seatMode,
        boolean timeChanged,
        String changeReason,
        String remark,
        List<Integer> weeklyAvailability,
        Integer maxQuantityPerOrder,
        String realNameMode,
        String issueMethod,
        String pickupMethod,
        String version,
        OffsetDateTime updatedAt
) {
    public ProviderSession {
        sessionId = ModelSupport.required(sessionId, "sessionId");
        projectId = ModelSupport.required(projectId, "projectId");
        sessionName = ModelSupport.required(sessionName, "sessionName");
        if (sessionStatus == null) throw new IllegalArgumentException("sessionStatus不能为空");
        sessionType = sessionType == null ? SessionType.SINGLE : sessionType;
        seatMode = seatMode == null ? SeatMode.GENERAL_ADMISSION : seatMode;
        weeklyAvailability = ModelSupport.list(weeklyAvailability);
        version = ModelSupport.required(version, "version");
        if (updatedAt == null) throw new IllegalArgumentException("updatedAt不能为空");
    }
}
