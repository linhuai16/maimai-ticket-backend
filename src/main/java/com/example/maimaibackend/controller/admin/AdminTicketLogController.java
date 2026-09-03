package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.service.admin.AdminTicketLogService;
import com.example.maimaibackend.vo.admin.AdminTicketLogItemVO;
import com.example.maimaibackend.vo.admin.AdminTicketLogPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ticket-logs")
public class AdminTicketLogController {
    private final AdminTicketLogService adminTicketLogService;

    public AdminTicketLogController(AdminTicketLogService adminTicketLogService) {
        this.adminTicketLogService = adminTicketLogService;
    }

    @GetMapping
    public Result<AdminTicketLogPageVO> getLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String operatorType,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String resultStatus,
            @RequestParam(required = false) Long ticketId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(adminTicketLogService.getLogs(keyword, businessType, operatorType, actionType,
                resultStatus, ticketId, orderId, operatorId, dateFrom, dateTo, pageNo, pageSize));
    }

    @GetMapping("/{logId}")
    public Result<AdminTicketLogItemVO> getLogDetail(@PathVariable Long logId) {
        return Result.success(adminTicketLogService.getLogDetail(logId));
    }
}
