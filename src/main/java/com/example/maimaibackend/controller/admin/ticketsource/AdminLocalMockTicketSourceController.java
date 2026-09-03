package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.mock.LocalMockTicketSourceService;
import com.example.maimaibackend.ticketsource.mock.dto.MockBehaviorUpdateRequest;
import com.example.maimaibackend.ticketsource.mock.dto.MockInventoryUpdateRequest;
import com.example.maimaibackend.ticketsource.mock.dto.MockIssuePlanUpdateRequest;
import com.example.maimaibackend.ticketsource.mock.dto.MockRefundPlanUpdateRequest;
import com.example.maimaibackend.ticketsource.mock.dto.MockSaleStatusUpdateRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefund;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceBehavior;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceDelivery;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceRefundPlan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ticket-source-mock")
public class AdminLocalMockTicketSourceController {
    private final LocalMockTicketSourceService mockService;

    public AdminLocalMockTicketSourceController(LocalMockTicketSourceService mockService) {
        this.mockService = mockService;
    }

    @GetMapping("/behaviors")
    public Result<List<MockTicketSourceBehavior>> behaviors() {
        return Result.success(mockService.listBehaviors());
    }

    @PutMapping("/behaviors/{operationCode}")
    public Result<MockTicketSourceBehavior> updateBehavior(
            @PathVariable String operationCode,
            @RequestBody MockBehaviorUpdateRequest request
    ) {
        return Result.success(mockService.updateBehavior(operationCode, request));
    }

    @PostMapping("/behaviors/reset")
    public Result<List<MockTicketSourceBehavior>> resetBehaviors() {
        return Result.success(mockService.resetBehaviors());
    }
    @PutMapping("/projects/{providerProjectId}/sale-status")
    public Result<TicketSourceProject> updateProjectSaleStatus(
            @PathVariable String providerProjectId,
            @RequestBody MockSaleStatusUpdateRequest request
    ) {
        return Result.success(mockService.updateProjectSaleStatus(providerProjectId, request));
    }

    @PutMapping("/sessions/{providerSessionId}/sale-status")
    public Result<TicketSourceSession> updateSessionSaleStatus(
            @PathVariable String providerSessionId,
            @RequestBody MockSaleStatusUpdateRequest request
    ) {
        return Result.success(mockService.updateSessionSaleStatus(providerSessionId, request));
    }

    @PutMapping("/skus/{providerSkuId}/inventory")
    public Result<TicketSourceInventory> updateInventory(
            @PathVariable String providerSkuId,
            @RequestBody MockInventoryUpdateRequest request
    ) {
        return Result.success(mockService.updateInventory(providerSkuId, request));
    }

    @PutMapping("/orders/{providerOrderId}/issue-plan")
    public Result<MockTicketSourceDelivery> updateIssuePlan(
            @PathVariable String providerOrderId,
            @RequestBody MockIssuePlanUpdateRequest request
    ) {
        return Result.success(mockService.configureIssuePlan(providerOrderId, request));
    }


    @PutMapping("/orders/{providerOrderId}/refund-plan")
    public Result<MockTicketSourceRefundPlan> updateRefundPlan(
            @PathVariable String providerOrderId,
            @RequestBody MockRefundPlanUpdateRequest request
    ) {
        return Result.success(mockService.configureRefundPlan(providerOrderId, request));
    }


    @PostMapping("/refunds/{providerRefundId}/available-now")
    public Result<TicketSourceRefund> makeRefundAvailableNow(@PathVariable String providerRefundId) {
        return Result.success(mockService.makeRefundAvailableNow(providerRefundId));
    }

}
