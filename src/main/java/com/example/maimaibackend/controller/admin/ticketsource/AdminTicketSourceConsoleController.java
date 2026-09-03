package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminTicketSourceCampaignPublishBannerRequest;
import com.example.maimaibackend.dto.admin.AdminTicketSourceCampaignReviewRequest;
import com.example.maimaibackend.dto.admin.AdminTicketSourceProviderUpdateRequest;
import com.example.maimaibackend.dto.admin.AdminTicketSourceSettlementAdjustmentRequest;
import com.example.maimaibackend.dto.admin.AdminTicketSourceSettlementCreateRequest;
import com.example.maimaibackend.service.admin.AdminTicketSourceBusinessService;
import com.example.maimaibackend.service.admin.AdminTicketSourceConsoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ticket-source-console")
public class AdminTicketSourceConsoleController {
    private final AdminTicketSourceConsoleService service;
    private final AdminTicketSourceBusinessService businessService;

    public AdminTicketSourceConsoleController(AdminTicketSourceConsoleService service,
                                              AdminTicketSourceBusinessService businessService) {
        this.service = service;
        this.businessService = businessService;
    }

    @GetMapping("/summary") public Result<Map<String,Object>> summary(){ return Result.success(service.summary()); }
    @GetMapping("/providers") public Result<List<Map<String,Object>>> providers(){ return Result.success(service.providers()); }
    @PutMapping("/providers/{providerId}") public Result<Map<String,Object>> updateProvider(@PathVariable Long providerId, @RequestBody AdminTicketSourceProviderUpdateRequest request){ return Result.success(service.updateProvider(providerId, request)); }
    @GetMapping("/mappings") public Result<List<Map<String,Object>>> mappings(@RequestParam(required=false) String providerCode,@RequestParam(required=false) String keyword,@RequestParam(required=false) Integer limit){return Result.success(service.mappings(providerCode,keyword,limit));}
    @GetMapping("/mappings/by-projects") public Result<List<Map<String,Object>>> mappingsByProjects(@RequestParam String providerCode,@RequestParam List<String> providerProjectId){return Result.success(service.mappingsByProjects(providerCode,providerProjectId));}
    @GetMapping("/orders") public Result<List<Map<String,Object>>> orders(@RequestParam(required=false) String providerCode,@RequestParam(required=false) String bridgeStatus,@RequestParam(required=false) String keyword,@RequestParam(required=false) Integer limit){return Result.success(service.orders(providerCode,bridgeStatus,keyword,limit));}
    @GetMapping("/orders-page") public Result<Map<String,Object>> orderPage(@RequestParam(required=false) String providerCode,@RequestParam(required=false) String bridgeStatus,@RequestParam(required=false) String keyword,@RequestParam(required=false) Integer pageNo,@RequestParam(required=false) Integer pageSize){return Result.success(service.orderPage(providerCode,bridgeStatus,keyword,pageNo,pageSize));}
    @GetMapping("/issues") public Result<List<Map<String,Object>>> issues(@RequestParam(required=false) String status,@RequestParam(required=false) Integer limit){return Result.success(service.issues(status,limit));}
    @GetMapping("/shipments") public Result<List<Map<String,Object>>> shipments(@RequestParam(required=false) String status,@RequestParam(required=false) Integer limit){return Result.success(service.shipments(status,limit));}
    @GetMapping("/refunds") public Result<List<Map<String,Object>>> refunds(@RequestParam(required=false) String status,@RequestParam(required=false) Integer limit){return Result.success(service.refunds(status,limit));}
    @GetMapping("/callbacks") public Result<List<Map<String,Object>>> callbacks(@RequestParam(required=false) String status,@RequestParam(required=false) Integer limit){return Result.success(service.callbacks(status,limit));}
    @GetMapping("/reconciliations") public Result<List<Map<String,Object>>> reconciliations(@RequestParam(required=false) String providerCode,@RequestParam(required=false) Integer limit){return Result.success(service.reconciliations(providerCode,limit));}
    @GetMapping("/campaign-assets") public Result<List<Map<String,Object>>> campaignAssets(@RequestParam(required=false) String providerCode,@RequestParam(required=false) String reviewStatus,@RequestParam(required=false) Integer limit){return Result.success(businessService.campaignAssets(providerCode,reviewStatus,limit));}
    @GetMapping("/promotions") public Result<List<Map<String,Object>>> promotions(@RequestParam(required=false) String providerCode,@RequestParam(required=false) String status,@RequestParam(required=false) Integer limit){return Result.success(businessService.promotions(providerCode,status,limit));}
    @PostMapping("/campaign-assets/{assetId}/review") public Result<Map<String,Object>> reviewCampaign(@PathVariable Long assetId,@RequestBody AdminTicketSourceCampaignReviewRequest request){return Result.success(businessService.reviewCampaignAsset(assetId,request));}
    @PostMapping("/campaign-assets/{assetId}/publish-banner") public Result<Map<String,Object>> publishCampaignBanner(@PathVariable Long assetId,@RequestBody AdminTicketSourceCampaignPublishBannerRequest request){return Result.success(businessService.publishCampaignBanner(assetId,request));}

    @GetMapping("/settlements") public Result<List<Map<String,Object>>> settlements(@RequestParam(required=false) String providerCode,@RequestParam(required=false) String status,@RequestParam(required=false) Integer limit){return Result.success(businessService.settlements(providerCode,status,limit));}
    @GetMapping("/settlements/{periodId}") public Result<Map<String,Object>> settlementDetail(@PathVariable Long periodId){return Result.success(businessService.settlementDetail(periodId));}
    @PostMapping("/settlements") public Result<Map<String,Object>> createSettlement(@RequestBody AdminTicketSourceSettlementCreateRequest request){return Result.success(businessService.createSettlement(request));}
    @PostMapping("/settlements/{periodId}/regenerate") public Result<Map<String,Object>> regenerateSettlement(@PathVariable Long periodId){return Result.success(businessService.regenerateSettlement(periodId));}
    @PostMapping("/settlements/{periodId}/adjustments") public Result<Map<String,Object>> addSettlementAdjustment(@PathVariable Long periodId,@RequestBody AdminTicketSourceSettlementAdjustmentRequest request){return Result.success(businessService.addSettlementAdjustment(periodId,request));}
    @PostMapping("/settlements/{periodId}/confirm") public Result<Map<String,Object>> confirmSettlement(@PathVariable Long periodId){return Result.success(businessService.confirmSettlement(periodId));}
    @PostMapping("/settlements/{periodId}/close") public Result<Map<String,Object>> closeSettlement(@PathVariable Long periodId,@RequestParam String mode){return Result.success(businessService.closeSettlement(periodId,mode));}

}
