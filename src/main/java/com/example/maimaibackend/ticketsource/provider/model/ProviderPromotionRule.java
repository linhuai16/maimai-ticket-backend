package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.PromotionType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record ProviderPromotionRule(
        String promotionId,
        PromotionType promotionType,
        String title,
        String description,
        List<String> projectIds,
        List<String> sessionIds,
        List<String> ticketProductIds,
        boolean stackable,
        Map<String, Object> ruleData,
        OffsetDateTime validFrom,
        OffsetDateTime validTo,
        String version,
        OffsetDateTime updatedAt
) {
    public ProviderPromotionRule {
        promotionId = ModelSupport.required(promotionId, "promotionId");
        if (promotionType == null || validFrom == null || validTo == null) throw new IllegalArgumentException("优惠类型和有效期不能为空");
        projectIds = ModelSupport.list(projectIds);
        sessionIds = ModelSupport.list(sessionIds);
        ticketProductIds = ModelSupport.list(ticketProductIds);
        ruleData = ModelSupport.map(ruleData);
    }
}
