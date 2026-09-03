package com.example.maimaibackend.ticketsource.status;

import java.time.LocalDateTime;

/** V1.3.3 本地展示状态重算结果。 */
public record V13LocalStatusRecalculateResult(
        LocalDateTime recalculatedAt,
        int expiredSessions,
        int expiredSessionSkus,
        int unmappedOrLocalCompatSkus,
        int mappedSkus,
        int sessionsAggregated,
        int projectsAggregated
) {
}
