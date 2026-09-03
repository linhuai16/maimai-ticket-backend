package com.example.maimaibackend.ticketsource.purchase.options;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.ticketsource.V11OrderMapper;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceOperation;
import com.example.maimaibackend.ticketsource.provider.enums.CredentialType;
import com.example.maimaibackend.ticketsource.provider.enums.DeliveryMode;
import com.example.maimaibackend.ticketsource.provider.enums.PurchaseMode;
import com.example.maimaibackend.ticketsource.provider.enums.SeatMode;
import com.example.maimaibackend.ticketsource.provider.model.ProviderCapabilities;
import com.example.maimaibackend.ticketsource.provider.model.ProviderInventory;
import com.example.maimaibackend.ticketsource.provider.model.ProviderProjectDetail;
import com.example.maimaibackend.ticketsource.provider.model.ProviderServiceCapability;
import com.example.maimaibackend.ticketsource.provider.model.ProviderSession;
import com.example.maimaibackend.ticketsource.provider.model.ProviderTicketProduct;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderQuoteRequest;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderSkuContext;
import com.example.maimaibackend.ticketsource.resource.provider.V11ResourceAdapterInvoker;
import com.example.maimaibackend.ticketsource.purchase.options.model.V12PurchaseOptionsView;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 第十批用户购票履约选项解析。

 */
@Service
public class V12PurchaseOptionService {
    private final V11OrderMapper mapper;
    private final V11ResourceAdapterInvoker invoker;

    public V12PurchaseOptionService(V11OrderMapper mapper, V11ResourceAdapterInvoker invoker) {
        this.mapper = mapper;
        this.invoker = invoker;
    }

    public V12PurchaseOptionsView get(Long projectId, Long sessionId, Long skuId) {
        V11OrderSkuContext sku = requireSku(projectId, sessionId, skuId);
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(sku.getProviderCode());
        ProviderCapabilities capabilities = call("查询第三方能力", () ->
                invoker.invoke(target, TicketSourceOperation.HEALTH, (adapter, ctx) -> adapter.capabilities(ctx)));
        ProviderProjectDetail project = call("查询第三方项目履约能力", () ->
                invoker.invoke(target, TicketSourceOperation.GET_PROJECT,
                        (adapter, ctx) -> adapter.getProject(ctx, sku.getProviderProjectId())));
        ProviderInventory inventory = call("查询第三方库存", () ->
                invoker.invoke(target, TicketSourceOperation.QUERY_INVENTORY,
                        (adapter, ctx) -> adapter.queryInventory(ctx, sku.getProviderSkuId())));
        refreshKnownInventorySnapshot(sku, inventory);
        List<ProviderSession> sessions = call("查询第三方场次履约规则", () ->
                invoker.invoke(target, TicketSourceOperation.QUERY_SESSIONS,
                        (adapter, ctx) -> adapter.querySessions(ctx, sku.getProviderProjectId())));
        ProviderSession providerSession = sessions.stream()
                .filter(item -> sku.getProviderSessionId().equals(item.sessionId()))
                .findFirst().orElseThrow(() -> new BusinessException("当前场次暂不可购买"));
        List<ProviderTicketProduct> products = call("查询第三方票档履约规则", () ->
                invoker.invoke(target, TicketSourceOperation.QUERY_SKUS,
                        (adapter, ctx) -> adapter.queryTicketProducts(ctx, sku.getProviderSessionId())));
        ProviderTicketProduct providerProduct = products.stream()
                .filter(item -> sku.getProviderSkuId().equals(item.ticketProductId()))
                .findFirst().orElse(null);
        PurchaseMode purchaseMode = resolvePurchaseMode(providerSession, capabilities);

        Set<String> serviceCodes = enabledServiceCodes(project);
        List<V12PurchaseOptionsView.Option> options = purchaseMode == null ? List.of()
                : resolveOptions(sku, providerProduct, capabilities, serviceCodes);
        List<String> warnings = new ArrayList<>();
        if (options.isEmpty()) {
            warnings.add("当前票档暂不可购买，请重新选择");
        }
        if (inventory == null || inventory.availableStock() == null) {
            warnings.add("库存待实时确认");
        }
        boolean saleable = purchaseMode != null && options.size() > 0
                && inventory != null
                && inventory.saleStatus() != null
                && "ON_SALE".equals(inventory.saleStatus().status().name())
                && (inventory.stockState() == null || !"SOLD_OUT".equals(inventory.stockState().name()))
                && (inventory.availableStock() == null || inventory.availableStock() > 0);
        return new V12PurchaseOptionsView(
                projectId, sessionId, skuId, saleable, purchaseMode == null ? null : purchaseMode.name(), sku.getLimitPerOrder(),
                inventory == null ? null : inventory.availableStock(),
                inventory != null && inventory.exact(), options, warnings);
    }

