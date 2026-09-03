package com.example.maimaibackend.ticketsource.fulfillment.shipment;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.ticketsource.V11ShipmentMapper;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceOperation;
import com.example.maimaibackend.ticketsource.provider.adapter.V11AdapterException;
import com.example.maimaibackend.ticketsource.provider.enums.ShipmentStatus;
import com.example.maimaibackend.ticketsource.provider.model.ProviderCapabilities;
import com.example.maimaibackend.ticketsource.provider.model.ProviderShipment;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.model.*;
import com.example.maimaibackend.ticketsource.resource.provider.V11ResourceAdapterInvoker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class V11ShipmentService {
    private static final int MAX_BATCH = 100;

    private final V11ShipmentMapper mapper;
    private final V11ResourceAdapterInvoker invoker;

    public V11ShipmentService(V11ShipmentMapper mapper, V11ResourceAdapterInvoker invoker) {
        this.mapper = mapper;
        this.invoker = invoker;
    }

    @Transactional
    public void ensureWaitShipment(Long bridgeId) {
        if (bridgeId == null || bridgeId <= 0) throw new BusinessException("订单桥接ID不能为空");
        mapper.insertWaitShipment(bridgeId);
    }

    @Transactional
    public void markNotRequired(Long bridgeId) {
        if (bridgeId != null && bridgeId > 0) {
            mapper.markShipmentNotRequired(bridgeId, LocalDateTime.now());
        }
    }

    public V11ShipmentView get(Long orderId, Long userId) {
        V11ShipmentContext context = requireUserContext(orderId, userId);
        V11ShipmentRecord local = ensureLocalRecord(context);
        V11ShipmentView closed = closeUnshippedRefundedOrder(context, local);
        return closed == null ? view(context, local, capabilityHint(context), List.of()) : closed;
    }

    /**
     * 整单退款成功后的物流收口：尚未发货的快递纸票不应继续显示“待发货”。
     * 已经进入发货/运输/签收阶段的订单保留真实物流事实，并继续允许后续同步（例如退回）。
     */
    public V11ShipmentView onRefundSuccess(Long orderId) {
        V11ShipmentContext context = mapper.selectShipmentContextAdmin(orderId);
        if (context == null) throw new BusinessException("V1.2订单不存在");
        requireExpressPaperOrder(context);
        V11ShipmentRecord local = ensureLocalRecord(context);
        V11ShipmentView closed = closeUnshippedRefundedOrder(context, local);
        return closed == null ? syncContext(context, true) : closed;
    }

    public V11ShipmentView sync(Long orderId, Long userId) {
        return syncContext(requireUserContext(orderId, userId), false);
    }

    public V11ShipmentView syncAdmin(Long orderId) {
        V11ShipmentContext context = mapper.selectShipmentContextAdmin(orderId);
        if (context == null) throw new BusinessException("V1.2订单不存在");
        return syncContext(context, true);
    }

    public V11ShipmentBatchResult syncPending(int requestedLimit, int requestedStaleMinutes, int requestedDeliveredStaleMinutes) {
        int limit = Math.max(1, Math.min(MAX_BATCH, requestedLimit));
        int staleMinutes = Math.max(0, Math.min(1440, requestedStaleMinutes));
        int deliveredStaleMinutes = Math.max(0, Math.min(10080, requestedDeliveredStaleMinutes));
        LocalDateTime now = LocalDateTime.now();
        List<V11ShipmentContext> contexts = mapper.selectPendingShipmentContexts(
                limit, now.minusMinutes(staleMinutes), now.minusMinutes(deliveredStaleMinutes));
        List<Long> successes = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        for (V11ShipmentContext context : contexts) {
            try {
                syncContext(context, true);
                successes.add(context.getOrderId());
            } catch (RuntimeException e) {
                failures.add(context.getOrderId() + ": " + safeMessage(e));
            }
        }
        return new V11ShipmentBatchResult(contexts.size(), successes.size(), failures.size(), successes, failures);
    }

    private V11ShipmentView syncContext(V11ShipmentContext context, boolean admin) {
        requireExpressPaperOrder(context);
        V11ShipmentRecord local = ensureLocalRecord(context);
        V11ShipmentView refundedClosed = closeUnshippedRefundedOrder(context, local);
        if (refundedClosed != null) return refundedClosed;
        if ("CANCELED".equals(context.getOrderStatus())) {
            markNotRequired(context.getBridgeId());
            return view(context, mapper.selectShipmentByBridgeId(context.getBridgeId()), false,
                    List.of("订单已取消，不再查询第三方物流"));
        }
        if (context.getProviderOrderId() == null || context.getProviderOrderId().isBlank()) {
            throw new BusinessException("第三方订单尚未创建，不能同步物流");
        }
        if ("WAIT_PAY".equals(context.getOrderStatus()) && !admin) {
            return view(context, local, capabilityHint(context), List.of("订单尚未支付，物流通常不会生成"));
        }

        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(context.getProviderCode());
        ProviderCapabilities capabilities;
        try {
            capabilities = invoker.invoke(target, TicketSourceOperation.HEALTH, (adapter, ctx) -> adapter.capabilities(ctx));
        } catch (RuntimeException e) {
            markFailure(context, e);
            throw invoker.translate("查询第三方物流能力", e);
        }
        if (capabilities == null || !capabilities.shipmentQuery()) {
            BusinessException error = new BusinessException("当前票源未开放物流查询能力");
            markFailure(context, error);
            throw error;
        }

        ProviderShipment providerShipment;
        try {
            providerShipment = invoker.invoke(target, TicketSourceOperation.GET_SHIPMENT,
                    (adapter, ctx) -> adapter.getShipment(ctx, context.getProviderOrderId()));
            validateProviderShipment(local, providerShipment);
        } catch (RuntimeException e) {
            markFailure(context, e);
            throw invoker.translate("同步第三方物流", e);
        }

        LocalDateTime syncTime = LocalDateTime.now();
        mapper.upsertProviderShipment(
                context.getBridgeId(), providerShipment.shipmentStatus().name(),
                blankToNull(providerShipment.carrierCode()), blankToNull(providerShipment.carrierName()),
                blankToNull(providerShipment.waybillNo()), blankToNull(providerShipment.trackingUrl()),
                local(providerShipment.shippedAt()), local(providerShipment.signedAt()), syncTime,
                blankToNull(providerShipment.version()));
        return view(context, mapper.selectShipmentByBridgeId(context.getBridgeId()), true, List.of());
    }

    private V11ShipmentContext requireUserContext(Long orderId, Long userId) {
        if (orderId == null || orderId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException("orderId和userId不能为空");
        }
        V11ShipmentContext context = mapper.selectShipmentContext(orderId, userId);
        if (context == null) throw new BusinessException("订单不存在或不属于当前用户");
        requireExpressPaperOrder(context);
        return context;
    }

    private void requireExpressPaperOrder(V11ShipmentContext context) {
        if (!"PAPER_TICKET".equals(context.getDeliveryType())) {
            throw new BusinessException("只有纸质票订单具有物流信息");
        }
        if (!"EXPRESS".equals(context.getDeliveryMode())) {
            throw new BusinessException("纸质票现场取票订单不使用快递物流");
        }
    }

    private V11ShipmentRecord ensureLocalRecord(V11ShipmentContext context) {
        V11ShipmentRecord record = mapper.selectShipmentByBridgeId(context.getBridgeId());
        if (record == null) {
            mapper.insertWaitShipment(context.getBridgeId());
            record = mapper.selectShipmentByBridgeId(context.getBridgeId());
        }
        if (record == null) throw new BusinessException("初始化本地物流记录失败");
        return record;
    }

    private V11ShipmentView closeUnshippedRefundedOrder(V11ShipmentContext context, V11ShipmentRecord local) {
        if (!"REFUND_SUCCESS".equals(context.getOrderStatus())) return null;
        ShipmentStatus current = parseStatus(local == null ? null : local.getShipmentStatus());
        if (current != ShipmentStatus.WAIT_SHIPMENT && current != ShipmentStatus.NOT_REQUIRED) return null;
        if (current == ShipmentStatus.WAIT_SHIPMENT) {
            mapper.markShipmentNotRequired(context.getBridgeId(), LocalDateTime.now());
            local = mapper.selectShipmentByBridgeId(context.getBridgeId());
        }
        return view(context, local, false, List.of("订单已退款且尚未发货，物流已关闭"));
    }

    private boolean capabilityHint(V11ShipmentContext context) {
        try {
            V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(context.getProviderCode());
            ProviderCapabilities capabilities = invoker.invoke(target, TicketSourceOperation.HEALTH,
                    (adapter, ctx) -> adapter.capabilities(ctx));
            return capabilities != null && capabilities.shipmentQuery();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private void validateProviderShipment(V11ShipmentRecord local, ProviderShipment provider) {
        if (provider == null || provider.shipmentStatus() == null) {
            throw new BusinessException("第三方未返回物流状态");
        }
        ShipmentStatus next = provider.shipmentStatus();
        if (V11ShipmentTransitionPolicy.requiresWaybill(next) && blankToNull(provider.waybillNo()) == null) {
            throw new BusinessException("第三方物流已发货但未返回运单号");
        }
        ShipmentStatus current = parseStatus(local == null ? null : local.getShipmentStatus());
        if (!V11ShipmentTransitionPolicy.canTransition(current, next)) {
            throw new BusinessException("第三方物流状态发生倒退: " + current + " -> " + next);
        }
    }

    private V11ShipmentView view(V11ShipmentContext context,
                                 V11ShipmentRecord record,
                                 boolean syncSupported,
                                 List<String> warnings) {
        ShipmentStatus status = parseStatus(record.getShipmentStatus());
        RefundHint hint = refundHint(context, status);
        return new V11ShipmentView(
                context.getOrderId(), context.getOrderNo(), context.getDeliveryType(),
                zero(context.getDeliveryFeeAmount()), status,
                record.getCarrierCode(), record.getCarrierName(), record.getWaybillNo(), record.getTrackingUrl(),
                record.getShippedTime(), record.getSignedTime(), record.getLastSyncTime(), syncSupported,
                hint.refundable(), hint.reason(), record.getLastSyncStatus(), warnings);
    }

    private RefundHint refundHint(V11ShipmentContext context, ShipmentStatus status) {
        BigDecimal fee = zero(context.getDeliveryFeeAmount());
        if (fee.signum() <= 0) return new RefundHint(false, "订单没有快递费");
        if (!Boolean.TRUE.equals(context.getDeliveryFeeRuleRefundable())) {
            return new RefundHint(false, "本地同步规则标记快递费不可退，最终以第三方退款试算为准");
        }
        if (status == ShipmentStatus.WAIT_SHIPMENT) {
            return new RefundHint(true, "当前尚未发货，规则允许退快递费；最终以第三方退款试算为准");
        }
        return new RefundHint(false, "物流已进入发货或后续状态，快递费是否可退以第三方退款试算为准");
    }

    private void markFailure(V11ShipmentContext context, RuntimeException e) {
        ensureLocalRecord(context);
        String code = e instanceof V11AdapterException adapterException
                ? adapterException.getErrorCode().name() : "SHIPMENT_SYNC_FAILED";
        mapper.markSyncFailure(context.getBridgeId(), code, safeMessage(e), LocalDateTime.now());
    }

    private ShipmentStatus parseStatus(String value) {
        if (value == null || value.isBlank()) return ShipmentStatus.WAIT_SHIPMENT;
        try { return ShipmentStatus.valueOf(value); }
        catch (IllegalArgumentException ignored) { return ShipmentStatus.WAIT_SHIPMENT; }
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2);
    }

    private LocalDateTime local(OffsetDateTime value) {
        return value == null ? null : value.withOffsetSameInstant(ZoneOffset.ofHours(8)).toLocalDateTime();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeMessage(Throwable e) {
        String value = e == null ? null : e.getMessage();
        if (value == null || value.isBlank()) return "未知错误";
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private record RefundHint(boolean refundable, String reason) {}
}
