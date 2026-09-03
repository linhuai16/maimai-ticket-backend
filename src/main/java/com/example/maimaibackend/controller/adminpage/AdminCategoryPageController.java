package com.example.maimaibackend.controller.adminpage;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/operation/categories")
public class AdminCategoryPageController {

    @GetMapping
    public String list(Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-categories", "演出分类");
        return "admin/operation/category/list";
    }

    @GetMapping("/{categoryId}/edit")
    public String edit(@PathVariable Long categoryId, Model model, HttpSession session) {
        AdminPageModelSupport.addCommonModel(model, session, "operation-categories", "编辑演出分类");
        model.addAttribute("categoryId", categoryId);
        return "admin/operation/category/form";
    }
}