    private void refreshKnownInventorySnapshot(V11OrderSkuContext sku, ProviderInventory inventory) {
        if (sku == null || inventory == null || sku.getSkuMappingId() == null) {
            return;
        }
        if (inventory.saleStatus() == null || inventory.saleStatus().status() == null) {
            return;
        }
        String sourceSaleStatus = inventory.saleStatus().status().name();
        if (inventory.availableStock() == null) {
            // 未知库存必须把 Mapping 快照刷新为 NULL，但不能把本地库存写成 0。
            // 同时按 Provider 销售状态恢复本地 skuStatus，确保 ON_SALE/PRESALE + NULL 仍可进入实时确认。
            mapper.updateLocalSkuSaleStatusKeepStock(sku.getSkuId(), sourceSaleStatus);
            mapper.updateSkuMappingInventory(sku.getSkuMappingId(), null, sourceSaleStatus, LocalDateTime.now());
            return;
        }
        int availableStock = Math.max(inventory.availableStock(), 0);
        mapper.updateLocalSkuInventory(sku.getSkuId(), availableStock, sourceSaleStatus);
        mapper.updateSkuMappingInventory(sku.getSkuMappingId(), availableStock, sourceSaleStatus, LocalDateTime.now());
    }

    /**
     * 用户票档选择页的轻量库存刷新：只查询当前票档库存，不重新同步项目/场次/富文本。
     * 本地项目没有 V11 映射时直接跳过；Provider 临时失败由调用方决定是否沿用已有快照。
     */
    public boolean refreshInventoryForDisplay(Long projectId, Long sessionId, Long skuId) {
        if (projectId == null || projectId <= 0 || sessionId == null || sessionId <= 0 || skuId == null || skuId <= 0) {
            return false;
        }
        List<V11OrderSkuContext> rows = mapper.selectSkuContexts(projectId, sessionId, List.of(skuId));
        if (rows.size() != 1) {
            return false;
        }
        V11OrderSkuContext sku = rows.get(0);
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(sku.getProviderCode());
        ProviderInventory inventory = call("刷新票档选择页第三方库存", () ->
                invoker.invoke(target, TicketSourceOperation.QUERY_INVENTORY,
                        (adapter, ctx) -> adapter.queryInventory(ctx, sku.getProviderSkuId())));
        refreshKnownInventorySnapshot(sku, inventory);
        return true;
    }

    public void validateUserSelection(V11OrderQuoteRequest request) {
        if (request == null) throw new BusinessException("计价请求不能为空");
        V12PurchaseOptionsView view = get(request.projectId(), request.sessionId(), request.skuId());
        if (!view.saleable()) throw new BusinessException("当前票档不可购买或没有可确认的履约方式");
        String purchaseMode = upper(request.purchaseMode());
        if (!purchaseMode.equals(view.purchaseMode())) {
            throw new BusinessException("当前场次不支持该购买模式: " + purchaseMode);
        }
        String ticketMode = upper(request.ticketMode());
        String deliveryMode = upper(request.deliveryMode());
        boolean matched = view.options().stream().anyMatch(option ->
                option.ticketMode().equals(ticketMode) && option.deliveryMode().equals(deliveryMode));
        if (!matched) {
            throw new BusinessException("当前票档不支持该履约组合: " + ticketMode + "+" + deliveryMode);
        }
    }

    private V11OrderSkuContext requireSku(Long projectId, Long sessionId, Long skuId) {
        if (projectId == null || projectId <= 0 || sessionId == null || sessionId <= 0 || skuId == null || skuId <= 0) {
            throw new BusinessException("projectId/sessionId/skuId不能为空");
        }
        List<V11OrderSkuContext> rows = mapper.selectSkuContexts(projectId, sessionId, List.of(skuId));
        if (rows.size() != 1) throw new BusinessException("当前票档暂不可购买，请重新选择");
        V11OrderSkuContext sku = rows.get(0);
        if ("LOCAL_COMPAT".equals(sku.getInventoryAuthority())) {
            throw new BusinessException("当前演出暂不可购买");
        }
        if (!("ON_SALE".equals(sku.getSkuStatus()) || "PRESALE".equals(sku.getSkuStatus()))) {
            throw new BusinessException("当前票档不可购买");
        }
        return sku;
    }


    private PurchaseMode resolvePurchaseMode(ProviderSession session, ProviderCapabilities caps) {
        if (session == null || caps == null) return null;
        SeatMode seatMode = session.seatMode();
        if (seatMode == SeatMode.GENERAL_ADMISSION) return PurchaseMode.GENERAL_ADMISSION;
        // ASSIGNED_SEAT / AREA_ONLY 都不允许用户选座；必须由第三方系统分配。
        if (caps.systemSeatAssignment()) return PurchaseMode.SYSTEM_ASSIGN;
        return null;
    }

