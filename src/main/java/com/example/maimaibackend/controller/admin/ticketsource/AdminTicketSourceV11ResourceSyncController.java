package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.resource.provider.V11ResourceSyncService;
import com.example.maimaibackend.ticketsource.resource.provider.model.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ticket-source-v11-sync")
public class AdminTicketSourceV11ResourceSyncController {
    private final V11ResourceSyncService service;

    public AdminTicketSourceV11ResourceSyncController(V11ResourceSyncService service) {
        this.service = service;
    }

    @GetMapping("/{providerCode}/projects/{providerProjectId}/preview")
    public Result<V11ResourcePreview> preview(@PathVariable String providerCode,
                                               @PathVariable String providerProjectId) {
        return Result.success(service.preview(providerCode, providerProjectId));
    }

    @PostMapping("/{providerCode}/projects/{providerProjectId}/sync")
    public Result<V11ResourceSyncResult> sync(@PathVariable String providerCode,
                                               @PathVariable String providerProjectId,
                                               @RequestBody(required = false) V11ResourceSyncRequest request) {
        return Result.success(service.syncProject(providerCode, providerProjectId, request));
    }

    @GetMapping("/{providerCode}/projects/{providerProjectId}/mapping")
    public Result<V11ResourceMappingSummary> mapping(@PathVariable String providerCode,
                                                       @PathVariable String providerProjectId) {
        return Result.success(service.mapping(providerCode, providerProjectId));
    }

    @PostMapping("/{providerCode}/campaign-assets/sync")
    public Result<V11CampaignSyncResult> syncCampaignAssets(@PathVariable String providerCode,
                                                             @RequestParam(required = false) String cityCode) {
        return Result.success(service.syncCampaignAssets(providerCode, cityCode));
    }
}
