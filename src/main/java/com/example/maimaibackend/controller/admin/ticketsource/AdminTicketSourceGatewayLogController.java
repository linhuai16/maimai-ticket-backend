package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.log.TicketSourceGatewayLogService;
import com.example.maimaibackend.ticketsource.log.model.TicketSourceGatewayLog;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ticket-source-gateway-logs")
public class AdminTicketSourceGatewayLogController {
    private final TicketSourceGatewayLogService service;

    public AdminTicketSourceGatewayLogController(TicketSourceGatewayLogService service) { this.service = service; }

    @GetMapping
    public Result<List<TicketSourceGatewayLog>> list(
            @RequestParam(required = false) String providerCode,
            @RequestParam(required = false) String operationCode,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) Integer limit) {
        return Result.success(service.list(providerCode, operationCode, success, limit));
    }
}
