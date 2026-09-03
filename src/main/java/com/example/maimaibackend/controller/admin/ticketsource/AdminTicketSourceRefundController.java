package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.refund.TicketSourceRefundService;
import com.example.maimaibackend.ticketsource.refund.model.TicketSourceRefundBatchResult;
import com.example.maimaibackend.ticketsource.refund.model.TicketSourceRefundBridge;
import com.example.maimaibackend.ticketsource.workflow.TicketSourceWorkflowService;
import com.example.maimaibackend.ticketsource.workflow.model.V12BatchResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ticket-source-refunds")
public class AdminTicketSourceRefundController {
    private final TicketSourceRefundService service;
    private final TicketSourceWorkflowService workflowService;

    public AdminTicketSourceRefundController(TicketSourceRefundService service, TicketSourceWorkflowService workflowService) {
        this.service = service;
        this.workflowService = workflowService;
    }

    @GetMapping("/{refundId}")
    public Result<TicketSourceRefundBridge> detail(@PathVariable Long refundId) {
        return Result.success(service.getBridge(refundId));
    }

    @PostMapping("/{refundId}/process")
    public Result<TicketSourceRefundBridge> process(@PathVariable Long refundId) {
        if (workflowService.isSingleSkuProviderRefund(refundId)) {
            java.util.Map<String,Object> view = workflowService.refundView(refundId);
            if ("PENDING_REVIEW".equals(String.valueOf(view.get("bridgeStatus")))) {
                throw new BusinessException("退款尚未审核通过，不能直接调用第三方退款处理接口");
            }
            workflowService.advanceRefund(refundId);
            return Result.success(service.getBridge(refundId));
        }
        return Result.success(service.processRefund(refundId));
    }

    @PostMapping("/{refundId}/sync-status")
    public Result<TicketSourceRefundBridge> syncStatus(@PathVariable Long refundId) {
        TicketSourceRefundBridge current = service.getBridge(refundId);
        if ("SUCCESS".equals(current.getBridgeStatus()) || "REJECTED".equals(current.getBridgeStatus())) {
            return Result.success(current);
        }
        if (workflowService.isSingleSkuProviderRefund(refundId)) {
            workflowService.syncRefund(refundId);
            return Result.success(service.getBridge(refundId));
        }
        return Result.success(service.syncProviderStatus(refundId));
    }

    @PostMapping("/{refundId}/retry")
    public Result<TicketSourceRefundBridge> retry(@PathVariable Long refundId) {
        if (workflowService.isSingleSkuProviderRefund(refundId)) {
            java.util.Map<String,Object> view = workflowService.refundView(refundId);
            String status = String.valueOf(view.get("bridgeStatus"));
            if (!("RETRY_WAIT".equals(status) || "MANUAL_REVIEW".equals(status) || "FAILED".equals(status))) {
                throw new BusinessException("当前售后协同状态不允许手动重试");
            }
            if (view.get("providerRefundId") == null) {
                workflowService.approvePreparedRefund(refundId);
            } else {
                workflowService.syncRefund(refundId);
            }
            return Result.success(service.getBridge(refundId));
        }
        return Result.success(service.retryException(refundId));
    }

    @PostMapping("/process-due")
    public Result<TicketSourceRefundBatchResult> processDue(@RequestParam(required = false) Integer limit) {
        int safeLimit = limit == null ? 50 : Math.max(1, Math.min(limit, 200));
        V12BatchResult workflowBatch = workflowService.syncPendingRefunds(safeLimit);
        TicketSourceRefundBatchResult legacy = service.processDue(safeLimit);
        TicketSourceRefundBatchResult merged = new TicketSourceRefundBatchResult();
        merged.setRequestedCount(safeLimit);
        merged.setProcessedCount(workflowBatch.requested() + legacy.getProcessedCount());
        int terminal = 0;
        int pending = 0;
        for (Long id : workflowBatch.succeededIds()) {
            String status = String.valueOf(workflowService.refundView(id).get("bridgeStatus"));
            if ("SUCCESS".equals(status)) terminal++; else pending++;
        }
        merged.setSuccessCount(terminal + legacy.getSuccessCount());
        merged.setPendingCount(pending + legacy.getPendingCount());
        merged.setFailedCount(workflowBatch.failed() + legacy.getFailedCount());
        java.util.List<Long> ids = new java.util.ArrayList<>(workflowBatch.succeededIds());
        ids.addAll(legacy.getRefundIds());
        merged.setRefundIds(ids);
        return Result.success(merged);
    }
}
