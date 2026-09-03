package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.refund.RefundOrderRequest;
import com.example.maimaibackend.service.RefundService;
import com.example.maimaibackend.vo.refund.MockRefundSuccessResponse;
import com.example.maimaibackend.vo.refund.RefundApplyPageVO;
import com.example.maimaibackend.vo.refund.RefundOrderResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping("/orders/{orderId}/refund-confirm")
    public Result<RefundApplyPageVO> getRefundConfirm(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ) {
        return Result.success(refundService.getRefundConfirm(userId, orderId));
    }

    @PostMapping("/orders/{orderId}/refund")
    public Result<RefundOrderResponse> applyRefund(
            @PathVariable Long orderId,
            @RequestBody RefundOrderRequest request
    ) {
        return Result.success(refundService.applyRefund(orderId, request));
    }

    @PostMapping("/mock/refunds/{refundId}/success")
    public Result<MockRefundSuccessResponse> mockRefundSuccess(@PathVariable Long refundId) {
        return Result.success(refundService.mockRefundSuccess(refundId));
    }
}
