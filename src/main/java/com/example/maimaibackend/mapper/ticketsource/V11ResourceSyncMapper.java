package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.resource.provider.model.V11ResourceMappingSummary;
import com.example.maimaibackend.ticketsource.resource.provider.model.V11ResourceMappingDetail;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface V11ResourceSyncMapper {
    LocalDateTime selectProjectSourceUpdatedTime(@Param("providerId") Long providerId,
                                                  @Param("providerProjectId") String providerProjectId);

    int updateProjectSourceState(@Param("mappingId") Long mappingId,
                                 @Param("sourceStatusValue") String sourceStatusValue,
                                 @Param("sourceStatusText") String sourceStatusText,
                                 @Param("sourceUpdatedTime") LocalDateTime sourceUpdatedTime);

    int updateSessionSourceState(@Param("providerId") Long providerId,
                                 @Param("projectMappingId") Long projectMappingId,
                                 @Param("providerSessionId") String providerSessionId,
                                 @Param("sourceStatusValue") String sourceStatusValue,
                                 @Param("sourceStatusText") String sourceStatusText,
                                 @Param("sourceUpdatedTime") LocalDateTime sourceUpdatedTime);

    int updateSkuSourceState(@Param("providerId") Long providerId,
                             @Param("providerSkuId") String providerSkuId,
                             @Param("sourceStatusValue") String sourceStatusValue,
                             @Param("sourceStatusText") String sourceStatusText,
                             @Param("sourceUpdatedTime") LocalDateTime sourceUpdatedTime);

    Long selectProjectVenueId(@Param("projectId") Long projectId);
    int updateVenue(Map<String, Object> values);
    int upsertVenueMapping(Map<String, Object> values);

    Long selectNoticeIdByType(@Param("noticeType") String noticeType);
    Long selectNoticeIdByTitle(@Param("title") String title);
    int insertNotice(Map<String, Object> values);
    int deleteProviderNotices(@Param("projectId") Long projectId, @Param("providerId") Long providerId);
    int insertProviderNoticeRelation(Map<String, Object> values);

    int deleteProviderServiceTags(@Param("projectId") Long projectId, @Param("providerId") Long providerId);
    Long selectTagIdByCapability(@Param("capabilityCode") String capabilityCode);
    int insertProviderServiceTagRelation(Map<String, Object> values);

    Long selectRefundRuleId(@Param("projectId") Long projectId);
    int insertRefundRule(Map<String, Object> values);
    int updateRefundRule(Map<String, Object> values);
    int deleteRefundStages(@Param("refundRuleId") Long refundRuleId);
    int insertRefundStage(Map<String, Object> values);

    int disablePromotions(@Param("projectMappingId") Long projectMappingId);
    int upsertPromotion(Map<String, Object> values);

    int disableCampaignAssetsByCity(@Param("providerId") Long providerId, @Param("cityCode") String cityCode);
    int upsertCampaignAsset(Map<String, Object> values);

    V11ResourceMappingSummary selectSummary(@Param("providerId") Long providerId,
                                             @Param("providerProjectId") String providerProjectId);

    List<V11ResourceMappingDetail> selectMappingDetails(@Param("providerId") Long providerId,
                                                         @Param("providerProjectId") String providerProjectId);
}
