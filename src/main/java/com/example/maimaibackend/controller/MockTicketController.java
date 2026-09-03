package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.service.MockTicketService;
import com.example.maimaibackend.service.admin.TicketOperationExecutor;
import com.example.maimaibackend.vo.ticket.MockTicketCheckResponse;
import com.example.maimaibackend.vo.ticket.MockTicketIssueFailedResponse;
import com.example.maimaibackend.vo.ticket.MockTicketIssueSuccessResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mock")
public class MockTicketController {

    private final MockTicketService mockTicketService;
    private final TicketOperationExecutor ticketOperationExecutor;

    public MockTicketController(MockTicketService mockTicketService,
                                TicketOperationExecutor ticketOperationExecutor) {
        this.mockTicketService = mockTicketService;
        this.ticketOperationExecutor = ticketOperationExecutor;
    }

    @PostMapping("/orders/{orderId}/tickets/issue-success")
    public Result<MockTicketIssueSuccessResponse> issueSuccess(@PathVariable Long orderId) {
        TicketOperationContext context = TicketOperationContext.system("MockTicketService");
        return ticketOperationExecutor.execute(context, "ISSUE", "SYSTEM_ISSUE_SUCCESS", "ORDER",
                orderId, orderId, null,
                () -> Result.success(mockTicketService.issueSuccess(orderId)));
    }

    @PostMapping("/orders/{orderId}/tickets/issue-failed")
    public Result<MockTicketIssueFailedResponse> issueFailed(
            @PathVariable Long orderId,
            @RequestParam(required = false) String abnormalReason
    ) {
        TicketOperationContext context = TicketOperationContext.system("MockTicketService");
        return ticketOperationExecutor.execute(context, "ISSUE", "SYSTEM_ISSUE_FAILED_REFUND", "ORDER",
                orderId, orderId, null,
                () -> Result.success(mockTicketService.issueFailed(orderId, abnormalReason)));
    }

    @PostMapping("/tickets/{ticketId}/check")
    public Result<MockTicketCheckResponse> checkTicket(@PathVariable Long ticketId) {
        TicketOperationContext context = TicketOperationContext.system("MockTicketService");
        return ticketOperationExecutor.execute(context, "CHECK", "SYSTEM_CHECK_TICKET", "TICKET",
                ticketId, null, ticketId,
                () -> Result.success(mockTicketService.checkTicket(ticketId)));
    }
}
