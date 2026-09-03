package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.V11ShipmentService;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.model.V11ShipmentBatchResult;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.model.V11ShipmentView;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ticket-source-v11-shipments")
public class AdminTicketSourceV11ShipmentController {
    private final V11ShipmentService service;

    public AdminTicketSourceV11ShipmentController(V11ShipmentService service) {
        this.service = service;
    }

    @PostMapping("/orders/{orderId}/sync")
    public Result<V11ShipmentView> syncOrder(@PathVariable Long orderId) {
        return Result.success(service.syncAdmin(orderId));
    }

    @PostMapping("/sync-pending")
    public Result<V11ShipmentBatchResult> syncPending(@RequestParam(defaultValue = "50") int limit,
                                                        @RequestParam(defaultValue = "5") int staleMinutes,
                                                        @RequestParam(defaultValue = "1440") int deliveredStaleMinutes) {
        return Result.success(service.syncPending(limit, staleMinutes, deliveredStaleMinutes));
    }
}
