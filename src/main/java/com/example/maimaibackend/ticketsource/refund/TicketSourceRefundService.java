package com.example.maimaibackend.ticketsource.refund;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceRefundMapper;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceGateway;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCallResult;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefund;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefundRequest;
import com.example.maimaibackend.ticketsource.refund.model.TicketSourceRefundBatchResult;
import com.example.maimaibackend.ticketsource.refund.model.TicketSourceRefundBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketSourceRefundService {
    private final TicketSourceRefundMapper mapper;
    private final TicketSourceGateway gateway;
    private final TicketSourceRefundProperties properties;
    private final TransactionTemplate transactionTemplate;

    public TicketSourceRefundService(TicketSourceRefundMapper mapper,
                                     TicketSourceGateway gateway,
                                     TicketSourceRefundProperties properties,
                                     PlatformTransactionManager transactionManager) {
        this.mapper = mapper;
        this.gateway = gateway;
        this.properties = properties;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** 对本地兼容订单返回 false；第三方订单创建待审核桥接并返回 true。 */
    public boolean createPendingBridge(Long refundId, Long orderId, String refundNo) {
        validateId(refundId, "refundId");
        validateId(orderId, "orderId");
        if (refundNo == null || refundNo.isBlank()) throw new BusinessException("refundNo 不能为空");
        mapper.insertPendingBridge(refundId, orderId, refundNo.trim(), Math.max(1, properties.getMaxRetryCount()));
        if (mapper.selectByRefundId(refundId) != null) {
            return true;
        }
        if (mapper.countTicketSourceOrder(orderId) > 0) {
            throw new BusinessException("第三方履约订单退款桥接创建失败，请检查订单桥接和票源配置");
        }
        return false;
    }

    public boolean isTicketSourceRefund(Long refundId) {
        return refundId != null && mapper.selectByRefundId(refundId) != null;
    }

    public TicketSourceRefundBridge getBridge(Long refundId) {
        validateId(refundId, "refundId");
        TicketSourceRefundBridge bridge = mapper.selectByRefundId(refundId);
        if (bridge == null) throw new BusinessException("第三方退款桥接不存在");
        return bridge;
    }

    /** 管理员审核通过：只启动/推进第三方退款，不提前篡改本地退款终态。 */
    public TicketSourceRefundBridge approveAndProcess(Long refundId) {
        TicketSourceRefundBridge bridge = getBridge(refundId);
        if ("SUCCESS".equals(bridge.getBridgeStatus())) return bridge;
        if ("REJECTED".equals(bridge.getBridgeStatus())) throw new BusinessException("该退款已被驳回");
        if (!"REFUNDING".equals(bridge.getLocalRefundStatus())
                || !"REFUNDING".equals(bridge.getLocalOrderStatus())) {
            throw new BusinessException("本地退款或订单状态已变化，不能发起第三方退款");
        }
        return process(refundId, true, true);
    }

    public TicketSourceRefundBridge processRefund(Long refundId) {
        return process(refundId, true, false);
    }

    /**
     * 管理后台“同步供应商售后状态”专用入口。
     * 必须已经取得 providerRefundId，只执行 GET_REFUND，绝不在这里首次 REQUEST_REFUND。
     */
    public TicketSourceRefundBridge syncProviderStatus(Long refundId) {
        TicketSourceRefundBridge bridge = getBridge(refundId);
        if ("SUCCESS".equals(bridge.getBridgeStatus()) || "REJECTED".equals(bridge.getBridgeStatus())) {
            return bridge;
        }
        if (bridge.getProviderRefundId() == null || bridge.getProviderRefundId().isBlank()) {
            throw new BusinessException("供应商售后尚未成功提交，当前只能等待自动协同或通过退款审核流程处理");
        }
        return process(refundId, true, false);
    }

    /** 只允许异常任务显式重试；PENDING_REVIEW 仍必须走标准退款审核入口。 */
    public TicketSourceRefundBridge retryException(Long refundId) {
        TicketSourceRefundBridge bridge = getBridge(refundId);
        if (!("RETRY_WAIT".equals(bridge.getBridgeStatus()) || "MANUAL_REVIEW".equals(bridge.getBridgeStatus())
                || "FAILED".equals(bridge.getBridgeStatus()))) {
            throw new BusinessException("当前售后协同状态不允许手动重试");
        }
        return process(refundId, true, false);
    }

    public TicketSourceRefundBatchResult processDue(Integer rawLimit) {
        int limit = rawLimit == null ? 50 : Math.max(1, Math.min(rawLimit, 200));
        List<TicketSourceRefundBridge> due = mapper.selectDueRefunds(LocalDateTime.now(), limit);
        TicketSourceRefundBatchResult result = new TicketSourceRefundBatchResult();
        result.setRequestedCount(limit);
        result.setProcessedCount(0);
        result.setSuccessCount(0);
        result.setPendingCount(0);
        result.setFailedCount(0);
        result.setRefundIds(due.stream().map(TicketSourceRefundBridge::getRefundId).toList());
        for (TicketSourceRefundBridge item : due) {
            try {
                TicketSourceRefundBridge current = process(item.getRefundId(), false, false);
                result.setProcessedCount(result.getProcessedCount() + 1);
                if ("SUCCESS".equals(current.getBridgeStatus())) {
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } else if ("MANUAL_REVIEW".equals(current.getBridgeStatus())) {
                    result.setFailedCount(result.getFailedCount() + 1);
                } else {
                    result.setPendingCount(result.getPendingCount() + 1);
                }
            } catch (Exception ignored) {
                result.setProcessedCount(result.getProcessedCount() + 1);
                result.setFailedCount(result.getFailedCount() + 1);
            }
        }
        return result;
    }

    public void rejectBeforeProvider(Long refundId) {
        TicketSourceRefundBridge bridge = getBridge(refundId);
        if ("REJECTED".equals(bridge.getBridgeStatus())) return;
        if (!"PENDING_REVIEW".equals(bridge.getBridgeStatus())
                || bridge.getProviderRefundId() != null) {
            throw new BusinessException("第三方退款已发起，不能直接驳回，请先人工处理第三方退款");
        }
        if (mapper.markRejected(refundId) != 1) {
            throw new BusinessException("退款桥接状态已变化，请刷新后重试");
        }
    }

    private TicketSourceRefundBridge process(Long refundId, boolean force, boolean allowPendingReview) {
        TicketSourceRefundBridge bridge = getBridge(refundId);
        if ("SUCCESS".equals(bridge.getBridgeStatus()) || "REJECTED".equals(bridge.getBridgeStatus())) {
            return bridge;
        }
        if ("PENDING_REVIEW".equals(bridge.getBridgeStatus()) && !allowPendingReview) {
            throw new BusinessException("退款尚未审核通过，不能直接调用第三方退款处理接口");
        }
        if (Boolean.TRUE.equals(bridge.getManualHold()) && !force) {
            throw new BusinessException("退款任务处于人工处理状态");
        }
        if (!force && bridge.getNextAttemptTime() != null
                && bridge.getNextAttemptTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("退款任务尚未到下次执行时间");
        }

        TicketSourceCallResult<TicketSourceRefund> call;
        String operation;
        if (bridge.getProviderRefundId() == null || bridge.getProviderRefundId().isBlank()) {
            operation = "REQUEST_REFUND";
            if (mapper.markRequesting(refundId) != 1) {
                throw new BusinessException("退款任务正在处理或状态已变化");
            }
            TicketSourceRefundRequest request = new TicketSourceRefundRequest();
            request.setClientRefundNo(bridge.getRefundNo());
            request.setRefundAmount(bridge.getRefundAmount());
            request.setCurrencyCode(bridge.getCurrencyCode());
            request.setReason("本地退款审核通过");
            request.setIdempotencyKey(bridge.getRequestIdempotencyKey());
            call = gateway.requestRefund(bridge.getProviderCode(), bridge.getProviderOrderId(), request);
        } else {
            operation = "GET_REFUND";
            call = gateway.getRefund(bridge.getProviderCode(), bridge.getProviderRefundId());
        }

        if (call == null || !call.isSuccess() || call.getData() == null) {
            finalizeFailure(refundId, bridge, call, operation);
        } else {
            finalizeProviderRefund(refundId, bridge, call.getData(), operation);
        }
        return getBridge(refundId);
    }

    private void finalizeProviderRefund(Long refundId, TicketSourceRefundBridge before,
                                        TicketSourceRefund providerRefund, String operation) {
        String providerStatus = normalizeStatus(providerRefund.getRefundStatus());
        if ("SUCCESS".equals(providerStatus)) {
            LocalDateTime refundTime = providerRefund.getRefundTime() == null
                    ? LocalDateTime.now() : providerRefund.getRefundTime();
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    requireOne(mapper.markProviderProgress(refundId, providerRefund.getProviderRefundId(),
                            providerRefund.getProviderRefundNo(), "SUCCESS", "SUCCESS", null,
                            LocalDateTime.now(), refundTime, snapshot(providerRefund), operation),
                            "第三方退款桥接终态更新失败");
                    requireOne(mapper.finalizeLocalRefund(refundId, before.getOrderId(), refundTime),
                            "本地退款记录终态更新失败");
                    requireOne(mapper.finalizeLocalOrder(before.getOrderId(), refundTime),
                            "本地订单退款终态更新失败");
                    if (mapper.finalizeLocalTickets(before.getOrderId(), refundTime) <= 0) {
                        throw new IllegalStateException("本地电子票退款失效失败");
                    }
                    requireOne(mapper.markOrderBridgeRefunded(before.getOrderId(), refundTime),
                            "第三方订单桥接退款终态更新失败");
                    mapper.stopIssueTaskForRefund(before.getOrderId());
                });
            } catch (RuntimeException ex) {
                TicketSourceRefundBridge current = mapper.selectByRefundId(refundId);
                if (current != null && "SUCCESS".equals(current.getBridgeStatus())
                        && "REFUND_SUCCESS".equals(current.getLocalRefundStatus())
                        && "REFUND_SUCCESS".equals(current.getLocalOrderStatus())) {
                    return;
                }
                mapper.markProviderProgress(refundId, providerRefund.getProviderRefundId(),
                        providerRefund.getProviderRefundNo(), "MANUAL_REVIEW", "SUCCESS", null,
                        LocalDateTime.now(), refundTime, snapshot(providerRefund), operation);
                mapper.markFailure(refundId, "MANUAL_REVIEW", "LOCAL_REFUND_FINALIZE_FAILED",
                        trimError(ex), false, null, true, operation);
            }
            return;
        }
        if ("PROCESSING".equals(providerStatus) || "PENDING".equals(providerStatus)) {
            LocalDateTime next = providerRefund.getNextPollTime();
            if (next == null || !next.isAfter(LocalDateTime.now())) {
                next = LocalDateTime.now().plusSeconds(Math.max(1, properties.getPollIntervalSeconds()));
            }
            mapper.markProviderProgress(refundId, providerRefund.getProviderRefundId(),
                    providerRefund.getProviderRefundNo(), "PROCESSING", providerStatus, next,
                    LocalDateTime.now(), null, snapshot(providerRefund), operation);
            mapper.markOrderBridgeRefunding(before.getOrderId());
            return;
        }
        String code = providerRefund.getErrorCode() == null ? "SOURCE_REFUND_FAILED" : providerRefund.getErrorCode();
        String message = providerRefund.getErrorMessage() == null ? "第三方拒绝或处理退款失败" : providerRefund.getErrorMessage();
        mapper.markProviderProgress(refundId, providerRefund.getProviderRefundId(),
                providerRefund.getProviderRefundNo(), "MANUAL_REVIEW", providerStatus, null,
                LocalDateTime.now(), providerRefund.getRefundTime(), snapshot(providerRefund), operation);
        mapper.markFailure(refundId, "MANUAL_REVIEW", code, message, false, null, true, operation);
    }

    private void finalizeFailure(Long refundId, TicketSourceRefundBridge before,
                                 TicketSourceCallResult<?> call, String operation) {
        boolean retryable = call != null && call.isRetryable();
        int retry = (before.getRetryCount() == null ? 0 : before.getRetryCount()) + 1;
        int max = before.getMaxRetryCount() == null ? properties.getMaxRetryCount() : before.getMaxRetryCount();
        boolean manual = !retryable || retry >= max;
        LocalDateTime next = manual ? null : LocalDateTime.now().plusSeconds(backoffSeconds(retry));
        String code = call == null ? "SOURCE_CALL_EMPTY"
                : call.getProviderErrorCode() != null ? call.getProviderErrorCode()
                : call.getErrorCode() == null ? "SOURCE_UNKNOWN_ERROR" : call.getErrorCode().name();
        String message = call == null || call.getMessage() == null ? "第三方票源无响应" : call.getMessage();
        mapper.markFailure(refundId, manual ? "MANUAL_REVIEW" : "RETRY_WAIT",
                code, operation + ": " + message, retryable, next, manual, operation);
    }

    private void requireOne(int affectedRows, String message) {
        if (affectedRows != 1) throw new IllegalStateException(message + "，affectedRows=" + affectedRows);
    }

    private String trimError(RuntimeException ex) {
        String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        return message.length() <= 450 ? message : message.substring(0, 450);
    }

    private long backoffSeconds(int retry) {
        long base = Math.max(1, properties.getRetryBaseSeconds());
        return Math.min(300, base * (1L << Math.min(5, Math.max(0, retry - 1))));
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "UNKNOWN" : status.trim().toUpperCase();
    }

    private String snapshot(TicketSourceRefund r) {
        return "providerRefundId=" + r.getProviderRefundId()
                + ",status=" + r.getRefundStatus()
                + ",amount=" + r.getRefundAmount()
                + ",version=" + r.getDataVersion();
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) throw new BusinessException(name + " 必须为正整数");
    }
}
