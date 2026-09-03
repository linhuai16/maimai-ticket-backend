package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProjectMapping;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceSessionMapping;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceSkuMapping;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceLocalProject;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceLocalSession;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceLocalSku;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceLocalVenue;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceMappingSummary;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceSkuBinding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketSourceResourceSyncMapper {
    Long selectCategoryIdByName(@Param("categoryName") String categoryName);

    Long selectVenueId(@Param("cityName") String cityName, @Param("venueName") String venueName);

    int insertVenue(TicketSourceLocalVenue venue);

    int insertProject(TicketSourceLocalProject project);

    int updateProjectFromSource(@Param("project") TicketSourceLocalProject project,
                                @Param("providerId") Long providerId,
                                @Param("providerProjectId") String providerProjectId);

    String selectProjectStatus(@Param("projectId") Long projectId);

    int insertSession(TicketSourceLocalSession session);

    int updateSessionFromSource(@Param("session") TicketSourceLocalSession session,
                                @Param("providerId") Long providerId,
                                @Param("projectMappingId") Long projectMappingId,
                                @Param("providerSessionId") String providerSessionId);

    int insertSku(TicketSourceLocalSku sku);

    int updateSkuFromSource(@Param("sku") TicketSourceLocalSku sku,
                            @Param("applyStock") boolean applyStock,
                            @Param("providerStock") Integer providerStock,
                            @Param("providerId") Long providerId,
                            @Param("sessionMappingId") Long sessionMappingId,
                            @Param("providerSkuId") String providerSkuId);

    TicketSourceProjectMapping selectProjectMappingByRemote(@Param("providerId") Long providerId,
                                                            @Param("providerProjectId") String providerProjectId);

    TicketSourceSessionMapping selectSessionMappingByRemote(@Param("providerId") Long providerId,
                                                            @Param("projectMappingId") Long projectMappingId,
                                                            @Param("providerSessionId") String providerSessionId);

    TicketSourceSkuMapping selectSkuMappingByRemote(@Param("providerId") Long providerId,
                                                    @Param("sessionMappingId") Long sessionMappingId,
                                                    @Param("providerSkuId") String providerSkuId);

    int insertProjectMapping(TicketSourceProjectMapping mapping);

    int updateProjectMapping(TicketSourceProjectMapping mapping);

    int insertSessionMapping(TicketSourceSessionMapping mapping);

    int updateSessionMapping(TicketSourceSessionMapping mapping);

    int insertSkuMapping(TicketSourceSkuMapping mapping);

    int updateSkuMapping(TicketSourceSkuMapping mapping);

    int markSkuMappingsPending(@Param("projectMappingId") Long projectMappingId);

    int markSessionMappingsPending(@Param("projectMappingId") Long projectMappingId);

    int offlinePendingLocalSkus(@Param("projectMappingId") Long projectMappingId);

    int offlinePendingLocalSessions(@Param("projectMappingId") Long projectMappingId);

    int disablePendingSkuMappings(@Param("projectMappingId") Long projectMappingId);

    int disablePendingSessionMappings(@Param("projectMappingId") Long projectMappingId);

    int updateProjectSyncFailure(@Param("mappingId") Long mappingId,
                                 @Param("errorCode") String errorCode,
                                 @Param("errorMessage") String errorMessage);

    TicketSourceMappingSummary selectMappingSummary(@Param("providerId") Long providerId,
                                                     @Param("providerProjectId") String providerProjectId);

    List<TicketSourceSkuBinding> selectSkuBindingsByProject(@Param("providerId") Long providerId,
                                                            @Param("providerProjectId") String providerProjectId);

    TicketSourceSkuBinding selectSkuBindingByRemote(@Param("providerId") Long providerId,
                                                    @Param("providerSkuId") String providerSkuId);

    int updateAutoPublish(@Param("projectMappingId") Long projectMappingId,
                          @Param("enabled") boolean enabled);

    int updateSkuMappingInventory(@Param("skuMappingId") Long skuMappingId,
                                  @Param("inventoryMode") String inventoryMode,
                                  @Param("availableStock") Integer availableStock,
                                  @Param("sourceSaleStatus") String sourceSaleStatus,
                                  @Param("sourceDataVersion") String sourceDataVersion,
                                  @Param("sourcePayloadSnapshot") String sourcePayloadSnapshot);

    int updateSkuInventoryFailure(@Param("skuMappingId") Long skuMappingId,
                                  @Param("errorCode") String errorCode,
                                  @Param("errorMessage") String errorMessage);

    int updateLocalSkuInventoryKnown(@Param("skuId") Long skuId,
                                     @Param("providerAvailableStock") Integer providerAvailableStock,
                                     @Param("inventoryAuthority") String inventoryAuthority);

    int updateLocalSkuInventoryUnknown(@Param("skuId") Long skuId,
                                       @Param("inventoryAuthority") String inventoryAuthority);

    Integer selectLocalSkuAvailableStock(@Param("skuId") Long skuId);

    String selectLocalSkuStatus(@Param("skuId") Long skuId);

    int applyPublishPolicyToSkus(@Param("projectMappingId") Long projectMappingId);

    int applyPublishPolicyToSessions(@Param("projectMappingId") Long projectMappingId);


    int refreshSessionPrices(@Param("projectMappingId") Long projectMappingId);

    int refreshProjectPrices(@Param("projectMappingId") Long projectMappingId);
}
