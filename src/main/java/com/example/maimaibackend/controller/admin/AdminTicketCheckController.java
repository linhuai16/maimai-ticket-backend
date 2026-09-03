package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminTicketCheckRequest;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.service.admin.AdminTicketService;
import com.example.maimaibackend.service.admin.TicketOperationExecutor;
import com.example.maimaibackend.vo.admin.AdminTicketCheckResponse;
import com.example.maimaibackend.vo.admin.AdminTicketListPageVO;
import com.example.maimaibackend.vo.admin.AdminTicketVerifyVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ticket-checks")
public class AdminTicketCheckController {
    private final AdminTicketService adminTicketService;
    private final TicketOperationExecutor ticketOperationExecutor;

    public AdminTicketCheckController(AdminTicketService adminTicketService,
                                      TicketOperationExecutor ticketOperationExecutor) {
        this.adminTicketService = adminTicketService;
        this.ticketOperationExecutor = ticketOperationExecutor;
    }

    @GetMapping("/verify")
    public Result<AdminTicketVerifyVO> verifyTicket(@RequestParam String code) {
        return Result.success(adminTicketService.verifyTicket(code));
    }

    @PostMapping("/check")
    public Result<AdminTicketCheckResponse> checkTicket(@RequestBody AdminTicketCheckRequest requestBody,
                                                        HttpServletRequest request,
                                                        HttpSession session) {
        TicketOperationContext context = AdminTicketContextResolver.resolve(request, session);
        return ticketOperationExecutor.execute(context, "CHECK", "CHECK_TICKET", "TICKET",
                null, null, null,
                () -> Result.success(adminTicketService.checkTicket(requestBody, context)));
    }

    @GetMapping("/records")
    public Result<AdminTicketListPageVO> getCheckRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(adminTicketService.getCheckRecords(keyword, projectId, sessionId, dateFrom, dateTo,
                pageNo, pageSize));
    }
}
