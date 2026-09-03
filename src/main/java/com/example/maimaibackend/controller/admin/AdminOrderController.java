package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.service.admin.AdminOrderService;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminOrderDetailVO;
import com.example.maimaibackend.vo.admin.AdminOrderListPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public Result<AdminOrderListPageVO> getOrderList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(adminOrderService.getOrderList(keyword, userId, projectId, orderStatus,
                dateFrom, dateTo, pageNo, pageSize));
    }

    @GetMapping("/{orderId}")
    public Result<AdminOrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        return Result.success(adminOrderService.getOrderDetail(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public Result<AdminOperateResponse> cancelOrder(@PathVariable Long orderId) {
        return Result.success(adminOrderService.cancelOrder(orderId));
    }
}
