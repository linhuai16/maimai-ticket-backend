package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.order.CancelOrderRequest;
import com.example.maimaibackend.dto.order.CreateOrderRequest;
import com.example.maimaibackend.service.OrderService;
import com.example.maimaibackend.vo.order.CancelOrderResponse;
import com.example.maimaibackend.vo.order.CreateOrderResponse;
import com.example.maimaibackend.vo.order.SubmitOrderPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/confirm")
    public Result<SubmitOrderPageVO> getSubmitOrderPage(
            @RequestParam Long projectId,
            @RequestParam Long sessionId,
            @RequestParam Long skuId,
            @RequestParam Integer quantity
    ) {
        return Result.success(orderService.getSubmitOrderPage(projectId, sessionId, skuId, quantity));
    }

    @PostMapping
    public Result<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return Result.success(orderService.createOrder(request));
    }

    @PostMapping("/{orderId}/cancel")
    public Result<CancelOrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody CancelOrderRequest request
    ) {
        return Result.success(orderService.cancelOrder(orderId, request));
    }

}
