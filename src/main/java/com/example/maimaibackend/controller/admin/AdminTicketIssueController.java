package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminTicketErrorRequest;
import com.example.maimaibackend.dto.admin.AdminTicketSystemRefundRequest;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.service.admin.AdminTicketService;
import com.example.maimaibackend.service.admin.TicketOperationExecutor;
import com.example.maimaibackend.vo.admin.AdminIssueOrderDetailVO;
import com.example.maimaibackend.vo.admin.AdminIssueOrderListPageVO;
import com.example.maimaibackend.vo.admin.AdminTicketIssueResponse;
import com.example.maimaibackend.vo.admin.AdminTicketSystemRefundResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ticket-issues")
public class AdminTicketIssueController {
    private final AdminTicketService adminTicketService;
    private final TicketOperationExecutor ticketOperationExecutor;

    public AdminTicketIssueController(AdminTicketService adminTicketService,
                                      TicketOperationExecutor ticketOperationExecutor) {
        this.adminTicketService = adminTicketService;
        this.ticketOperationExecutor = ticketOperationExecutor;
    }

    @GetMapping("/orders")
    public Result<AdminIssueOrderListPageVO> getIssueOrderList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) String issueStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(adminTicketService.getIssueOrderList(keyword, projectId, sessionId, issueStatus,
                dateFrom, dateTo, pageNo, pageSize));
    }

    @GetMapping("/orders/{orderId}")
    public Result<AdminIssueOrderDetailVO> getIssueOrderDetail(@PathVariable Long orderId) {
        return Result.success(adminTicketService.getIssueOrderDetail(orderId));
    }

    @PostMapping("/orders/{orderId}/issue-success")
    public Result<AdminTicketIssueResponse> issueSuccess(@PathVariable Long orderId,
                                                         HttpServletRequest request,
                                                         HttpSession session) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(request, session);
        return ticketOperationExecutor.execute(context, "ISSUE", "ISSUE_ORDER_SUCCESS", "ORDER",
                orderId, orderId, null,
                () -> Result.success(adminTicketService.issueOrderSuccess(orderId, context)));
    }

    @PostMapping("/orders/{orderId}/mark-error")
    public Result<AdminTicketIssueResponse> markIssueError(@PathVariable Long orderId,
                                                           @RequestBody(required = false) AdminTicketErrorRequest requestBody,
                                                           HttpServletRequest request,
                                                           HttpSession session) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(request, session);
        return ticketOperationExecutor.execute(context, "ISSUE", "MARK_ORDER_ERROR", "ORDER",
                orderId, orderId, null,
                () -> Result.success(adminTicketService.markOrderIssueError(orderId, requestBody, context)));
    }

    @PostMapping("/orders/{orderId}/retry")
    public Result<AdminTicketIssueResponse> retryIssue(@PathVariable Long orderId,
                                                       HttpServletRequest request,
                                                       HttpSession session) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(request, session);
        return ticketOperationExecutor.execute(context, "ISSUE", "RETRY_ORDER", "ORDER",
                orderId, orderId, null,
                () -> Result.success(adminTicketService.retryOrderIssue(orderId, context)));
    }

    @PostMapping("/orders/{orderId}/system-refund")
    public Result<AdminTicketSystemRefundResponse> systemRefund(@PathVariable Long orderId,
                                                                 @RequestBody(required = false) AdminTicketSystemRefundRequest requestBody,
                                                                 HttpServletRequest request,
                                                                 HttpSession session) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(request, session);
        return ticketOperationExecutor.execute(context, "ISSUE", "ISSUE_FAILED_SYSTEM_REFUND", "ORDER",
                orderId, orderId, null,
                () -> Result.success(adminTicketService.systemRefundForIssueFailure(orderId, requestBody, context)));
    }
}
