package com.example.maimaibackend.ai.domain.context;

import java.util.List;

public record AiEntityContext(
        Long mentionedProjectId,
        List<Long> comparedProjectIds,
        String city,
        String venue,
        Long venueId
) {
    public static AiEntityContext empty() {
        return new AiEntityContext(null, List.of(), "", "", null);
    }
}
