package com.example.maimaibackend.controller.adminpage;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/operation/banners")
public class AdminBannerPageController {

    @GetMapping
    public String list(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-banners", "Banner管理");
        return "admin/operation/banner/list";
    }

    @GetMapping("/new")
    public String create(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-banners", "新增Banner");
        model.addAttribute("formMode", "create");
        model.addAttribute("bannerId", null);
        return "admin/operation/banner/form";
    }

    @GetMapping("/{bannerId}/edit")
    public String edit(@PathVariable Long bannerId, Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-banners", "编辑Banner");
        model.addAttribute("formMode", "edit");
        model.addAttribute("bannerId", bannerId);
        return "admin/operation/banner/form";
    }
}
