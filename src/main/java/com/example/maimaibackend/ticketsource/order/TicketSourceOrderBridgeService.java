package com.example.maimaibackend.ticketsource.order;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceOrderMapper;
import com.example.maimaibackend.ticketsource.order.model.TicketSourceExpireResult;
import com.example.maimaibackend.ticketsource.order.model.TicketSourceOrderBridge;
import com.example.maimaibackend.ticketsource.order.provider.V11OrderService;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderActionRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 第三方 WAIT_PAY 订单超时释放入口。
 *
 * <p>V1.3 订单是通过 V11/V12 Adapter 创建的，因此超时取消也必须回到同一 Adapter 边界。
 * 旧实现通过 legacy TicketSourceGateway 取消，会把 MOCK-V11-* 订单交给旧 MOCK-ORDER-* 模拟器，
 * 造成 ADAPTER_NOT_FOUND / providerOrderId 不存在，并让历史 WAIT_PAY 长期残留。</p>
 */
@Service
public class TicketSourceOrderBridgeService {
    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 500;

    private final TicketSourceOrderMapper orderMapper;
    private final V11OrderService v11OrderService;

    public TicketSourceOrderBridgeService(TicketSourceOrderMapper orderMapper,
                                          V11OrderService v11OrderService) {
        this.orderMapper = orderMapper;
        this.v11OrderService = v11OrderService;
    }

    public TicketSourceOrderBridge getBridge(Long orderId) {
        validateOrderId(orderId);
        TicketSourceOrderBridge bridge = orderMapper.selectBridgeByOrderId(orderId);
        if (bridge == null) throw new BusinessException("第三方订单映射不存在");
        return bridge;
    }

    public TicketSourceExpireResult expireDue(Integer limit) {
        int safeLimit = normalizeLimit(limit);
        List<TicketSourceOrderBridge> dueOrders = orderMapper.selectDueWaitPayOrders(safeLimit);
        if (dueOrders == null) dueOrders = Collections.emptyList();

        TicketSourceExpireResult result = new TicketSourceExpireResult();
        result.setScannedCount(dueOrders.size());
        for (TicketSourceOrderBridge bridge : dueOrders) {
            if (expireOneViaV11(bridge)) {
                result.getExpiredOrderIds().add(bridge.getOrderId());
            } else {
                result.getFailedOrderIds().add(bridge.getOrderId());
            }
        }
        result.setExpiredCount(result.getExpiredOrderIds().size());
        result.setFailedCount(result.getFailedOrderIds().size());
        return result;
    }

    public TicketSourceOrderBridge expireOrder(Long orderId) {
        TicketSourceOrderBridge bridge = getBridge(orderId);
        if (isCreateResultUnresolved(bridge)) {
            throw new BusinessException("创建结果尚未确认，禁止超时取消，请先补查创建结果");
        }
        if (!expireOneViaV11(bridge)) {
            throw new BusinessException("释放第三方预占失败，请查看桥接错误后重试");
        }
        return getBridge(orderId);
    }


    private boolean isCreateResultUnresolved(TicketSourceOrderBridge bridge) {
        if (bridge == null) return false;
        if ("UNKNOWN_RESULT".equals(bridge.getBridgeStatus())) return true;
        return "MANUAL_REVIEW".equals(bridge.getBridgeStatus())
                && ("CREATE_ORDER_LOOKUP".equals(bridge.getLastOperation())
                || (bridge.getCreateRecoveryAttempts() != null && bridge.getCreateRecoveryAttempts() > 0)
                || bridge.getUnknownResultSince() != null);
    }

    private boolean expireOneViaV11(TicketSourceOrderBridge bridge) {
        if (bridge == null || bridge.getOrderId() == null || bridge.getUserId() == null) return false;
        if ("CANCELED".equals(bridge.getLocalOrderStatus())
                || "CANCELED".equals(bridge.getBridgeStatus())
                || "EXPIRED".equals(bridge.getBridgeStatus())) {
            return true;
        }
        try {
            v11OrderService.cancel(
                    bridge.getOrderId(),
                    new V11OrderActionRequest(bridge.getUserId(), null, "PAY_TIMEOUT")
            );
            TicketSourceOrderBridge updated = orderMapper.selectBridgeByOrderId(bridge.getOrderId());
            return updated != null
                    && "CANCELED".equals(updated.getLocalOrderStatus())
                    && ("CANCELED".equals(updated.getBridgeStatus()) || "EXPIRED".equals(updated.getBridgeStatus()));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) return DEFAULT_LIMIT;
        if (limit <= 0) throw new BusinessException("limit 必须大于 0");
        return Math.min(limit, MAX_LIMIT);
    }

    private void validateOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) throw new BusinessException("orderId 无效");
    }
}
