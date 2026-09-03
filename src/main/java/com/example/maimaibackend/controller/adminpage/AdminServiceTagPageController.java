package com.example.maimaibackend.controller.adminpage;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/operation/service-tags")
public class AdminServiceTagPageController {

    @GetMapping
    public String list(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-service-tags", "服务标签");
        return "admin/operation/service-tag/list";
    }

    @GetMapping("/new")
    public String create(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-service-tags", "新增服务标签");
        model.addAttribute("formMode", "create");
        model.addAttribute("serviceTagId", null);
        return "admin/operation/service-tag/form";
    }

    @GetMapping("/{tagId}/edit")
    public String edit(@PathVariable Long tagId, Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-service-tags", "编辑服务标签");
        model.addAttribute("formMode", "edit");
        model.addAttribute("serviceTagId", tagId);
        return "admin/operation/service-tag/form";
    }

    @GetMapping("/project-config")
    public String projectConfig(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-service-tags", "项目服务标签配置");
        return "admin/operation/service-tag/project-config";
    }
}
