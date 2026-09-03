package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.issue.TicketSourceIssueService;
import com.example.maimaibackend.ticketsource.issue.model.TicketSourceIssueBatchResult;
import com.example.maimaibackend.ticketsource.issue.model.TicketSourceIssueTask;
import com.example.maimaibackend.ticketsource.workflow.TicketSourceWorkflowService;
import com.example.maimaibackend.ticketsource.workflow.model.V12BatchResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ticket-source-issues")
public class AdminTicketSourceIssueController {
    private final TicketSourceIssueService service;
    private final TicketSourceWorkflowService workflowService;

    public AdminTicketSourceIssueController(TicketSourceIssueService service, TicketSourceWorkflowService workflowService) {
        this.service = service;
        this.workflowService = workflowService;
    }

    @GetMapping("/{orderId}")
    public Result<TicketSourceIssueTask> detail(@PathVariable Long orderId) {
        return Result.success(service.getTask(orderId));
    }

    @PostMapping("/{orderId}/process")
    public Result<TicketSourceIssueTask> process(@PathVariable Long orderId) {
        if (workflowService.isSingleSkuProviderOrder(orderId)) {
            workflowService.processFulfillment(orderId);
            return Result.success(service.getTask(orderId));
        }
        return Result.success(service.processOrder(orderId));
    }

    @PostMapping("/{orderId}/sync-status")
    public Result<TicketSourceIssueTask> syncStatus(@PathVariable Long orderId) {
        if (workflowService.isSingleSkuProviderOrder(orderId)) {
            workflowService.syncFulfillmentStatus(orderId);
            return Result.success(service.getTask(orderId));
        }
        return Result.success(service.syncProviderStatus(orderId));
    }

    @PostMapping("/{orderId}/retry")
    public Result<TicketSourceIssueTask> retry(@PathVariable Long orderId) {
        if (workflowService.isSingleSkuProviderOrder(orderId)) {
            workflowService.retryFulfillment(orderId);
            return Result.success(service.getTask(orderId));
        }
        return Result.success(service.retryOrder(orderId));
    }

    @PostMapping("/process-due")
    public Result<TicketSourceIssueBatchResult> processDue(@RequestParam(required = false) Integer limit) {
        int safeLimit = limit == null ? 50 : Math.max(1, Math.min(limit, 200));
        // 旧 Service 的 due SQL 已排除 SINGLE_SKU；V1.2 和历史任务分别处理，避免双写。
        V12BatchResult workflowBatch = workflowService.processDueFulfillment(safeLimit);
        TicketSourceIssueBatchResult legacy = service.processDue(safeLimit);
        TicketSourceIssueBatchResult merged = new TicketSourceIssueBatchResult();
        merged.setRequestedCount(safeLimit);
        merged.setProcessedCount(workflowBatch.requested() + legacy.getProcessedCount());
        int v12TerminalSuccess = 0;
        int v12Pending = 0;
        for (Long id : workflowBatch.succeededIds()) {
            String status = String.valueOf(workflowService.getFulfillment(id).get("taskStatus"));
            if ("SUCCESS".equals(status)) v12TerminalSuccess++; else v12Pending++;
        }
        merged.setSuccessCount(v12TerminalSuccess + legacy.getSuccessCount());
        merged.setPendingCount(v12Pending + legacy.getPendingCount());
        merged.setFailedCount(workflowBatch.failed() + legacy.getFailedCount());
        java.util.List<Long> ids = new java.util.ArrayList<>(workflowBatch.succeededIds());
        ids.addAll(legacy.getOrderIds());
        merged.setOrderIds(ids);
        return Result.success(merged);
    }
}
