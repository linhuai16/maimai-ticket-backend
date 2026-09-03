package com.example.maimaibackend.ai.domain.context;

import java.math.BigDecimal;

public record AiSearchContext(
        String city,
        String category,
        String keyword,
        String venue,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String startTime,
        String endTime,
        String timeIntent,
        String sort
) {
}
