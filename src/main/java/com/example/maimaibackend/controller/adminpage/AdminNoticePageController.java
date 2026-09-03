package com.example.maimaibackend.controller.adminpage;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/operation/notices")
public class AdminNoticePageController {

    @GetMapping
    public String list(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-notices", "观演须知");
        return "admin/operation/notice/list";
    }

    @GetMapping("/new")
    public String create(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-notices", "新增观演须知");
        model.addAttribute("formMode", "create");
        model.addAttribute("noticeId", null);
        return "admin/operation/notice/form";
    }

    @GetMapping("/{noticeId}/edit")
    public String edit(@PathVariable Long noticeId, Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-notices", "编辑观演须知");
        model.addAttribute("formMode", "edit");
        model.addAttribute("noticeId", noticeId);
        return "admin/operation/notice/form";
    }

    @GetMapping("/config")
    public String relationConfig(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-notices", "须知关联配置");
        return "admin/operation/notice/relation-config";
    }
}
