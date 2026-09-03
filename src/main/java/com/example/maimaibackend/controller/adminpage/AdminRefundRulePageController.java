package com.example.maimaibackend.controller.adminpage;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/operation/refund-rules")
public class AdminRefundRulePageController {

    @GetMapping
    public String list(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-refund-rules", "退款规则");
        return "admin/operation/refund-rule/list";
    }

    @GetMapping("/{projectId}")
    public String detail(@PathVariable Long projectId, Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-refund-rules", "退款规则详情");
        model.addAttribute("projectId", projectId);
        return "admin/operation/refund-rule/detail";
    }

    @GetMapping("/{projectId}/edit")
    public String legacyEdit(@PathVariable Long projectId) {
        return "redirect:/admin/operation/refund-rules/" + projectId;
    }

    @GetMapping("/new")
    public String legacyCreate() {
        return "redirect:/admin/operation/refund-rules";
    }
}
