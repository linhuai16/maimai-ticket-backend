package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.mapper.ticketsource.V11OrderMapper;
import com.example.maimaibackend.ticketsource.order.provider.V11OrderService;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderCreateResult;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderResourceEntry;
import com.example.maimaibackend.ticketsource.order.provider.model.V11UnknownCreateRecoveryBatchResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ticket-source-v11-orders")
public class AdminTicketSourceV11OrderController {
    private final V11OrderMapper mapper;
    private final V11OrderService orderService;

    public AdminTicketSourceV11OrderController(V11OrderMapper mapper, V11OrderService orderService) {
        this.mapper = mapper;
        this.orderService = orderService;
    }

    @GetMapping("/resources/{providerCode}/{providerProjectId}")
    public Result<List<V11OrderResourceEntry>> resources(@PathVariable String providerCode,
                                                          @PathVariable String providerProjectId) {
        return Result.success(mapper.selectResourceEntries(providerCode.trim().toUpperCase(), providerProjectId));
    }
    @PostMapping("/{orderId}/recover-unknown")
    public Result<V11OrderCreateResult> recoverUnknown(@PathVariable Long orderId) {
        return Result.success(orderService.recoverUnknownCreate(orderId));
    }

    @GetMapping("/create-recovery/by-submit-no/{clientSubmitNo}")
    public Result<Map<String, Object>> createRecoveryBySubmitNo(@PathVariable String clientSubmitNo) {
        Map<String, Object> row = mapper.selectCreateRecoveryBySubmitNo(clientSubmitNo);
        if (row == null || row.isEmpty()) {
            throw new com.example.maimaibackend.common.BusinessException("提交流水不存在");
        }
        return Result.success(row);
    }

    @PostMapping("/create-recovery/by-submit-no/{clientSubmitNo}")
    public Result<V11OrderCreateResult> recoverUnknownBySubmitNo(@PathVariable String clientSubmitNo) {
        Map<String, Object> row = mapper.selectCreateRecoveryBySubmitNo(clientSubmitNo);
        if (row == null || row.get("orderId") == null) {
            throw new com.example.maimaibackend.common.BusinessException("提交流水尚未形成可补查订单");
        }
        Object orderId = row.get("orderId");
        Long id = orderId instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(orderId));
        return Result.success(orderService.recoverUnknownCreate(id));
    }

    @PostMapping("/unknown:recover")
    public Result<V11UnknownCreateRecoveryBatchResult> recoverUnknownBatch(@RequestParam(required = false) Integer limit) {
        return Result.success(orderService.recoverUnknownCreates(limit == null ? 50 : limit));
    }

}
