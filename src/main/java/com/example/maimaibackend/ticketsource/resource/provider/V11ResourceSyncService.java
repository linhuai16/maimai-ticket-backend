package com.example.maimaibackend.ticketsource.resource.provider;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceResourceSyncMapper;
import com.example.maimaibackend.mapper.ticketsource.V11ResourceSyncMapper;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProjectMapping;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceOperation;
import com.example.maimaibackend.ticketsource.provider.adapter.V11AdapterException;
import com.example.maimaibackend.ticketsource.provider.model.*;
import com.example.maimaibackend.ticketsource.resource.provider.model.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class V11ResourceSyncService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final V11ResourceAdapterInvoker invoker;
    private final TicketSourceResourceSyncMapper legacyMapper;
    private final V11ResourceSyncMapper mapper;
    private final V11ResourceSyncWriter writer;

    public V11ResourceSyncService(V11ResourceAdapterInvoker invoker,
                                  TicketSourceResourceSyncMapper legacyMapper,
                                  V11ResourceSyncMapper mapper,
                                  V11ResourceSyncWriter writer) {
        this.invoker = invoker;
        this.legacyMapper = legacyMapper;
        this.mapper = mapper;
        this.writer = writer;
    }

    public V11ResourcePreview preview(String providerCode, String providerProjectId) {
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(providerCode);
        List<String> warnings = new ArrayList<>();
        V11ResourceBundle bundle = loadBundle(target, requireId(providerProjectId, "第三方项目ID"), false, true, warnings);
        TicketSourceProjectMapping mapping = legacyMapper.selectProjectMappingByRemote(
                target.provider().getProviderId(), providerProjectId);
        V11ResourcePreview preview = new V11ResourcePreview();
        preview.setProviderCode(target.provider().getProviderCode());
        preview.setCapabilities(bundle.getCapabilities());
        preview.setProject(bundle.getProject());
        preview.setSessionCount(bundle.sessionCount());
        preview.setTicketProductCount(bundle.ticketProductCount());
        preview.setNoticeCount(bundle.getProject().notices().size());
        preview.setCapabilityCount(bundle.getProject().serviceCapabilities().size());
        preview.setRefundTierCount(bundle.getProject().refundPolicy() == null ? 0 : bundle.getProject().refundPolicy().tiers().size());
        preview.setPromotionCount(bundle.getPromotionRules().size());
        preview.setCampaignAssetCount(bundle.getCampaignAssets().size());
        preview.setAlreadyBound(mapping != null);
        preview.setLocalProjectId(mapping == null ? null : mapping.getProjectId());
        preview.setAutoPublishEnabled(mapping != null && Boolean.TRUE.equals(mapping.getAutoPublishEnabled()));
        preview.setWarnings(warnings);
        return preview;
    }

    public V11ResourceSyncResult syncProject(String providerCode, String providerProjectId,
                                               V11ResourceSyncRequest request) {
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(providerCode);
        String projectId = requireId(providerProjectId, "第三方项目ID");
        TicketSourceProjectMapping existing = legacyMapper.selectProjectMappingByRemote(
                target.provider().getProviderId(), projectId);
        List<String> warnings = new ArrayList<>();
        try {
            boolean syncInventory = request == null || request.syncInventoryOrDefault();
            boolean syncCampaigns = request == null || request.syncCampaignAssetsOrDefault();
            V11ResourceBundle bundle = loadBundle(target, projectId, syncInventory, syncCampaigns, warnings);
            preventOlderVersion(target, bundle.getProject().summary());
            boolean autoPublish = request != null && request.getAutoPublish() != null
                    ? Boolean.TRUE.equals(request.getAutoPublish())
                    : existing != null && Boolean.TRUE.equals(existing.getAutoPublishEnabled());
            boolean applyInventory = syncInventory && !bundle.isInventoryQueryFailed();
            if (syncInventory && !applyInventory) {
                warnings.add("本次存在库存查询失败，为避免部分票档快照不一致，本轮所有库存均未写入；修复后重新同步即可");
            }
            V11ResourceSyncResult result = writer.synchronize(
                    target.provider(), bundle, autoPublish, applyInventory);
            List<String> merged = new ArrayList<>(result.getWarnings());
            merged.addAll(warnings);
            result.setWarnings(merged);
            return result;
        } catch (RuntimeException e) {
            recordFailure(existing, e);
            throw invoker.translate("V1.1资源同步", e);
        }
    }

    public V11ResourceMappingSummary mapping(String providerCode, String providerProjectId) {
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(providerCode);
        V11ResourceMappingSummary summary = mapper.selectSummary(
                target.provider().getProviderId(), requireId(providerProjectId, "第三方项目ID"));
        if (summary == null) throw new BusinessException("第三方项目尚未同步到本地: " + providerProjectId);
        summary.setDetails(mapper.selectMappingDetails(target.provider().getProviderId(), providerProjectId));
        return summary;
    }

    public V11CampaignSyncResult syncCampaignAssets(String providerCode, String cityCode) {
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(providerCode);
        ProviderCapabilities capabilities;
        try {
            capabilities = invoker.invoke(target, TicketSourceOperation.HEALTH, (adapter, ctx) -> adapter.capabilities(ctx));
            if (!capabilities.campaignAssetFeed()) {
                throw new BusinessException("当前票源未声明活动素材同步能力: " + providerCode);
            }
            List<ProviderCampaignAsset> assets = invoker.invoke(target, TicketSourceOperation.QUERY_PROJECTS,
                    (adapter, ctx) -> adapter.queryCampaignAssets(ctx, cityCode));
            int saved = writer.synchronizeCampaignAssets(target.provider(), cityCode, assets);
            return new V11CampaignSyncResult(true, target.provider().getProviderCode(), cityCode,
                    assets == null ? 0 : assets.size(), saved, LocalDateTime.now());
        } catch (RuntimeException e) {
            throw invoker.translate("V1.1活动素材同步", e);
        }
    }

    private V11ResourceBundle loadBundle(V11ResourceAdapterInvoker.Target target,
                                         String projectId,
                                         boolean syncInventory,
                                         boolean syncCampaigns,
                                         List<String> warnings) {
        try {
            ProviderCapabilities capabilities = invoker.invoke(target, TicketSourceOperation.HEALTH,
                    (adapter, ctx) -> adapter.capabilities(ctx));
            ProviderProjectDetail project = invoker.invoke(target, TicketSourceOperation.GET_PROJECT,
                    (adapter, ctx) -> adapter.getProject(ctx, projectId));
            V11ResourceBundle bundle = new V11ResourceBundle();
            bundle.setCapabilities(capabilities);
            bundle.setProject(project);
            ProviderVenue venue = project.summary().venue();
            if (capabilities.venueQuery() && venue != null && venue.venueId() != null) {
                try {
                    ProviderVenue currentVenue = venue;
                    venue = invoker.invoke(target, TicketSourceOperation.GET_PROJECT,
                            (adapter, ctx) -> adapter.getVenue(ctx, currentVenue.venueId()));
                } catch (V11AdapterException e) {
                    warnings.add("独立场馆查询失败，使用项目内场馆快照: " + e.getMessage());
                }
            }
            bundle.setVenue(venue);

            List<V11ResourceBundle.SessionBundle> sessionBundles = new ArrayList<>();
            List<ProviderSession> sessions = invoker.invoke(target, TicketSourceOperation.QUERY_SESSIONS,
                    (adapter, ctx) -> adapter.querySessions(ctx, projectId));
            for (ProviderSession session : safe(sessions)) {
                V11ResourceBundle.SessionBundle sessionBundle = new V11ResourceBundle.SessionBundle();
                sessionBundle.setSession(session);
                List<ProviderTicketProduct> products = invoker.invoke(target, TicketSourceOperation.QUERY_SKUS,
                        (adapter, ctx) -> adapter.queryTicketProducts(ctx, session.sessionId()));
                List<ProviderTicketProduct> normalizedProducts = new ArrayList<>();
                List<ProviderInventory> inventories = new ArrayList<>();
                for (ProviderTicketProduct product : safe(products)) {
                    if (!syncInventory) {
                        normalizedProducts.add(product);
                        continue;
                    }
                    try {
                        ProviderInventory inventory = invoker.invoke(target, TicketSourceOperation.QUERY_INVENTORY,
                                (adapter, ctx) -> adapter.queryInventory(ctx, product.ticketProductId()));
                        inventories.add(inventory);
                        normalizedProducts.add(product);
                    } catch (RuntimeException inventoryError) {
                        bundle.setInventoryQueryFailed(true);
                        warnings.add("票档 " + product.ticketProductId() + " 库存查询失败；保留原本地库存: " + inventoryError.getMessage());
                        normalizedProducts.add(withUnknownStock(product));
                    }
                }
                sessionBundle.setTicketProducts(normalizedProducts);
                sessionBundle.setInventories(inventories);
                sessionBundles.add(sessionBundle);
            }
            bundle.setSessions(sessionBundles);

            if (capabilities.promotionRuleFeed()) {
                bundle.setPromotionRules(invoker.invoke(target, TicketSourceOperation.GET_PROJECT,
                        (adapter, ctx) -> adapter.queryPromotionRules(ctx, projectId)));
            }
            if (syncCampaigns && capabilities.campaignAssetFeed()) {
                bundle.setCampaignAssets(invoker.invoke(target, TicketSourceOperation.QUERY_PROJECTS,
                        (adapter, ctx) -> adapter.queryCampaignAssets(ctx, project.summary().cityCode())));
            }
            addBusinessWarnings(bundle, warnings);
            return bundle;
        } catch (RuntimeException e) {
            throw invoker.translate("读取V1.1第三方资源", e);
        }
    }

    private ProviderTicketProduct withUnknownStock(ProviderTicketProduct p) {
        return new ProviderTicketProduct(p.ticketProductId(), p.projectId(), p.sessionId(), p.productName(),
                p.productType(), p.facePrice(), p.salePrice(), p.settlementPrice(), p.saleStatus(), p.subStatus(),
                p.inventoryMode(), null, p.maxQuantityPerOrder(), p.version(), p.updatedAt());
    }

    private void addBusinessWarnings(V11ResourceBundle bundle, List<String> warnings) {
        if (bundle.sessionCount() == 0) warnings.add("第三方项目没有场次，当前项目无法正常交易");
        if (bundle.ticketProductCount() == 0) warnings.add("第三方项目没有票档，当前项目无法正常交易");
        if (bundle.getProject().refundPolicy() == null) warnings.add("第三方未提供结构化退款规则，项目应保持不可交易或人工核对");
        for (V11ResourceBundle.SessionBundle session : bundle.getSessions()) {
            for (ProviderTicketProduct product : session.getTicketProducts()) {
                if (product.availableStock() == null && session.inventory(product.ticketProductId()) == null) {
                    warnings.add("票档 " + product.ticketProductId() + " 没有精确库存；NULL不会按0处理");
                }
            }
        }
        if (bundle.getCapabilities().dynamicQr() && bundle.getCapabilities().dynamicQrMode() == null) {
            warnings.add("票源声明动态二维码但未声明刷新模式，相关票档不得上架");
        }
    }

    private void preventOlderVersion(V11ResourceAdapterInvoker.Target target, ProviderProjectSummary project) {
        LocalDateTime stored = mapper.selectProjectSourceUpdatedTime(
                target.provider().getProviderId(), project.projectId());
        LocalDateTime incoming = local(project.updatedAt());
        if (stored != null && incoming != null && stored.isAfter(incoming)) {
            throw new BusinessException(409, "拒绝旧版本资源覆盖：本地来源更新时间=" + stored + "，本次来源更新时间=" + incoming);
        }
    }

    private void recordFailure(TicketSourceProjectMapping existing, RuntimeException e) {
        if (existing == null || existing.getMappingId() == null) return;
        try {
            legacyMapper.updateProjectSyncFailure(existing.getMappingId(), "V11_RESOURCE_SYNC_FAILED", safeMessage(e));
        } catch (Exception ignored) {
            // 失败记录不得覆盖原始异常。
        }
    }

    private String safeMessage(Throwable e) {
        String value = e == null ? null : e.getMessage();
        if (value == null || value.isBlank()) return "未知错误";
        return value.length() <= 500 ? value : value.substring(0, 500);
    }
    private String requireId(String value, String name) {
        if (value == null || value.isBlank()) throw new BusinessException(name + "不能为空");
        return value.trim();
    }
    private LocalDateTime local(OffsetDateTime time) { return time == null ? null : time.atZoneSameInstant(ZONE).toLocalDateTime(); }
    private <T> List<T> safe(List<T> values) { return values == null ? List.of() : values; }
}
