package com.example.maimaibackend.ticketsource.sync;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceProviderMapper;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceResourceSyncMapper;
import com.example.maimaibackend.ticketsource.domain.enums.TicketSourceProviderStatus;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProjectMapping;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProvider;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceGateway;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCallResult;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSku;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceInventorySyncResult;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceMappingSummary;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceProjectBundle;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceProjectSyncResult;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceResourcePreview;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceSkuBinding;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceSyncRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TicketSourceResourceSyncService {
    private final TicketSourceGateway gateway;
    private final TicketSourceProviderMapper providerMapper;
    private final TicketSourceResourceSyncMapper resourceMapper;
    private final TicketSourceResourceSyncWriter writer;

    public TicketSourceResourceSyncService(
            TicketSourceGateway gateway,
            TicketSourceProviderMapper providerMapper,
            TicketSourceResourceSyncMapper resourceMapper,
            TicketSourceResourceSyncWriter writer
    ) {
        this.gateway = gateway;
        this.providerMapper = providerMapper;
        this.resourceMapper = resourceMapper;
        this.writer = writer;
    }

    public TicketSourceResourcePreview preview(String rawProviderCode, String rawProviderProjectId) {
        TicketSourceProvider provider = requireEnabledProvider(rawProviderCode);
        String providerProjectId = requireId(rawProviderProjectId, "第三方项目ID");
        TicketSourceProjectBundle bundle = loadBundle(provider.getProviderCode(), providerProjectId);
        TicketSourceProjectMapping mapping = resourceMapper.selectProjectMappingByRemote(
                provider.getProviderId(), providerProjectId);

        TicketSourceResourcePreview preview = new TicketSourceResourcePreview();
        preview.setProviderCode(provider.getProviderCode());
        preview.setProject(bundle.getProject());
        preview.setSessionCount(bundle.sessionCount());
        preview.setSkuCount(bundle.skuCount());
        preview.setAlreadyBound(mapping != null);
        preview.setLocalProjectId(mapping == null ? null : mapping.getProjectId());
        preview.setAutoPublishEnabled(mapping != null && Boolean.TRUE.equals(mapping.getAutoPublishEnabled()));
        preview.setWarnings(buildPreviewWarnings(bundle));
        return preview;
    }

    public TicketSourceProjectSyncResult syncProject(
            String rawProviderCode,
            String rawProviderProjectId,
            TicketSourceSyncRequest request
    ) {
        TicketSourceProvider provider = requireEnabledProvider(rawProviderCode);
        String providerProjectId = requireId(rawProviderProjectId, "第三方项目ID");
        TicketSourceProjectMapping existing = resourceMapper.selectProjectMappingByRemote(
                provider.getProviderId(), providerProjectId);
        try {
            TicketSourceProjectBundle bundle = loadBundle(provider.getProviderCode(), providerProjectId);
            boolean autoPublish = request != null && request.getAutoPublish() != null
                    ? Boolean.TRUE.equals(request.getAutoPublish())
                    : existing != null && Boolean.TRUE.equals(existing.getAutoPublishEnabled());
            boolean syncInventory = request == null || request.syncInventoryOrDefault();
            return writer.synchronize(provider, bundle, autoPublish, syncInventory);
        } catch (BusinessException e) {
            recordFailure(existing, "RESOURCE_SYNC_FAILED", e.getMessage());
            throw e;
        } catch (Exception e) {
            recordFailure(existing, "RESOURCE_SYNC_EXCEPTION", e.getMessage());
            throw new BusinessException(500, "第三方资源同步异常: " + safeMessage(e));
        }
    }

    public TicketSourceMappingSummary mapping(String rawProviderCode, String rawProviderProjectId) {
        TicketSourceProvider provider = requireProvider(rawProviderCode);
        String providerProjectId = requireId(rawProviderProjectId, "第三方项目ID");
        TicketSourceMappingSummary summary = resourceMapper.selectMappingSummary(
                provider.getProviderId(), providerProjectId);
        if (summary == null) {
            throw new BusinessException("第三方项目尚未同步到本地: " + providerProjectId);
        }
        return summary;
    }

    public TicketSourceMappingSummary updateAutoPublish(
            String rawProviderCode,
            String rawProviderProjectId,
            Boolean enabled
    ) {
        if (enabled == null) {
            throw new BusinessException("enabled 不能为空");
        }
        TicketSourceProvider provider = requireProvider(rawProviderCode);
        String providerProjectId = requireId(rawProviderProjectId, "第三方项目ID");
        TicketSourceMappingSummary summary = resourceMapper.selectMappingSummary(
                provider.getProviderId(), providerProjectId);
        if (summary == null) {
            throw new BusinessException("第三方项目尚未同步到本地: " + providerProjectId);
        }
        writer.updateAutoPublish(summary.getProjectMappingId(), enabled);
        return mapping(provider.getProviderCode(), providerProjectId);
    }

    public List<TicketSourceInventorySyncResult> syncProjectInventory(
            String rawProviderCode,
            String rawProviderProjectId
    ) {
        TicketSourceProvider provider = requireEnabledProvider(rawProviderCode);
        String providerProjectId = requireId(rawProviderProjectId, "第三方项目ID");
        List<TicketSourceSkuBinding> bindings = resourceMapper.selectSkuBindingsByProject(
                provider.getProviderId(), providerProjectId);
        if (bindings == null || bindings.isEmpty()) {
            throw new BusinessException("第三方项目尚未同步，或没有已绑定票档: " + providerProjectId);
        }

        List<TicketSourceInventorySyncResult> results = new ArrayList<>();
        for (TicketSourceSkuBinding binding : bindings) {
            TicketSourceCallResult<TicketSourceInventory> call = gateway.queryInventory(
                    provider.getProviderCode(), binding.getProviderSkuId());
            if (call == null || !call.isSuccess() || call.getData() == null) {
                recordInventoryFailure(binding, call);
                results.add(failedInventoryResult(binding, call));
                continue;
            }
            results.add(writer.applyInventory(binding, call.getData()));
        }
        return results;
    }

    public TicketSourceInventorySyncResult syncSkuInventory(
            String rawProviderCode,
            String rawProviderSkuId
    ) {
        TicketSourceProvider provider = requireEnabledProvider(rawProviderCode);
        String providerSkuId = requireId(rawProviderSkuId, "第三方票档ID");
        TicketSourceSkuBinding binding = resourceMapper.selectSkuBindingByRemote(
                provider.getProviderId(), providerSkuId);
        if (binding == null) {
            throw new BusinessException("第三方票档尚未同步或映射未绑定: " + providerSkuId);
        }
        TicketSourceCallResult<TicketSourceInventory> call = gateway.queryInventory(
                provider.getProviderCode(), providerSkuId);
        if (call == null || !call.isSuccess() || call.getData() == null) {
            recordInventoryFailure(binding, call);
            requireGatewaySuccess(call, "查询第三方库存");
        }
        return writer.applyInventory(binding, call.getData());
    }

    private TicketSourceProjectBundle loadBundle(String providerCode, String providerProjectId) {
        TicketSourceCallResult<TicketSourceProject> projectCall = gateway.getProject(
                providerCode, providerProjectId);
        requireGatewaySuccess(projectCall, "查询第三方项目");

        TicketSourceCallResult<List<TicketSourceSession>> sessionCall = gateway.querySessions(
                providerCode, providerProjectId);
        requireGatewaySuccess(sessionCall, "查询第三方场次");

        TicketSourceProjectBundle bundle = new TicketSourceProjectBundle();
        bundle.setProject(projectCall.getData());
        List<TicketSourceProjectBundle.SessionBundle> sessionBundles = new ArrayList<>();
        List<TicketSourceSession> sessions = sessionCall.getData() == null ? List.of() : sessionCall.getData();
        for (TicketSourceSession session : sessions) {
            TicketSourceCallResult<List<TicketSourceSku>> skuCall = gateway.querySkus(
                    providerCode, session.getProviderSessionId());
            requireGatewaySuccess(skuCall, "查询第三方票档 " + session.getProviderSessionId());
            TicketSourceProjectBundle.SessionBundle sessionBundle = new TicketSourceProjectBundle.SessionBundle();
            sessionBundle.setSession(session);
            sessionBundle.setSkus(skuCall.getData());
            sessionBundles.add(sessionBundle);
        }
        bundle.setSessions(sessionBundles);
        return bundle;
    }

    private List<String> buildPreviewWarnings(TicketSourceProjectBundle bundle) {
        List<String> warnings = new ArrayList<>();
        if (bundle.sessionCount() == 0) {
            warnings.add("第三方项目没有可同步场次，开启自动上架后仍会保持 OFFLINE");
        }
        if (bundle.skuCount() == 0) {
            warnings.add("第三方项目没有可同步票档，开启自动上架后仍会保持 OFFLINE");
        }
        for (TicketSourceProjectBundle.SessionBundle sessionBundle : bundle.getSessions()) {
            for (TicketSourceSku sku : sessionBundle.getSkus()) {
                if (sku.getAvailableStock() == null) {
                    warnings.add("票档 " + sku.getProviderSkuId() + " 的库存为未知；同步时不会把 NULL 当作 0");
                }
            }
        }
        return warnings;
    }

    private TicketSourceProvider requireEnabledProvider(String rawProviderCode) {
        TicketSourceProvider provider = requireProvider(rawProviderCode);
        if (!TicketSourceProviderStatus.ENABLED.name().equals(provider.getProviderStatus())) {
            throw new BusinessException("票源未启用: " + provider.getProviderCode());
        }
        return provider;
    }

    private TicketSourceProvider requireProvider(String rawProviderCode) {
        String providerCode = normalizeProviderCode(rawProviderCode);
        if (providerCode == null) {
            throw new BusinessException("票源编码不能为空");
        }
        TicketSourceProvider provider = providerMapper.selectByCode(providerCode);
        if (provider == null) {
            throw new BusinessException("票源不存在: " + providerCode);
        }
        return provider;
    }

    private <T> void requireGatewaySuccess(TicketSourceCallResult<T> call, String action) {
        if (call != null && call.isSuccess() && call.getData() != null) {
            return;
        }
        String errorCode = call == null || call.getErrorCode() == null
                ? "UNKNOWN"
                : call.getErrorCode().name();
        String providerError = call == null ? null : call.getProviderErrorCode();
        String message = call == null ? "票源网关返回空结果" : call.getMessage();
        String detail = action + "失败: " + errorCode
                + (providerError == null ? "" : "/" + providerError)
                + " - " + message;
        throw new BusinessException(502, detail);
    }


    private void recordInventoryFailure(
            TicketSourceSkuBinding binding,
            TicketSourceCallResult<TicketSourceInventory> call
    ) {
        if (binding == null || binding.getSkuMappingId() == null) {
            return;
        }
        String errorCode = call == null || call.getErrorCode() == null
                ? "UNKNOWN"
                : call.getErrorCode().name();
        String providerError = call == null ? null : call.getProviderErrorCode();
        String mergedCode = providerError == null ? errorCode : errorCode + "/" + providerError;
        String message = call == null ? "票源网关返回空结果" : call.getMessage();
        try {
            resourceMapper.updateSkuInventoryFailure(
                    binding.getSkuMappingId(), mergedCode, safeText(message));
        } catch (Exception ignored) {
            // 失败记录不能覆盖原始库存同步结果。
        }
    }

    private TicketSourceInventorySyncResult failedInventoryResult(
            TicketSourceSkuBinding binding,
            TicketSourceCallResult<TicketSourceInventory> call
    ) {
        TicketSourceInventorySyncResult result = new TicketSourceInventorySyncResult();
        result.setSuccess(false);
        result.setProviderCode(binding.getProviderCode());
        result.setProviderSkuId(binding.getProviderSkuId());
        result.setSkuMappingId(binding.getSkuMappingId());
        result.setSkuId(binding.getSkuId());
        result.setStockApplied(false);
        String errorCode = call == null || call.getErrorCode() == null
                ? "UNKNOWN"
                : call.getErrorCode().name();
        String providerErrorCode = call == null ? null : call.getProviderErrorCode();
        String message = call == null ? "票源网关返回空结果" : safeText(call.getMessage());
        result.setMessage("库存同步失败: "
                + errorCode
                + (providerErrorCode == null ? "" : "/" + providerErrorCode)
                + " - " + message);
        result.setSyncTime(LocalDateTime.now());
        return result;
    }

    private void recordFailure(TicketSourceProjectMapping mapping, String errorCode, String message) {
        if (mapping == null || mapping.getMappingId() == null) {
            return;
        }
        try {
            resourceMapper.updateProjectSyncFailure(mapping.getMappingId(), errorCode, safeText(message));
        } catch (Exception ignored) {
            // 失败记录不能覆盖原始同步异常。
        }
    }

    private String normalizeProviderCode(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String requireId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(fieldName + "不能为空");
        }
        return value.trim();
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "未知错误";
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private String safeMessage(Exception e) {
        return e == null ? "未知错误" : safeText(e.getMessage());
    }
}
