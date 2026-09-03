package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.dto.payment.MockPaySuccessRequest;
import com.example.maimaibackend.service.PaymentService;
import com.example.maimaibackend.service.admin.TicketOperationExecutor;
import com.example.maimaibackend.vo.payment.MockPaySuccessResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mock/orders")
public class PaymentController {

    private final PaymentService paymentService;
    private final TicketOperationExecutor ticketOperationExecutor;

    public PaymentController(PaymentService paymentService,
                             TicketOperationExecutor ticketOperationExecutor) {
        this.paymentService = paymentService;
        this.ticketOperationExecutor = ticketOperationExecutor;
    }

    @PostMapping("/{orderId}/pay-success")
    public Result<MockPaySuccessResponse> mockPaySuccess(
            @PathVariable Long orderId,
            @RequestBody MockPaySuccessRequest request
    ) {
        TicketOperationContext context = TicketOperationContext.system("PaymentService");
        return ticketOperationExecutor.execute(context, "ISSUE", "CREATE_ISSUE_TASK", "ORDER",
                orderId, orderId, null,
                () -> Result.success(paymentService.mockPaySuccess(orderId, request)));
    }
}