    private List<V12PurchaseOptionsView.Option> resolveOptions(V11OrderSkuContext sku,
                                                                 ProviderTicketProduct providerProduct,
                                                                 ProviderCapabilities caps,
                                                                 Set<String> serviceCodes) {
        if (caps == null || providerProduct == null) return List.of();
        List<V12PurchaseOptionsView.Option> options = new ArrayList<>();
        String localDeliveryType = upper(sku.getDeliveryType());

        /*
         * 纸质/电子属性必须以票档级履约声明为准。performance_session.delivery_type
         * 只保留为场次聚合展示字段，MIXED 场次不能反向给所有 SKU 补履约方式。
         */
        if (isPaperTicketSku(providerProduct)) {
            if (!caps.paperTicket() || !serviceCodes.contains("PAPER_TICKET")) return List.of();
            Set<String> tokens = fulfillmentTokens(providerProduct);

            // Provider 全局能力只做上限；SKU 没声明的方式绝不自动生成。
            if (tokens.contains("EXPRESS_SUPPORTED") && caps.expressDelivery()) {
                options.add(option(CredentialType.PAPER_TICKET, DeliveryMode.EXPRESS,
                        "快递配送", true, true));
            }
            if (tokens.contains("SELF_PICKUP_SUPPORTED")) {
                options.add(option(CredentialType.PAPER_TICKET, DeliveryMode.SELF_PICKUP,
                        "现场取票", false, options.isEmpty()));
            }
            return List.copyOf(options);
        }

        if ("PAPER_TICKET".equals(localDeliveryType) || !caps.electronicTicket()) return List.of();

        boolean genericElectronic = serviceCodes.contains("E_TICKET");
        if (genericElectronic || serviceCodes.contains("STATIC_QR")) {
            options.add(option(CredentialType.STATIC_QR, DeliveryMode.PAPERLESS,
                    "电子票", false, true));
        }
        if (caps.dynamicQr() && serviceCodes.contains("DYNAMIC_QR")) {
            options.add(option(CredentialType.DYNAMIC_QR, DeliveryMode.PAPERLESS,
                    "动态二维码", false, options.isEmpty()));
        }
        if (serviceCodes.contains("ID_CARD_ENTRY")) {
            options.add(option(CredentialType.ID_CARD, DeliveryMode.PAPERLESS,
                    "身份证入场", false, options.isEmpty()));
        }
        if (serviceCodes.contains("SMS_CODE")) {
            options.add(option(CredentialType.SMS_CODE, DeliveryMode.PAPERLESS,
                    "短信凭证", false, options.isEmpty()));
        }
        return List.copyOf(options);
    }

    private boolean isPaperTicketSku(ProviderTicketProduct providerProduct) {
        if (providerProduct == null) return false;
        Set<String> tokens = fulfillmentTokens(providerProduct);
        if (tokens.contains("PAPER_TICKET")
                || tokens.contains("EXPRESS_SUPPORTED")
                || tokens.contains("SELF_PICKUP_SUPPORTED")) {
            return true;
        }
        // 历史模拟数据迁移兜底；正式 Adapter 应返回结构化票档级履约 token。
        String productName = providerProduct.productName() == null ? "" : providerProduct.productName();
        return productName.contains("纸质票") || productName.toUpperCase(Locale.ROOT).contains("PAPER");
    }

    private V12PurchaseOptionsView.Option option(CredentialType ticketMode,
                                                   DeliveryMode deliveryMode,
                                                   String label,
                                                   boolean requiresAddress,
                                                   boolean recommended) {
        return new V12PurchaseOptionsView.Option(ticketMode.name(), deliveryMode.name(), label,
                requiresAddress, recommended);
    }

    private Set<String> fulfillmentTokens(ProviderTicketProduct providerProduct) {
        Set<String> result = new HashSet<>();
        if (providerProduct == null || providerProduct.subStatus() == null) return result;
        String normalized = upper(providerProduct.subStatus());
        for (String token : normalized.split("[|,;\\s]+")) {
            if (!token.isBlank()) result.add(token);
        }
        return result;
    }

    private Set<String> enabledServiceCodes(ProviderProjectDetail project) {
        Set<String> result = new HashSet<>();
        if (project == null || project.serviceCapabilities() == null) return result;
        for (ProviderServiceCapability capability : project.serviceCapabilities()) {
            if (capability == null || !capability.enabled() || capability.capabilityCode() == null) continue;
            result.add(upper(capability.capabilityCode()));
        }
        return result;
    }

    private <T> T call(String action, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (BusinessException error) {
            throw error;
        } catch (RuntimeException error) {
            throw invoker.translate(action, error);
        }
    }

    private String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
