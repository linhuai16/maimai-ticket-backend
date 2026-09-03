package com.example.maimaibackend.controller.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.sync.TicketSourceResourceSyncService;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceAutoPublishRequest;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceInventorySyncResult;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceMappingSummary;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceProjectSyncResult;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceResourcePreview;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceSyncRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ticket-source-sync")
public class AdminTicketSourceResourceSyncController {
    private final TicketSourceResourceSyncService syncService;

    public AdminTicketSourceResourceSyncController(TicketSourceResourceSyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/{providerCode}/projects/{providerProjectId}/preview")
    public Result<TicketSourceResourcePreview> preview(
            @PathVariable String providerCode,
            @PathVariable String providerProjectId
    ) {
        return Result.success(syncService.preview(providerCode, providerProjectId));
    }

    @PostMapping("/{providerCode}/projects/{providerProjectId}/sync")
    public Result<TicketSourceProjectSyncResult> syncProject(
            @PathVariable String providerCode,
            @PathVariable String providerProjectId,
            @RequestBody(required = false) TicketSourceSyncRequest request
    ) {
        return Result.success(syncService.syncProject(providerCode, providerProjectId, request));
    }

    @GetMapping("/{providerCode}/projects/{providerProjectId}/mapping")
    public Result<TicketSourceMappingSummary> mapping(
            @PathVariable String providerCode,
            @PathVariable String providerProjectId
    ) {
        return Result.success(syncService.mapping(providerCode, providerProjectId));
    }

    @PutMapping("/{providerCode}/projects/{providerProjectId}/auto-publish")
    public Result<TicketSourceMappingSummary> updateAutoPublish(
            @PathVariable String providerCode,
            @PathVariable String providerProjectId,
            @RequestBody TicketSourceAutoPublishRequest request
    ) {
        return Result.success(syncService.updateAutoPublish(
                providerCode,
                providerProjectId,
                request == null ? null : request.getEnabled()));
    }

    @PostMapping("/{providerCode}/projects/{providerProjectId}/inventory/sync")
    public Result<List<TicketSourceInventorySyncResult>> syncProjectInventory(
            @PathVariable String providerCode,
            @PathVariable String providerProjectId
    ) {
        return Result.success(syncService.syncProjectInventory(providerCode, providerProjectId));
    }

    @PostMapping("/{providerCode}/skus/{providerSkuId}/inventory/sync")
    public Result<TicketSourceInventorySyncResult> syncSkuInventory(
            @PathVariable String providerCode,
            @PathVariable String providerSkuId
    ) {
        return Result.success(syncService.syncSkuInventory(providerCode, providerSkuId));
    }
}
