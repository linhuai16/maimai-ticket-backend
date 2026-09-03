package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.order.TicketSourceOrderBridgeService;
import com.example.maimaibackend.ticketsource.order.model.TicketSourceExpireResult;
import com.example.maimaibackend.ticketsource.order.model.TicketSourceOrderBridge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ticket-source-orders")
public class AdminTicketSourceOrderController {
    private final TicketSourceOrderBridgeService bridgeService;

    public AdminTicketSourceOrderController(TicketSourceOrderBridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    @GetMapping("/{orderId}")
    public Result<TicketSourceOrderBridge> detail(@PathVariable Long orderId) {
        return Result.success(bridgeService.getBridge(orderId));
    }

    @PostMapping("/{orderId}/expire")
    public Result<TicketSourceOrderBridge> expire(@PathVariable Long orderId) {
        return Result.success(bridgeService.expireOrder(orderId));
    }

    @PostMapping("/expire-due")
    public Result<TicketSourceExpireResult> expireDue(
            @RequestParam(required = false) Integer limit
    ) {
        return Result.success(bridgeService.expireDue(limit));
    }
}
