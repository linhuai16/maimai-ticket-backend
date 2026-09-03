package com.example.maimaibackend.ticketsource.sync;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceResourceSyncMapper;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProjectMapping;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProvider;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceSessionMapping;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceSkuMapping;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSku;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceInventorySyncResult;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceLocalProject;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceLocalSession;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceLocalSku;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceLocalVenue;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceProjectBundle;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceProjectSyncResult;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceSkuBinding;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TicketSourceResourceSyncWriter {
    private static final String MAPPING_BOUND = "BOUND";
    private static final String SYNC_SUCCESS = "SUCCESS";
    private static final String LOCAL_OFFLINE = "OFFLINE";
    private static final String DEFAULT_POSTER = "default_project_poster";
    private static final String DEFAULT_VENUE_ADDRESS = "第三方票源导入，详细地址待完善";

    private final TicketSourceResourceSyncMapper mapper;
    private final ObjectMapper objectMapper;

    public TicketSourceResourceSyncWriter(
            TicketSourceResourceSyncMapper mapper,
            ObjectMapper objectMapper
    ) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketSourceProjectSyncResult synchronize(
            TicketSourceProvider provider,
            TicketSourceProjectBundle bundle,
            boolean autoPublish,
            boolean syncInventory
    ) {
        TicketSourceProject sourceProject = requireProject(bundle);
        LocalDateTime syncTime = LocalDateTime.now();
        Long categoryId = mapper.selectCategoryIdByName(requireText(sourceProject.getCategoryName(), "第三方项目分类"));
        if (categoryId == null) {
            throw new BusinessException("第三方分类未在本地 category 表配置: " + sourceProject.getCategoryName());
        }

        TicketSourceProjectMapping projectMapping = mapper.selectProjectMappingByRemote(
                provider.getProviderId(), sourceProject.getProviderProjectId());
        boolean created = projectMapping == null;

        TicketSourceLocalProject localProject = buildLocalProject(sourceProject, categoryId);
        if (created) {
            mapper.insertProject(localProject);
            projectMapping = new TicketSourceProjectMapping();
            projectMapping.setProviderId(provider.getProviderId());
            projectMapping.setProjectId(localProject.getProjectId());
            projectMapping.setProviderProjectId(sourceProject.getProviderProjectId());
        } else {
            localProject.setProjectId(projectMapping.getProjectId());
            int projectUpdated = mapper.updateProjectFromSource(
                    localProject, provider.getProviderId(), sourceProject.getProviderProjectId());
            if (projectUpdated != 1) {
                throw new BusinessException("Provider 项目事实同步失败：本地项目与 Provider 映射不匹配");
            }
        }

        fillProjectMapping(projectMapping, sourceProject, autoPublish, syncTime);
        if (created) {
            mapper.insertProjectMapping(projectMapping);
        } else {
            mapper.updateProjectMapping(projectMapping);
            mapper.markSkuMappingsPending(projectMapping.getMappingId());
            mapper.markSessionMappingsPending(projectMapping.getMappingId());
        }

        int sessionCount = 0;
        int skuCount = 0;
        int inventoryAppliedCount = 0;
        int inventoryUnknownCount = 0;
        List<String> warnings = new ArrayList<>();

        for (TicketSourceProjectBundle.SessionBundle sessionBundle : safeSessions(bundle)) {
            TicketSourceSession sourceSession = requireSession(sessionBundle.getSession());
            Long venueId = resolveVenue(sourceSession);
            TicketSourceSessionMapping sessionMapping = mapper.selectSessionMappingByRemote(
                    provider.getProviderId(), projectMapping.getMappingId(), sourceSession.getProviderSessionId());
            boolean newSession = sessionMapping == null;

            TicketSourceLocalSession localSession = buildLocalSession(
                    projectMapping.getProjectId(), venueId, sourceSession);
            if (newSession) {
                mapper.insertSession(localSession);
                sessionMapping = new TicketSourceSessionMapping();
                sessionMapping.setProviderId(provider.getProviderId());
                sessionMapping.setProjectMappingId(projectMapping.getMappingId());
                sessionMapping.setSessionId(localSession.getSessionId());
                sessionMapping.setProviderSessionId(sourceSession.getProviderSessionId());
            } else {
                localSession.setSessionId(sessionMapping.getSessionId());
                int sessionUpdated = mapper.updateSessionFromSource(
                        localSession, provider.getProviderId(), projectMapping.getMappingId(), sourceSession.getProviderSessionId());
                if (sessionUpdated != 1) {
                    throw new BusinessException("Provider 场次事实同步失败：本地场次与 Provider 映射不匹配");
                }
            }
            fillSessionMapping(sessionMapping, sourceSession, syncTime);
            if (newSession) {
                mapper.insertSessionMapping(sessionMapping);
            } else {
                mapper.updateSessionMapping(sessionMapping);
            }
            sessionCount++;

            int sortOrder = 0;
            for (TicketSourceSku sourceSku : safeSkus(sessionBundle)) {
                sortOrder++;
                requireSku(sourceSku);
                TicketSourceSkuMapping skuMapping = mapper.selectSkuMappingByRemote(
                        provider.getProviderId(), sessionMapping.getMappingId(), sourceSku.getProviderSkuId());
                boolean newSku = skuMapping == null;
                String inventoryAuthority = TicketSourcePublishPolicy.inventoryAuthority(sourceSku.getInventoryMode());
                boolean applyStock = syncInventory && sourceSku.getAvailableStock() != null;

                TicketSourceLocalSku localSku = buildLocalSku(
                        projectMapping.getProjectId(), sessionMapping.getSessionId(), sourceSku,
                        sortOrder, inventoryAuthority, applyStock);
                if (newSku) {
                    mapper.insertSku(localSku);
                    skuMapping = new TicketSourceSkuMapping();
                    skuMapping.setProviderId(provider.getProviderId());
                    skuMapping.setSessionMappingId(sessionMapping.getMappingId());
                    skuMapping.setSkuId(localSku.getSkuId());
                    skuMapping.setProviderSkuId(sourceSku.getProviderSkuId());
                } else {
                    localSku.setSkuId(skuMapping.getSkuId());
                    int skuUpdated = mapper.updateSkuFromSource(
                            localSku, applyStock, sourceSku.getAvailableStock(), provider.getProviderId(),
                            sessionMapping.getMappingId(), sourceSku.getProviderSkuId());
                    if (skuUpdated != 1) {
                        throw new BusinessException("Provider 票档事实同步失败：本地票档与 Provider 映射不匹配");
                    }
                }
                fillSkuMapping(skuMapping, sourceSku, syncTime, syncInventory);
                if (newSku) {
                    mapper.insertSkuMapping(skuMapping);
                } else {
                    mapper.updateSkuMapping(skuMapping);
                }
                skuCount++;
                if (syncInventory && sourceSku.getAvailableStock() != null) {
                    inventoryAppliedCount++;
                } else if (sourceSku.getAvailableStock() == null) {
                    inventoryUnknownCount++;
                    warnings.add("票档 " + sourceSku.getProviderSkuId() + " 未返回库存，NULL 未按售罄处理");
                }
            }
        }

        if (!created) {
            mapper.offlinePendingLocalSkus(projectMapping.getMappingId());
            mapper.offlinePendingLocalSessions(projectMapping.getMappingId());
            mapper.disablePendingSkuMappings(projectMapping.getMappingId());
            mapper.disablePendingSessionMappings(projectMapping.getMappingId());
        }

        refreshDerivedFields(projectMapping.getMappingId());

        TicketSourceProjectSyncResult result = new TicketSourceProjectSyncResult();
        result.setSuccess(true);
        result.setProviderCode(provider.getProviderCode());
        result.setProviderProjectId(sourceProject.getProviderProjectId());
        result.setProjectMappingId(projectMapping.getMappingId());
        result.setProjectId(projectMapping.getProjectId());
        result.setCreated(created);
        result.setAutoPublishEnabled(autoPublish);
        result.setProjectStatus(mapper.selectProjectStatus(projectMapping.getProjectId()));
        result.setSessionCount(sessionCount);
        result.setSkuCount(skuCount);
        result.setInventoryAppliedCount(inventoryAppliedCount);
        result.setInventoryUnknownCount(inventoryUnknownCount);
        result.setSyncTime(syncTime);
        result.setWarnings(warnings);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketSourceInventorySyncResult applyInventory(
            TicketSourceSkuBinding binding,
            TicketSourceInventory inventory
    ) {
        if (binding == null) {
            throw new BusinessException("第三方票档尚未绑定本地票档");
        }
        if (inventory == null) {
            throw new BusinessException("第三方库存响应为空");
        }
        String inventoryMode = TicketSourcePublishPolicy.normalizeInventoryMode(inventory.getInventoryMode());
        String sourceSaleStatus = TicketSourcePublishPolicy.normalizeSaleStatus(inventory.getSaleStatus());
        String authority = TicketSourcePublishPolicy.inventoryAuthority(inventoryMode);
        Integer providerStock = inventory.getAvailableStock();

        mapper.updateSkuMappingInventory(
                binding.getSkuMappingId(), inventoryMode, providerStock, sourceSaleStatus,
                inventory.getDataVersion(), snapshot(inventory));
        boolean stockApplied = providerStock != null;
        if (stockApplied) {
            mapper.updateLocalSkuInventoryKnown(binding.getSkuId(), providerStock, authority);
        } else {
            mapper.updateLocalSkuInventoryUnknown(binding.getSkuId(), authority);
        }
        refreshDerivedFields(binding.getProjectMappingId());

        TicketSourceInventorySyncResult result = new TicketSourceInventorySyncResult();
        result.setSuccess(true);
        result.setProviderCode(binding.getProviderCode());
        result.setProviderSkuId(binding.getProviderSkuId());
        result.setSkuMappingId(binding.getSkuMappingId());
        result.setSkuId(binding.getSkuId());
        result.setInventoryMode(inventoryMode);
        result.setProviderAvailableStock(providerStock);
        result.setLocalAvailableStock(mapper.selectLocalSkuAvailableStock(binding.getSkuId()));
        result.setSourceSaleStatus(sourceSaleStatus);
        result.setLocalSkuStatus(mapper.selectLocalSkuStatus(binding.getSkuId()));
        result.setStockApplied(stockApplied);
        result.setMessage(stockApplied
                ? "第三方库存快照已写入本地展示库存"
                : "第三方库存为未知，保留本地库存值且未按 0/售罄处理");
        result.setSyncTime(LocalDateTime.now());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAutoPublish(Long projectMappingId, boolean enabled) {
        int updated = mapper.updateAutoPublish(projectMappingId, enabled);
        if (updated != 1) {
            throw new BusinessException("项目映射不存在或未绑定");
        }
        refreshDerivedFields(projectMappingId);
    }

    private void refreshDerivedFields(Long projectMappingId) {
        mapper.refreshSessionPrices(projectMappingId);
        mapper.refreshProjectPrices(projectMappingId);
        mapper.applyPublishPolicyToSkus(projectMappingId);
        mapper.applyPublishPolicyToSessions(projectMappingId);
    }

    private TicketSourceProject requireProject(TicketSourceProjectBundle bundle) {
        if (bundle == null || bundle.getProject() == null) {
            throw new BusinessException("第三方项目数据为空");
        }
        TicketSourceProject project = bundle.getProject();
        requireText(project.getProviderProjectId(), "第三方项目ID");
        requireText(project.getProjectName(), "第三方项目名称");
        return project;
    }

    private TicketSourceSession requireSession(TicketSourceSession session) {
        if (session == null) {
            throw new BusinessException("第三方场次数据为空");
        }
        requireText(session.getProviderSessionId(), "第三方场次ID");
        requireText(session.getSessionName(), "第三方场次名称");
        requireText(session.getCityName(), "第三方场次城市");
        requireText(session.getVenueName(), "第三方场馆名称");
        if (session.getStartTime() == null) {
            throw new BusinessException("第三方场次开始时间不能为空: " + session.getProviderSessionId());
        }
        return session;
    }

    private void requireSku(TicketSourceSku sku) {
        if (sku == null) {
            throw new BusinessException("第三方票档数据为空");
        }
        requireText(sku.getProviderSkuId(), "第三方票档ID");
        requireText(sku.getSkuName(), "第三方票档名称");
        if (sku.getSalePrice() == null || sku.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("第三方票档销售价必须大于 0: " + sku.getProviderSkuId());
        }
        if (sku.getAvailableStock() != null && sku.getAvailableStock() < 0) {
            throw new BusinessException("第三方库存不能小于 0: " + sku.getProviderSkuId());
        }
    }

    private TicketSourceLocalProject buildLocalProject(TicketSourceProject source, Long categoryId) {
        TicketSourceLocalProject local = new TicketSourceLocalProject();
        local.setTitle(limit(requireText(source.getProjectName(), "第三方项目名称"), 200));
        local.setCategoryId(categoryId);
        local.setPosterUrl(limit(defaultIfBlank(source.getPosterUrl(), DEFAULT_POSTER), 500));
        local.setDetailContent(source.getDetailContent());
        local.setMinPrice(source.getMinPrice());
        local.setMaxPrice(source.getMaxPrice());
        local.setProjectStatus(LOCAL_OFFLINE);
        return local;
    }

    private TicketSourceLocalSession buildLocalSession(Long projectId, Long venueId, TicketSourceSession source) {
        TicketSourceLocalSession local = new TicketSourceLocalSession();
        local.setProjectId(projectId);
        local.setCityName(limit(source.getCityName().trim(), 50));
        local.setStationName(limit(source.getSessionName().trim(), 100));
        local.setVenueId(venueId);
        local.setStartTime(source.getStartTime());
        local.setEndTime(source.getEndTime());
        local.setSaleStartTime(source.getSaleStartTime());
        local.setSaleEndTime(source.getSaleEndTime());
        local.setSessionStatus(LOCAL_OFFLINE);
        int limitPerOrder = source.getLimitPerOrder() == null ? 2 : source.getLimitPerOrder();
        local.setLimitPerOrder(Math.max(1, Math.min(limitPerOrder, 99)));
        local.setDeliveryType(defaultIfBlank(source.getDeliveryType(), "ETICKET"));
        return local;
    }

    private TicketSourceLocalSku buildLocalSku(
            Long projectId,
            Long sessionId,
            TicketSourceSku source,
            int sortOrder,
            String inventoryAuthority,
            boolean applyStock
    ) {
        TicketSourceLocalSku local = new TicketSourceLocalSku();
        local.setProjectId(projectId);
        local.setSessionId(sessionId);
        local.setSkuName(limit(source.getSkuName().trim(), 100));
        local.setSkuDesc(limit("第三方票源：" + source.getProviderSkuId(), 200));
        local.setPrice(source.getSalePrice());
        local.setStockAvailable(TicketSourcePublishPolicy.initialLocalStock(source.getAvailableStock(), applyStock));
        local.setSkuStatus(LOCAL_OFFLINE);
        local.setSortOrder(sortOrder);
        local.setInventoryAuthority(inventoryAuthority);
        return local;
    }

    private Long resolveVenue(TicketSourceSession source) {
        String cityName = limit(source.getCityName().trim(), 50);
        String venueName = limit(source.getVenueName().trim(), 100);
        Long venueId = mapper.selectVenueId(cityName, venueName);
        if (venueId != null) {
            return venueId;
        }
        TicketSourceLocalVenue venue = new TicketSourceLocalVenue();
        venue.setCityName(cityName);
        venue.setVenueName(venueName);
        venue.setAddress(limit(defaultIfBlank(source.getVenueAddress(), DEFAULT_VENUE_ADDRESS), 255));
        mapper.insertVenue(venue);
        return venue.getVenueId();
    }

    private void fillProjectMapping(
            TicketSourceProjectMapping mapping,
            TicketSourceProject source,
            boolean autoPublish,
            LocalDateTime syncTime
    ) {
        mapping.setProviderProjectName(limit(source.getProjectName(), 255));
        mapping.setMappingStatus(MAPPING_BOUND);
        mapping.setSourceSaleStatus(TicketSourcePublishPolicy.normalizeSaleStatus(source.getSaleStatus()));
        mapping.setSourceDataVersion(limit(source.getDataVersion(), 128));
        mapping.setAutoPublishEnabled(autoPublish);
        mapping.setLastSyncStatus(SYNC_SUCCESS);
        mapping.setLastSyncTime(syncTime);
        mapping.setLastErrorCode(null);
        mapping.setLastErrorMessage(null);
        mapping.setSourcePayloadSnapshot(snapshot(source));
    }

    private void fillSessionMapping(
            TicketSourceSessionMapping mapping,
            TicketSourceSession source,
            LocalDateTime syncTime
    ) {
        mapping.setProviderSessionName(limit(source.getSessionName(), 255));
        mapping.setMappingStatus(MAPPING_BOUND);
        mapping.setSourceSaleStatus(TicketSourcePublishPolicy.normalizeSaleStatus(source.getSaleStatus()));
        mapping.setSourceDataVersion(limit(source.getDataVersion(), 128));
        mapping.setLastSyncStatus(SYNC_SUCCESS);
        mapping.setLastSyncTime(syncTime);
        mapping.setLastErrorCode(null);
        mapping.setLastErrorMessage(null);
        mapping.setSourcePayloadSnapshot(snapshot(source));
    }

    private void fillSkuMapping(
            TicketSourceSkuMapping mapping,
            TicketSourceSku source,
            LocalDateTime syncTime,
            boolean inventorySyncRequested
    ) {
        mapping.setProviderSkuName(limit(source.getSkuName(), 255));
        mapping.setMappingStatus(MAPPING_BOUND);
        mapping.setSourceSaleStatus(TicketSourcePublishPolicy.normalizeSaleStatus(source.getSaleStatus()));
        if (inventorySyncRequested) {
            mapping.setInventoryMode(TicketSourcePublishPolicy.normalizeInventoryMode(source.getInventoryMode()));
            mapping.setAvailableStockSnapshot(source.getAvailableStock());
            mapping.setLastInventorySyncTime(syncTime);
        } else if (mapping.getInventoryMode() == null || mapping.getInventoryMode().isBlank()) {
            // 首次仅同步资源时只记录库存模式，不把尚未应用的第三方数量写成“已生效库存快照”。
            mapping.setInventoryMode(TicketSourcePublishPolicy.normalizeInventoryMode(source.getInventoryMode()));
        }
        mapping.setFacePrice(source.getFacePrice());
        mapping.setSalePrice(source.getSalePrice());
        mapping.setSettlementPrice(source.getSettlementPrice());
        mapping.setCurrencyCode(limit(defaultIfBlank(source.getCurrencyCode(), "CNY"), 3));
        mapping.setSourceDataVersion(limit(source.getDataVersion(), 128));
        mapping.setLastSyncStatus(SYNC_SUCCESS);
        mapping.setLastSyncTime(syncTime);
        mapping.setLastErrorCode(null);
        mapping.setLastErrorMessage(null);
        mapping.setSourcePayloadSnapshot(snapshot(source));
    }

    private List<TicketSourceProjectBundle.SessionBundle> safeSessions(TicketSourceProjectBundle bundle) {
        return bundle.getSessions() == null ? List.of() : bundle.getSessions();
    }

    private List<TicketSourceSku> safeSkus(TicketSourceProjectBundle.SessionBundle bundle) {
        return bundle.getSkus() == null ? List.of() : bundle.getSkus();
    }

    private String snapshot(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(fieldName + "不能为空");
        }
        return value.trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
