package com.example.maimaibackend.ticketsource.resource.provider;

import com.example.maimaibackend.mapper.ticketsource.V11ResourceSyncMapper;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProvider;
import com.example.maimaibackend.ticketsource.sync.TicketSourceResourceSyncWriter;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceProjectSyncResult;
import com.example.maimaibackend.ticketsource.provider.model.*;
import com.example.maimaibackend.ticketsource.resource.provider.model.V11ResourceBundle;
import com.example.maimaibackend.ticketsource.resource.provider.model.V11ResourceSyncResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
public class V11ResourceSyncWriter {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final TicketSourceResourceSyncWriter legacyWriter;
    private final V11ResourceSyncMapper mapper;
    private final ObjectMapper objectMapper;
    private final V11ProviderContentSanitizer contentSanitizer;

    public V11ResourceSyncWriter(TicketSourceResourceSyncWriter legacyWriter,
                                 V11ResourceSyncMapper mapper,
                                 ObjectMapper objectMapper,
                                 V11ProviderContentSanitizer contentSanitizer) {
        this.legacyWriter = legacyWriter;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.contentSanitizer = contentSanitizer;
    }

    @Transactional(rollbackFor = Exception.class)
    public V11ResourceSyncResult synchronize(TicketSourceProvider provider,
                                               V11ResourceBundle bundle,
                                               boolean autoPublish,
                                               boolean syncInventory) {
        List<String> preWriteWarnings = new ArrayList<>();
        String safeDetailContent = contentSanitizer.sanitize(bundle.getProject(), preWriteWarnings);
        TicketSourceProjectSyncResult base = legacyWriter.synchronize(
                provider, V11ToLegacyResourceMapper.convert(bundle, safeDetailContent), autoPublish, syncInventory);
        List<String> warnings = new ArrayList<>(base.getWarnings());
        warnings.addAll(preWriteWarnings);
        ProviderProjectDetail detail = bundle.getProject();
        ProviderProjectSummary summary = detail.summary();

        mapper.updateProjectSourceState(
                base.getProjectMappingId(),
                sourceCode(summary.projectStatus()),
                sourceText(summary.projectStatus()),
                local(summary.updatedAt()));

        for (V11ResourceBundle.SessionBundle sessionBundle : bundle.getSessions()) {
            ProviderSession session = sessionBundle.getSession();
            mapper.updateSessionSourceState(provider.getProviderId(), base.getProjectMappingId(),
                    session.sessionId(), sourceCode(session.sessionStatus()),
                    sourceText(session.sessionStatus()), local(session.updatedAt()));
            for (ProviderTicketProduct product : sessionBundle.getTicketProducts()) {
                ProviderInventory inventory = sessionBundle.inventory(product.ticketProductId());
                ProviderStatusValue<?> status = inventory == null ? product.saleStatus() : inventory.saleStatus();
                mapper.updateSkuSourceState(provider.getProviderId(), product.ticketProductId(),
                        sourceCode(status), sourceText(status),
                        local(inventory == null ? product.updatedAt() : inventory.snapshotAt()));
            }
        }

        syncVenue(provider, bundle, base.getProjectId(), warnings);
        int noticeCount = syncNotices(provider, detail, base.getProjectId(), warnings);
        int tagCount = syncServiceTags(provider, detail, base.getProjectId(), warnings);
        int refundTierCount = syncRefundPolicy(provider, detail, base.getProjectId());
        int promotionCount = syncPromotions(bundle, base.getProjectMappingId());
        int campaignCount = syncCampaignAssets(provider, bundle.getCampaignAssets());

        V11ResourceSyncResult result = new V11ResourceSyncResult();
        result.setSuccess(true);
        result.setProviderCode(provider.getProviderCode());
        result.setProviderProjectId(summary.projectId());
        result.setProjectMappingId(base.getProjectMappingId());
        result.setProjectId(base.getProjectId());
        result.setCreated(base.isCreated());
        result.setAutoPublishEnabled(base.isAutoPublishEnabled());
        result.setLocalProjectStatus(base.getProjectStatus());
        result.setSessionCount(base.getSessionCount());
        result.setTicketProductCount(base.getSkuCount());
        result.setInventoryAppliedCount(base.getInventoryAppliedCount());
        result.setInventoryUnknownCount(base.getInventoryUnknownCount());
        result.setNoticeCount(noticeCount);
        result.setServiceTagCount(tagCount);
        result.setRefundTierCount(refundTierCount);
        result.setPromotionCount(promotionCount);
        result.setCampaignAssetCount(campaignCount);
        result.setSyncTime(base.getSyncTime());
        result.setWarnings(warnings);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public int synchronizeCampaignAssets(TicketSourceProvider provider, String cityCode,
                                         List<ProviderCampaignAsset> assets) {
        mapper.disableCampaignAssetsByCity(provider.getProviderId(), cityCode);
        return syncCampaignAssets(provider, assets);
    }

    private void syncVenue(TicketSourceProvider provider, V11ResourceBundle bundle,
                           Long projectId, List<String> warnings) {
        ProviderVenue venue = bundle.getVenue();
        if (venue == null) return;
        Long localVenueId = mapper.selectProjectVenueId(projectId);
        if (localVenueId == null) {
            warnings.add("项目没有可更新的本地场馆，已跳过场馆坐标同步");
            return;
        }
        Map<String, Object> values = new HashMap<>();
        values.put("venueId", localVenueId);
        values.put("venueName", venue.venueName());
        values.put("cityName", bundle.getProject().summary().cityName());
        values.put("address", defaultText(venue.address(), "第三方票源导入，详细地址待完善"));
        values.put("longitude", decimal(venue.longitude()));
        values.put("latitude", decimal(venue.latitude()));
        values.put("coordinateSystem", venue.coordinateSystem().name());
        mapper.updateVenue(values);
        values.put("providerId", provider.getProviderId());
        values.put("providerVenueId", venue.venueId());
        mapper.upsertVenueMapping(values);
    }

    private int syncNotices(TicketSourceProvider provider, ProviderProjectDetail detail,
                            Long projectId, List<String> warnings) {
        mapper.deleteProviderNotices(projectId, provider.getProviderId());
        int saved = 0;
        for (ProviderNotice notice : detail.notices()) {
            String noticeType = localNoticeType(notice.noticeCode());
            Long noticeId = mapper.selectNoticeIdByType(noticeType);
            if (noticeId == null) noticeId = mapper.selectNoticeIdByTitle(defaultText(notice.title(), notice.noticeCode()));
            if (noticeId == null) {
                Map<String, Object> item = new HashMap<>();
                item.put("title", defaultText(notice.title(), notice.noticeCode()));
                item.put("description", defaultText(notice.content(), "第三方未提供详细说明"));
                item.put("iconUrl", defaultNoticeIcon(noticeType));
                item.put("noticeType", noticeType);
                mapper.insertNotice(item);
                noticeId = ((Number) item.get("noticeId")).longValue();
            }
            Map<String, Object> relation = new HashMap<>();
            relation.put("projectId", projectId);
            relation.put("noticeId", noticeId);
            relation.put("providerId", provider.getProviderId());
            relation.put("providerNoticeKey", defaultText(notice.noticeCode(), notice.title()));
            relation.put("title", notice.title());
            relation.put("description", notice.content());
            relation.put("sortOrder", notice.priority());
            int inserted = mapper.insertProviderNoticeRelation(relation);
            if (inserted == 0) {
                warnings.add("须知“" + notice.title() + "”已有本地运营关系，保留本地配置");
            } else saved += inserted;
        }
        return saved;
    }

    private int syncServiceTags(TicketSourceProvider provider, ProviderProjectDetail detail,
                                Long projectId, List<String> warnings) {
        mapper.deleteProviderServiceTags(projectId, provider.getProviderId());
        int sort = 0;
        int saved = 0;
        Set<Long> selectedTagIds = new LinkedHashSet<>();
        for (ProviderServiceCapability capability : detail.serviceCapabilities()) {
            if (!capability.enabled()) continue;
            if (isRefundCapability(capability.capabilityCode())) continue;
            Long tagId = mapper.selectTagIdByCapability(capability.capabilityCode());
            if (tagId == null) {
                warnings.add("统一能力码未映射本地服务标签，已跳过: " + capability.capabilityCode());
                continue;
            }
            if (!selectedTagIds.add(tagId)) continue;
            if (selectedTagIds.size() > 4) break;
            Map<String, Object> relation = new HashMap<>();
            relation.put("projectId", projectId);
            relation.put("tagId", tagId);
            relation.put("providerId", provider.getProviderId());
            relation.put("sortOrder", ++sort);
            int inserted = mapper.insertProviderServiceTagRelation(relation);
            if (inserted == 0) warnings.add("服务标签已有本地运营关系，保留本地配置: " + capability.capabilityCode());
            else saved += inserted;
        }
        return saved;
    }


    private boolean isRefundCapability(String capabilityCode) {
        return "CONDITIONAL_REFUND".equals(capabilityCode)
                || "FREE_REFUND".equals(capabilityCode)
                || "NO_REFUND".equals(capabilityCode);
    }

    private int syncRefundPolicy(TicketSourceProvider provider, ProviderProjectDetail detail, Long projectId) {
        ProviderRefundPolicy policy = detail.refundPolicy();
        if (policy == null) return 0;
        Long ruleId = mapper.selectRefundRuleId(projectId);
        Map<String, Object> rule = new HashMap<>();
        rule.put("projectId", projectId);
        rule.put("providerId", provider.getProviderId());
        rule.put("providerRuleId", detail.summary().projectId() + ":REFUND");
        rule.put("refundType", defaultText(policy.refundType(), "NO_REFUND"));
        rule.put("consumerEntryEnabled", policy.consumerEntryEnabled());
        rule.put("deliveryFeeRefundable", policy.deliveryFeeRefundable());
        rule.put("ruleDescription", truncate(policy.sourceRuleText(), 500));
        if (ruleId == null) {
            mapper.insertRefundRule(rule);
            ruleId = ((Number) rule.get("refundRuleId")).longValue();
        } else {
            rule.put("refundRuleId", ruleId);
            mapper.updateRefundRule(rule);
        }
        mapper.deleteRefundStages(ruleId);
        int sort = 0;
        for (ProviderRefundTier tier : policy.tiers()) {
            Map<String, Object> stage = new HashMap<>();
            stage.put("refundRuleId", ruleId);
            stage.put("minBeforeStartMinutes", minBefore(tier));
            stage.put("maxBeforeStartMinutes", maxBefore(tier));
            stage.put("stageResult", defaultText(tier.result(), "NOT_ALLOWED"));
            stage.put("feeRate", feeRate(tier.feePercent()));
            stage.put("fixedFeeAmount", tier.feeFixed() == null ? null : tier.feeFixed().toMajor());
            stage.put("sortOrder", ++sort);
            mapper.insertRefundStage(stage);
        }
        return sort;
    }

private int syncPromotions(V11ResourceBundle bundle, Long projectMappingId) {
        mapper.disablePromotions(projectMappingId);
        int saved = 0;
        for (ProviderPromotionRule promotion : bundle.getPromotionRules()) {
            Map<String, Object> values = new HashMap<>();
            values.put("projectMappingId", projectMappingId);
            values.put("providerPromotionId", promotion.promotionId());
            values.put("promotionType", promotion.promotionType().name());
            values.put("promotionTitle", promotion.title());
            values.put("promotionDescription", truncate(promotion.description(), 500));
            values.put("startTime", local(promotion.validFrom()));
            values.put("endTime", local(promotion.validTo()));
            values.put("stackable", promotion.stackable());
            values.put("targetScopeJson", json(Map.of(
                    "projectIds", promotion.projectIds(),
                    "sessionIds", promotion.sessionIds(),
                    "ticketProductIds", promotion.ticketProductIds())));
            values.put("ruleDataJson", json(promotion.ruleData()));
            values.put("sourceVersion", promotion.version());
            values.put("sourceUpdatedTime", local(promotion.updatedAt()));
            mapper.upsertPromotion(values);
            saved++;
        }
        return saved;
    }

    private int syncCampaignAssets(TicketSourceProvider provider, List<ProviderCampaignAsset> assets) {
        int saved = 0;
        for (ProviderCampaignAsset asset : assets) {
            Map<String, Object> values = new HashMap<>();
            values.put("providerId", provider.getProviderId());
            values.put("providerAssetId", asset.assetId());
            values.put("assetType", asset.assetType().name());
            values.put("positionCode", asset.position());
            values.put("title", asset.title());
            values.put("description", truncate(asset.description(), 500));
            values.put("imageUrl", asset.imageUrl());
            values.put("mobileImageUrl", asset.mobileImageUrl());
            values.put("targetType", asset.targetType().name());
            values.put("providerTargetId", asset.targetValue());
            values.put("cityCodes", String.join(",", asset.cityCodes()));
            values.put("startTime", local(asset.startAt()));
            values.put("endTime", local(asset.endAt()));
            values.put("providerPromotionId", asset.providerPromotionId());
            values.put("sourceVersion", asset.version());
            values.put("sourceUpdatedTime", local(asset.updatedAt()));
            mapper.upsertCampaignAsset(values);
            saved++;
        }
        return saved;
    }

    private String sourceCode(ProviderStatusValue<?> status) {
        if (status == null) return null;
        return defaultText(status.sourceStatusCode(), status.status().name());
    }
    private String sourceText(ProviderStatusValue<?> status) {
        return status == null ? null : defaultText(status.sourceStatusText(), status.status().name());
    }
    private String localNoticeType(String code) {
        String normalized = code == null ? "OTHER" : code.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CHILDREN_NOTICE", "CHILDREN_POLICY" -> "CHILD";
            case "REAL_NAME_NOTICE", "REAL_NAME_POLICY" -> "REAL_NAME";
            case "LIMIT_NOTICE", "PURCHASE_LIMIT" -> "LIMIT";
            case "ENTRANCE_NOTICE", "ENTRY_NOTICE" -> "ENTRY";
            case "PROHIBITED_ITEMS" -> "SECURITY";
            case "DEPOSIT_INFO", "STORAGE_NOTICE" -> "STORAGE";
            case "SELF_GET_TICKET_NOTICE", "PICKUP_NOTICE" -> "PAPER_TICKET_NOTICE";
            case "ETICKET_NOTICE", "E_TICKET_NOTICE" -> "E_TICKET_NOTICE";
            case "CHOICE_SEAT_NOTICE", "SEAT_SELECTION_NOTICE" -> "SEAT_SELECTION_NOTICE";
            case "POLICY_OF_RETURN", "REFUND_NOTICE" -> "REFUND";
            default -> normalized.length() <= 32 ? normalized : "OTHER";
        };
    }
    private String defaultNoticeIcon(String type) {
        return switch (type) {
            case "CHILD" -> "item_ertonggoupiao";
            case "REAL_NAME" -> "item_shimingzhi";
            case "LIMIT" -> "item_xiangouguize";
            case "ENTRY" -> "item_ruchangfangshi";
            case "SECURITY" -> "item_anjian";
            case "REFUND" -> "item_tuipiaoguize";
            case "PAPER_TICKET_NOTICE", "E_TICKET_NOTICE", "SEAT_SELECTION_NOTICE" -> "item_anjian";
            default -> "item_anjian";
        };
    }
    private int minBefore(ProviderRefundTier tier) {
        if (tier.endOffsetMinutes() == null) return 0;
        return (int) Math.min(Integer.MAX_VALUE, Math.abs(tier.endOffsetMinutes()));
    }
    private Integer maxBefore(ProviderRefundTier tier) {
        if (tier.startOffsetMinutes() == null) return null;
        long value = Math.abs(tier.startOffsetMinutes());
        return value >= 500_000 ? null : (int) Math.min(Integer.MAX_VALUE, value);
    }
    private BigDecimal feeRate(String percent) {
        if (percent == null || percent.isBlank()) return null;
        return new BigDecimal(percent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }
    private BigDecimal decimal(String value) {
        try { return value == null || value.isBlank() ? null : new BigDecimal(value); }
        catch (NumberFormatException ignored) { return null; }
    }
    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new IllegalStateException("同步快照序列化失败", e); }
    }
    private LocalDateTime local(OffsetDateTime time) { return time == null ? null : time.atZoneSameInstant(ZONE).toLocalDateTime(); }
    private String defaultText(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private String truncate(String value, int max) { return value == null || value.length() <= max ? value : value.substring(0, max); }
    private String joinText(String first, String second) {
        if (first == null || first.isBlank()) return second;
        if (second == null || second.isBlank()) return first;
        return first + "；" + second;
    }
}
