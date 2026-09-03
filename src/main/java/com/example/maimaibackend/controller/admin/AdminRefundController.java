package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminRefundRejectRequest;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.service.admin.AdminRefundService;
import com.example.maimaibackend.service.admin.TicketOperationExecutor;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminRefundDetailVO;
import com.example.maimaibackend.vo.admin.AdminRefundListPageVO;
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
@RequestMapping("/api/admin/refunds")
public class AdminRefundController {

    private final AdminRefundService adminRefundService;
    private final TicketOperationExecutor ticketOperationExecutor;

    public AdminRefundController(AdminRefundService adminRefundService,
                                 TicketOperationExecutor ticketOperationExecutor) {
        this.adminRefundService = adminRefundService;
        this.ticketOperationExecutor = ticketOperationExecutor;
    }

    @GetMapping
    public Result<AdminRefundListPageVO> getRefundList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String refundStatus,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(adminRefundService.getRefundList(keyword, refundStatus, userId, orderId,
                dateFrom, dateTo, pageNo, pageSize));
    }

    @GetMapping("/{refundId}")
    public Result<AdminRefundDetailVO> getRefundDetail(@PathVariable Long refundId) {
        return Result.success(adminRefundService.getRefundDetail(refundId));
    }

    @PostMapping("/{refundId}/approve")
    public Result<AdminOperateResponse> approveRefund(@PathVariable Long refundId,
                                                       HttpServletRequest request,
                                                       HttpSession session) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(request, session);
        return Result.success(ticketOperationExecutor.execute(
                context, "REFUND", "APPROVE_REFUND", "REFUND", refundId, null, null,
                () -> adminRefundService.approveRefund(refundId, context)
        ));
    }

    @PostMapping("/{refundId}/reject")
    public Result<AdminOperateResponse> rejectRefund(
            @PathVariable Long refundId,
            @RequestBody AdminRefundRejectRequest rejectRequest,
            HttpServletRequest request,
            HttpSession session
    ) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(request, session);
        return Result.success(ticketOperationExecutor.execute(
                context, "REFUND", "REJECT_REFUND", "REFUND", refundId, null, null,
                () -> adminRefundService.rejectRefund(refundId, rejectRequest, context)
        ));
    }
}
