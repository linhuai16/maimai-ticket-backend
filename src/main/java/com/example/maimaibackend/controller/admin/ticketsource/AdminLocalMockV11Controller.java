package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.provider.model.ProviderCallbackEvent;
import com.example.maimaibackend.ticketsource.provider.model.ProviderRefund;
import com.example.maimaibackend.ticketsource.provider.model.ProviderShipment;
import com.example.maimaibackend.ticketsource.provider.model.ProviderTicketDelivery;
import com.example.maimaibackend.ticketsource.provider.mock.LocalMockV11TicketSourceService;
import com.example.maimaibackend.ticketsource.provider.mock.dto.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** V1.1 MOCK_DAMAI 测试控制接口。 */
@RestController
@RequestMapping("/api/admin/ticket-source-mock/v11")
public class AdminLocalMockV11Controller {
    private final LocalMockV11TicketSourceService service;

    public AdminLocalMockV11Controller(LocalMockV11TicketSourceService service) {
        this.service = service;
    }

    @GetMapping("/behaviors")
    public Result<List<Map<String, Object>>> behaviors() {
        return Result.success(service.listBehaviors());
    }

    @PutMapping("/behaviors/{operationCode}")
    public Result<Map<String, Object>> updateBehavior(
            @PathVariable String operationCode,
            @RequestBody MockV11BehaviorRequest request
    ) {
        return Result.success(service.updateBehavior(operationCode, request));
    }

    @PostMapping("/behaviors/reset")
    public Result<List<Map<String, Object>>> resetBehaviors() {
        return Result.success(service.resetBehaviors());
    }

    @PutMapping("/orders/{providerOrderId}/issue-plan")
    public Result<ProviderTicketDelivery> issuePlan(
            @PathVariable String providerOrderId,
            @RequestBody MockV11IssuePlanRequest request
    ) {
        return Result.success(service.configureIssuePlan(providerOrderId, request));
    }

    @PostMapping("/orders/{providerOrderId}/issue-available-now")
    public Result<ProviderTicketDelivery> issueAvailableNow(@PathVariable String providerOrderId) {
        return Result.success(service.makeIssueAvailableNow(providerOrderId));
    }

    @PutMapping("/orders/{providerOrderId}/refund-plan")
    public Result<Map<String, Object>> refundPlan(
            @PathVariable String providerOrderId,
            @RequestBody MockV11RefundPlanRequest request
    ) {
        return Result.success(service.configureRefundPlan(providerOrderId, request));
    }

    @PostMapping("/refunds/{providerRefundId}/available-now")
    public Result<ProviderRefund> refundAvailableNow(@PathVariable String providerRefundId) {
        return Result.success(service.makeRefundAvailableNow(providerRefundId));
    }

    @PutMapping("/orders/{providerOrderId}/shipment")
    public Result<ProviderShipment> updateShipment(
            @PathVariable String providerOrderId,
            @RequestBody MockV11ShipmentUpdateRequest request
    ) {
        return Result.success(service.updateShipment(providerOrderId, request));
    }

    @PostMapping("/callback-events")
    public Result<ProviderCallbackEvent> emitCallback(@RequestBody MockV11CallbackRequest request) {
        return Result.success(service.emitCallback(request));
    }

    @GetMapping("/callback-events")
    public Result<List<ProviderCallbackEvent>> callbackEvents(
            @RequestParam(defaultValue = "50") int limit
    ) {
        return Result.success(service.listCallbackEvents(limit));
    }

    @PutMapping("/ticket-products/{providerSkuId}/inventory")
    public Result<Map<String, Object>> updateSkuInventory(
            @PathVariable String providerSkuId,
            @RequestBody MockV11SkuInventoryRequest request
    ) {
        return Result.success(service.updateSkuInventory(providerSkuId, request));
    }

    @PutMapping("/ticket-products/{providerSkuId}/price")
    public Result<Map<String, Object>> updateSkuPrice(
            @PathVariable String providerSkuId,
            @RequestBody MockV11SkuPriceRequest request
    ) {
        return Result.success(service.updateSkuPrice(providerSkuId, request));
    }
}
