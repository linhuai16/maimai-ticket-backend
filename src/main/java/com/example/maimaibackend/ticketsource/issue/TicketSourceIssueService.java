package com.example.maimaibackend.ticketsource.issue;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceIssueMapper;
import com.example.maimaibackend.service.admin.AdminTicketLogService;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceGateway;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCallResult;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCredential;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceDelivery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceIssueRequest;
import com.example.maimaibackend.ticketsource.issue.model.TicketSourceIssueBatchResult;
import com.example.maimaibackend.ticketsource.issue.model.TicketSourceIssueTask;
import com.example.maimaibackend.ticketsource.issue.model.TicketSourceLocalTicket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketSourceIssueService {
    private final TicketSourceIssueMapper mapper;
    private final TicketSourceGateway gateway;
    private final TicketSourceIssueProperties properties;
    private final AdminTicketLogService logService;
    private final TransactionTemplate transactionTemplate;

    public TicketSourceIssueService(
            TicketSourceIssueMapper mapper,
            TicketSourceGateway gateway,
            TicketSourceIssueProperties properties,
            AdminTicketLogService logService,
            PlatformTransactionManager transactionManager
    ) {
        this.mapper = mapper;
        this.gateway = gateway;
        this.properties = properties;
        this.logService = logService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void createTask(Long orderId, Long bridgeId, Long providerId, String providerOrderId,
                           int expectedCount, String orderNo, LocalDateTime now) {
        if (orderId == null || bridgeId == null || providerId == null || providerOrderId == null
                || expectedCount <= 0) {
            throw new BusinessException("创建第三方出票任务参数不完整");
        }
        LocalDateTime createTime = now == null ? LocalDateTime.now() : now;
        mapper.insertIssueTask(orderId, bridgeId, providerId, providerOrderId, expectedCount,
                "FULFILL:" + orderNo, properties.getMaxRetryCount(),
                createTime.plusSeconds(Math.max(0, properties.getInitialDelaySeconds())), createTime);
    }

    public TicketSourceIssueTask getTask(Long orderId) {
        validateOrderId(orderId);
        TicketSourceIssueTask task = mapper.selectTaskByOrderId(orderId);
        if (task == null) throw new BusinessException("第三方出票任务不存在");
        return task;
    }

    public TicketSourceIssueTask processOrder(Long orderId) {
        return processOrder(orderId, true);
    }

    public TicketSourceIssueTask retryOrder(Long orderId) {
        validateOrderId(orderId);
        transactionTemplate.executeWithoutResult(status -> {
            TicketSourceIssueTask task = requireTaskForUpdate(orderId);
            if ("SUCCESS".equals(task.getTaskStatus())) throw new BusinessException("订单已经全部出票成功");
            if (!("FAILED".equals(task.getTaskStatus()) || "PARTIAL".equals(task.getTaskStatus())
                    || "MANUAL_REVIEW".equals(task.getTaskStatus()) || "RETRY_WAIT".equals(task.getTaskStatus()))) {
                throw new BusinessException("当前出票任务状态不允许手动重试");
            }
            LocalDateTime now = LocalDateTime.now();
            mapper.resetFailedTickets(orderId, now);
            if (mapper.resetTaskForRetry(orderId, now, now) != 1) {
                throw new BusinessException("重置出票任务失败，请刷新后重试");
            }
        });
        logService.recordSuccess(TicketOperationContext.system("TicketSourceIssueService"),
                "ISSUE", "RETRY_SOURCE_ISSUE", "ORDER", orderId, orderId, null,
                "ERROR", "GENERATING", "管理员触发第三方出票重试", null);
        return processOrder(orderId, true);
    }

    /**
     * 管理后台“同步供应商状态”专用入口。
     * 只允许对已经向供应商发起过履约的任务执行 GET_TICKETS，绝不在这里首次 REQUEST_TICKETS。
     */
    public TicketSourceIssueTask syncProviderStatus(Long orderId) {
        TicketSourceIssueTask current = getTask(orderId);
        if ("SUCCESS".equals(current.getTaskStatus())) return current;
        if (!Boolean.TRUE.equals(current.getRequestSent())) {
            throw new BusinessException("该履约任务尚未向供应商发起，等待系统自动履约；管理后台不能手工首次发起履约");
        }
        return processOrder(orderId, true);
    }

    public TicketSourceIssueBatchResult processDue(Integer rawLimit) {
        int limit = rawLimit == null ? 50 : Math.max(1, Math.min(rawLimit, 200));
        LocalDateTime now = LocalDateTime.now();
        List<Long> orderIds = mapper.selectDueOrderIds(now, processingCutoff(now), limit);
        TicketSourceIssueBatchResult result = new TicketSourceIssueBatchResult();
        result.setRequestedCount(limit);
        result.setProcessedCount(0);
        result.setSuccessCount(0);
        result.setPendingCount(0);
        result.setFailedCount(0);
        result.setOrderIds(orderIds);
        for (Long orderId : orderIds) {
            try {
                TicketSourceIssueTask task = processOrder(orderId, false);
                result.setProcessedCount(result.getProcessedCount() + 1);
                if ("SUCCESS".equals(task.getTaskStatus())) {
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } else if ("MANUAL_REVIEW".equals(task.getTaskStatus()) || "FAILED".equals(task.getTaskStatus())) {
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

    private TicketSourceIssueTask processOrder(Long orderId, boolean force) {
        validateOrderId(orderId);
        TicketSourceIssueTask prepared = transactionTemplate.execute(status -> prepare(orderId, force));
        if (prepared == null) throw new BusinessException("出票任务准备失败");
        if ("SUCCESS".equals(prepared.getTaskStatus())) return prepared;

        TicketSourceCallResult<TicketSourceDelivery> call;
        String operation = Boolean.TRUE.equals(prepared.getRequestSent())
                ? "GET_TICKETS" : "REQUEST_TICKETS";
        try {
            if ("GET_TICKETS".equals(operation)) {
                call = gateway.getTickets(prepared.getProviderCode(), prepared.getProviderOrderId());
            } else {
                TicketSourceIssueRequest request = new TicketSourceIssueRequest();
                request.setClientOrderNo(prepared.getOrderNo());
                request.setExpectedTicketCount(prepared.getExpectedTicketCount());
                request.setIdempotencyKey(prepared.getIssueIdempotencyKey());
                call = gateway.requestTickets(prepared.getProviderCode(), prepared.getProviderOrderId(), request);
            }

            if (call == null || !call.isSuccess() || call.getData() == null) {
                finalizeCallFailure(orderId, prepared, call, operation);
            } else {
                finalizeDelivery(orderId, prepared, call.getData(), operation);
            }
        } catch (RuntimeException e) {
            finalizeUnexpectedFailure(orderId, operation, e);
        }
        return getTask(orderId);
    }

    private TicketSourceIssueTask prepare(Long orderId, boolean force) {
        TicketSourceIssueTask task = requireTaskForUpdate(orderId);
        if ("SUCCESS".equals(task.getTaskStatus())) return task;
        if (!"WAIT_USE".equals(task.getOrderStatus()) || !"PROVIDER_CONFIRMED".equals(task.getPaymentStatus())) {
            throw new BusinessException("本地订单未处于第三方已确认支付的待使用状态");
        }
        if (Boolean.TRUE.equals(task.getManualHold()) && !force) {
            throw new BusinessException("出票任务处于人工处理状态");
        }
        if (!force && task.getNextAttemptTime() != null && task.getNextAttemptTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("出票任务尚未到下次执行时间");
        }
        LocalDateTime now = LocalDateTime.now();
        if (mapper.markProcessing(orderId, now, processingCutoff(now)) != 1) {
            throw new BusinessException("出票任务正在处理中或状态已变化");
        }
        task.setTaskStatus("PROCESSING");
        return task;
    }

    private void finalizeDelivery(Long orderId, TicketSourceIssueTask prepared,
                                  TicketSourceDelivery delivery, String operation) {
        transactionTemplate.executeWithoutResult(status -> {
            TicketSourceIssueTask current = requireTaskForUpdate(orderId);
            if ("SUCCESS".equals(current.getTaskStatus())) return;
            if (delivery.getExpectedTicketCount() == null
                    || !delivery.getExpectedTicketCount().equals(current.getExpectedTicketCount())) {
                finalizeInvalidDelivery(current, "SOURCE_TICKET_COUNT_MISMATCH", "第三方出票数量与本地订单不一致");
                return;
            }
            if (delivery.getTickets() == null) {
                finalizeInvalidDelivery(current, "SOURCE_TICKET_LIST_EMPTY", "第三方出票结果未返回凭证列表");
                return;
            }
            List<TicketSourceLocalTicket> localTickets = mapper.selectLocalTickets(orderId);
            Map<Integer, TicketSourceLocalTicket> localByIndex = new HashMap<>();
            for (TicketSourceLocalTicket ticket : localTickets) localByIndex.put(ticket.getTicketIndex(), ticket);
            LocalDateTime now = LocalDateTime.now();
            for (TicketSourceCredential credential : delivery.getTickets()) {
                if (credential == null || credential.getTicketIndex() == null) continue;
                TicketSourceLocalTicket local = localByIndex.get(credential.getTicketIndex());
                if (local == null) continue;
                if ("ISSUED".equals(credential.getTicketStatus())) {
                    if (!isIssuedCredentialValid(credential)) {
                        finalizeInvalidDelivery(current, "SOURCE_CREDENTIAL_INVALID",
                                "第三方已出票凭证字段不完整，ticketIndex=" + credential.getTicketIndex());
                        return;
                    }
                    mapper.applyIssuedCredential(local.getTicketId(), current.getProviderId(),
                            current.getProviderOrderId(), credential.getProviderTicketId(),
                            credential.getCredentialType(), credential.getCredentialPayload(),
                            credential.getCredentialVersion(), buildSeatInfo(credential),
                            credential.getSeatZone(), credential.getSeatRow(), credential.getSeatNumber(),
                            credential.getEntranceInfo(),
                            credential.getIssueTime() == null ? now : credential.getIssueTime(),
                            credential.getExpireTime(), now);
                } else if ("FAILED".equals(credential.getTicketStatus())) {
                    String reason = safeCredentialError(credential);
                    mapper.applyFailedCredential(local.getTicketId(), current.getProviderId(),
                            current.getProviderOrderId(), credential.getProviderTicketId(), reason, now);
                }
            }

            int issued = mapper.countTicketsByStatus(orderId, "UNUSED");
            int failed = mapper.countTicketsByStatus(orderId, "ERROR");
            String providerStatus = normalizeDeliveryStatus(delivery.getDeliveryStatus());
            if (issued == current.getExpectedTicketCount() && failed == 0) {
                mapper.updateOrderIssuedTime(orderId, now);
                mapper.updateTaskResult(orderId, "SUCCESS", "ISSUED", issued, 0,
                        current.getRetryCount(), null, now, delivery.getDataVersion(), operation,
                        null, null, false, true, false, now);
                logService.recordSuccess(TicketOperationContext.system("TicketSourceIssueService"),
                        "ISSUE", "SOURCE_ISSUE_SUCCESS", "ORDER", orderId, orderId, null,
                        "GENERATING", "ISSUED", "第三方自动出票成功，共同步 " + issued + " 张电子凭证", null);
                return;
            }
            if ("PENDING".equals(providerStatus) && failed == 0) {
                LocalDateTime next = delivery.getNextPollTime();
                if (next == null || !next.isAfter(now)) next = now.plusSeconds(properties.getPollIntervalSeconds());
                mapper.updateTaskResult(orderId, "WAIT_PROVIDER", providerStatus, issued, 0,
                        current.getRetryCount(), next, null, delivery.getDataVersion(), operation,
                        null, null, false, true, false, now);
                return;
            }
            int retry = current.getRetryCount() + 1;
            boolean exhausted = retry >= current.getMaxRetryCount();
            String taskStatus = exhausted ? "MANUAL_REVIEW" : (issued > 0 ? "PARTIAL" : "RETRY_WAIT");
            LocalDateTime next = exhausted ? null : now.plusSeconds(backoffSeconds(retry));
            String error = failed > 0 ? "SOURCE_TICKET_PARTIAL_FAILED" : "SOURCE_TICKET_INCOMPLETE";
            String message = "第三方出票未全部完成：已出票 " + issued + "，异常 " + failed;
            mapper.updateTaskResult(orderId, taskStatus, providerStatus, issued, failed, retry,
                    next, null, delivery.getDataVersion(), operation,
                    error, message, !exhausted, true, exhausted, now);
        });
    }

    private void finalizeCallFailure(Long orderId, TicketSourceIssueTask prepared,
                                     TicketSourceCallResult<?> call, String operation) {
        transactionTemplate.executeWithoutResult(status -> {
            TicketSourceIssueTask current = requireTaskForUpdate(orderId);
            int retry = current.getRetryCount() + 1;
            boolean retryable = call != null && call.isRetryable();
            boolean exhausted = retry >= current.getMaxRetryCount();
            boolean manual = !retryable || exhausted;
            String errorCode = sourceErrorCode(call);
            String message = safeCallMessage(call);
            LocalDateTime now = LocalDateTime.now();
            if (manual) mapper.markGeneratingTicketsError(orderId, errorCode + " - " + message, now);
            int issued = mapper.countTicketsByStatus(orderId, "UNUSED");
            int failed = mapper.countTicketsByStatus(orderId, "ERROR");
            mapper.updateTaskResult(orderId, manual ? "MANUAL_REVIEW" : "RETRY_WAIT",
                    current.getProviderDeliveryStatus(), issued, failed, retry,
                    manual ? null : now.plusSeconds(backoffSeconds(retry)), null,
                    current.getProviderDeliveryVersion(), operation, errorCode, message,
                    retryable, Boolean.TRUE.equals(current.getRequestSent()), manual, now);
        });
    }

    private void finalizeUnexpectedFailure(Long orderId, String operation, RuntimeException error) {
        transactionTemplate.executeWithoutResult(status -> {
            TicketSourceIssueTask current = requireTaskForUpdate(orderId);
            if ("SUCCESS".equals(current.getTaskStatus())) return;
            LocalDateTime now = LocalDateTime.now();
            String message = error.getMessage() == null || error.getMessage().isBlank()
                    ? "第三方出票处理发生未预期异常" : error.getMessage();
            mapper.markGeneratingTicketsError(orderId, "SOURCE_ISSUE_UNEXPECTED - " + message, now);
            int issued = mapper.countTicketsByStatus(orderId, "UNUSED");
            int failed = mapper.countTicketsByStatus(orderId, "ERROR");
            mapper.updateTaskResult(orderId, "MANUAL_REVIEW", current.getProviderDeliveryStatus(),
                    issued, failed, current.getRetryCount() + 1, null, null,
                    current.getProviderDeliveryVersion(), operation, "SOURCE_ISSUE_UNEXPECTED",
                    message, false, Boolean.TRUE.equals(current.getRequestSent()), true, now);
        });
    }

    private void finalizeInvalidDelivery(TicketSourceIssueTask task, String code, String message) {
        LocalDateTime now = LocalDateTime.now();
        mapper.markGeneratingTicketsError(task.getOrderId(), message, now);
        int issued = mapper.countTicketsByStatus(task.getOrderId(), "UNUSED");
        int failed = mapper.countTicketsByStatus(task.getOrderId(), "ERROR");
        mapper.updateTaskResult(task.getOrderId(), "MANUAL_REVIEW", "INVALID", issued, failed,
                task.getRetryCount() + 1, null, null, task.getProviderDeliveryVersion(),
                "VALIDATE_DELIVERY", code, message, false,
                Boolean.TRUE.equals(task.getRequestSent()), true, now);
    }

    private TicketSourceIssueTask requireTaskForUpdate(Long orderId) {
        TicketSourceIssueTask task = mapper.selectTaskForUpdate(orderId);
        if (task == null) throw new BusinessException("第三方出票任务不存在");
        return task;
    }

    private boolean isIssuedCredentialValid(TicketSourceCredential credential) {
        return credential.getProviderTicketId() != null && !credential.getProviderTicketId().isBlank()
                && credential.getCredentialType() != null && !credential.getCredentialType().isBlank()
                && credential.getCredentialPayload() != null && !credential.getCredentialPayload().isBlank();
    }

    private String buildSeatInfo(TicketSourceCredential c) {
        StringBuilder value = new StringBuilder();
        appendSeat(value, c.getSeatZone());
        appendSeat(value, c.getSeatRow() == null ? null : c.getSeatRow() + "排");
        appendSeat(value, c.getSeatNumber() == null ? null : c.getSeatNumber() + "座");
        return value.length() == 0 ? null : value.toString();
    }

    private void appendSeat(StringBuilder target, String value) {
        if (value == null || value.isBlank()) return;
        if (target.length() > 0) target.append(' ');
        target.append(value.trim());
    }

    private String safeCredentialError(TicketSourceCredential c) {
        String code = c.getErrorCode() == null ? "SOURCE_SINGLE_TICKET_FAILED" : c.getErrorCode();
        String message = c.getErrorMessage() == null ? "第三方单票出票失败" : c.getErrorMessage();
        return code + " - " + message;
    }

    private String normalizeDeliveryStatus(String status) {
        if (status == null || status.isBlank()) return "UNKNOWN";
        return status.trim().toUpperCase();
    }

    private LocalDateTime processingCutoff(LocalDateTime now) {
        int seconds = Math.max(30, properties.getProcessingTimeoutSeconds());
        return now.minusSeconds(seconds);
    }

    private long backoffSeconds(int retry) {
        long base = Math.max(1, properties.getRetryBaseSeconds());
        return Math.min(300, base * (1L << Math.min(5, Math.max(0, retry - 1))));
    }

    private String sourceErrorCode(TicketSourceCallResult<?> call) {
        if (call == null) return "SOURCE_CALL_EMPTY";
        if (call.getProviderErrorCode() != null) return call.getProviderErrorCode();
        return call.getErrorCode() == null ? "SOURCE_UNKNOWN_ERROR" : call.getErrorCode().name();
    }

    private String safeCallMessage(TicketSourceCallResult<?> call) {
        return call == null || call.getMessage() == null ? "第三方票源无响应" : call.getMessage();
    }

    private void validateOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) throw new BusinessException("orderId 必须为正整数");
    }
}
