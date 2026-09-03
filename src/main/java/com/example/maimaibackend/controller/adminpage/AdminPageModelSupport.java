package com.example.maimaibackend.controller.adminpage;

import com.example.maimaibackend.config.AdminSessionConstants;
import com.example.maimaibackend.vo.admin.AdminLoginVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

final class AdminPageModelSupport {

    private AdminPageModelSupport() {
    }

    static void addCommonModel(Model model, HttpSession session, String activeMenu, String pageTitle) {
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("adminUser", (AdminLoginVO) session.getAttribute(AdminSessionConstants.ADMIN_LOGIN_INFO));
    }
}
