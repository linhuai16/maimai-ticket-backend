package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminTicketErrorRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateTicketSeatRequest;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.service.admin.AdminTicketService;
import com.example.maimaibackend.service.admin.TicketOperationExecutor;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminTicketDetailVO;
import com.example.maimaibackend.vo.admin.AdminTicketListPageVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tickets")
public class AdminTicketController {
    private final AdminTicketService adminTicketService;
    private final TicketOperationExecutor ticketOperationExecutor;

    public AdminTicketController(AdminTicketService adminTicketService,
                                 TicketOperationExecutor ticketOperationExecutor) {
        this.adminTicketService = adminTicketService;
        this.ticketOperationExecutor = ticketOperationExecutor;
    }

    @GetMapping
    public Result<AdminTicketListPageVO> getTicketList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) String ticketStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(adminTicketService.getTicketList(keyword, orderId, userId, projectId, sessionId,
                ticketStatus, dateFrom, dateTo, pageNo, pageSize));
    }

    @GetMapping("/{ticketId}")
    public Result<AdminTicketDetailVO> getTicketDetail(@PathVariable Long ticketId) {
        return Result.success(adminTicketService.getTicketDetail(ticketId));
    }

    @GetMapping(value = "/{ticketId}/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable Long ticketId,
                                             HttpServletRequest request,
                                             HttpSession session) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(request, session);
        byte[] qrCode = ticketOperationExecutor.execute(context, "QR", "VIEW_QR", "TICKET",
                ticketId, null, ticketId,
                () -> adminTicketService.getTicketQrCode(ticketId, context));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCode);
    }

    @PutMapping("/{ticketId}/seat")
    public Result<AdminOperateResponse> updateSeatInfo(@PathVariable Long ticketId,
                                                       @RequestBody AdminUpdateTicketSeatRequest request,
                                                       HttpServletRequest servletRequest,
                                                       HttpSession session) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(servletRequest, session);
        return ticketOperationExecutor.execute(context, "TICKET", "UPDATE_SEAT", "TICKET",
                ticketId, null, ticketId,
                () -> Result.success(adminTicketService.updateSeatInfo(ticketId, request, context)));
    }

    @PostMapping("/{ticketId}/mark-error")
    public Result<AdminOperateResponse> markTicketError(@PathVariable Long ticketId,
                                                        @RequestBody(required = false) AdminTicketErrorRequest request,
                                                        HttpServletRequest servletRequest,
                                                        HttpSession session) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(servletRequest, session);
        return ticketOperationExecutor.execute(context, "ISSUE", "MARK_TICKET_ERROR", "TICKET",
                ticketId, null, ticketId,
                () -> Result.success(adminTicketService.markTicketError(ticketId, request, context)));
    }

    @PostMapping("/{ticketId}/retry")
    public Result<AdminOperateResponse> retryTicket(@PathVariable Long ticketId,
                                                    HttpServletRequest servletRequest,
                                                    HttpSession session) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(servletRequest, session);
        return ticketOperationExecutor.execute(context, "ISSUE", "RETRY_TICKET", "TICKET",
                ticketId, null, ticketId,
                () -> Result.success(adminTicketService.retryTicket(ticketId, context)));
    }
}
