package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.CampaignAssetType;
import com.example.maimaibackend.ticketsource.provider.enums.CampaignTargetType;
import java.time.OffsetDateTime;
import java.util.List;

public record ProviderCampaignAsset(
        String assetId,
        CampaignAssetType assetType,
        String position,
        String title,
        String description,
        String imageUrl,
        String mobileImageUrl,
        CampaignTargetType targetType,
        String targetValue,
        List<String> cityCodes,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String providerPromotionId,
        boolean requiresLocalApproval,
        String version,
        OffsetDateTime updatedAt
) {
    public ProviderCampaignAsset {
        assetId = ModelSupport.required(assetId, "assetId");
        if (assetType == null || targetType == null) throw new IllegalArgumentException("素材类型和跳转类型不能为空");
        title = ModelSupport.required(title, "title");
        cityCodes = ModelSupport.list(cityCodes);
        if (!requiresLocalApproval) throw new IllegalArgumentException("第三方活动素材必须进入本地待审核池");
    }
}
