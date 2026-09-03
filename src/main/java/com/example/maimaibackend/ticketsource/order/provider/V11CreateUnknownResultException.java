package com.example.maimaibackend.ticketsource.order.provider;

import com.example.maimaibackend.common.BusinessException;

/**
 * Provider createOrder 可能已经成功，但麦麦没有拿到可靠结果。
 *
 * <p>这是 R5 的 G2 专用异常。它不是“创建失败”，调用方不得取消本地订单，
 * 也不得再次发起 createOrder；只能用 orderId 对应的商户订单号 / 创建幂等键补查。</p>
 */
public class V11CreateUnknownResultException extends BusinessException {
    private final Long orderId;
    private final String recoveryStatus;

    public V11CreateUnknownResultException(Long orderId, String recoveryStatus, String message) {
        super(503, message);
        this.orderId = orderId;
        this.recoveryStatus = recoveryStatus;
    }

    public Long getOrderId() { return orderId; }
    public String getRecoveryStatus() { return recoveryStatus; }
}
