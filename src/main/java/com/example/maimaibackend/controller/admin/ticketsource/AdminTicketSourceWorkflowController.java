package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.workflow.TicketSourceWorkflowService;
import com.example.maimaibackend.ticketsource.workflow.model.V12BatchResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ticket-source-v12")
public class AdminTicketSourceWorkflowController {
    private final TicketSourceWorkflowService service;
    public AdminTicketSourceWorkflowController(TicketSourceWorkflowService service) { this.service = service; }

    @PostMapping("/fulfillment/orders/{orderId}:process")
    public Result<Map<String,Object>> processFulfillment(@PathVariable Long orderId) {
        return Result.success(service.processFulfillment(orderId));
    }

    @PostMapping("/fulfillment/pending:process")
    public Result<V12BatchResult> processPendingFulfillment(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(service.processDueFulfillment(limit));
    }

    @PostMapping("/refunds/{refundId}:sync")
    public Result<Map<String,Object>> syncRefund(@PathVariable Long refundId) {
        return Result.success(service.syncRefund(refundId));
    }

    @PostMapping("/refunds/pending:sync")
    public Result<V12BatchResult> syncPendingRefunds(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(service.syncPendingRefunds(limit));
    }

    @PostMapping("/callbacks/{eventId}:process")
    public Result<Map<String,Object>> processCallback(@PathVariable Long eventId) {
        return Result.success(service.processCallback(eventId));
    }

    @PostMapping("/callbacks/pending:process")
    public Result<V12BatchResult> processCallbacks(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(service.processPendingCallbacks(limit));
    }

    @PostMapping("/reconciliation:run")
    public Result<Map<String,Object>> reconcile(@RequestParam String providerCode,
                                                @RequestBody List<Long> orderIds) {
        return Result.success(service.reconcile(providerCode, orderIds));
    }
}
