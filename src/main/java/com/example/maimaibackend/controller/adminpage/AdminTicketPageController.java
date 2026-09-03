package com.example.maimaibackend.controller.adminpage;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminTicketPageController {
    @GetMapping("/tickets")
    public String tickets(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "tickets", "电子票管理");
        return "admin/ticket/ticket-list";
    }

    @GetMapping("/ticket-issues")
    public String issueTasks(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "ticket-issues", "出票任务");
        return "admin/ticket/issue-task-list";
    }

    @GetMapping("/ticket-errors")
    public String issueErrors(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "ticket-errors", "出票异常处理");
        return "admin/ticket/issue-error-list";
    }

    @GetMapping("/ticket-checks")
    public String ticketChecks(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "ticket-checks", "检票管理");
        return "admin/ticket/check-management";
    }

    @GetMapping("/ticket-qr")
    public String qrSearch(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "ticket-qr", "二维码查看");
        return "admin/ticket/qr-search";
    }

    @GetMapping("/tickets/{ticketId}/qr")
    public String qrView(@PathVariable Long ticketId, Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "ticket-qr", "二维码查看");
        model.addAttribute("ticketId", ticketId);
        return "admin/ticket/qr-view";
    }

    @GetMapping("/ticket-logs/issues")
    public String issueLogs(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "ticket-issue-logs", "出票日志");
        model.addAttribute("logView", "ISSUE");
        return "admin/ticket/log-list";
    }

    @GetMapping("/ticket-logs/operations")
    public String operationLogs(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "ticket-operation-logs", "操作日志");
        model.addAttribute("logView", "OPERATION");
        return "admin/ticket/log-list";
    }
}
