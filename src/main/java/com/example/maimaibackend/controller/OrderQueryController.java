package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.service.OrderQueryService;
import com.example.maimaibackend.vo.order.OrderDetailVO;
import com.example.maimaibackend.vo.order.OrderListPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    public OrderQueryController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping
    public Result<OrderListPageVO> getOrderList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "ALL") String tab,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return Result.success(orderQueryService.getOrderList(userId, tab, pageNo, pageSize));
    }

    @GetMapping("/{orderId}/detail")
    public Result<OrderDetailVO> getOrderDetail(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ) {
        return Result.success(orderQueryService.getOrderDetail(userId, orderId));
    }
}
