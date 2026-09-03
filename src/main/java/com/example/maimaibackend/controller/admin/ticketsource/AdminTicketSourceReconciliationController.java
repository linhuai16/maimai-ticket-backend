package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.reconcile.TicketSourceReconciliationService;
import com.example.maimaibackend.ticketsource.reconcile.dto.TicketSourceReconciliationRequest;
import com.example.maimaibackend.ticketsource.reconcile.model.TicketSourceReconciliationBatch;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ticket-source-reconciliation")
public class AdminTicketSourceReconciliationController {
    private final TicketSourceReconciliationService service;

    public AdminTicketSourceReconciliationController(TicketSourceReconciliationService service) {
        this.service = service;
    }

    @PostMapping("/run")
    public Result<TicketSourceReconciliationBatch> run(@RequestBody TicketSourceReconciliationRequest request) {
        return Result.success(service.run(request));
    }

    @GetMapping("/batches/{batchId}")
    public Result<TicketSourceReconciliationBatch> detail(@PathVariable Long batchId) {
        return Result.success(service.getBatch(batchId));
    }
}
