package com.example.maimaibackend.controller.adminpage;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.config.AdminSessionConstants;
import com.example.maimaibackend.service.admin.AdminAuthService;
import com.example.maimaibackend.vo.admin.AdminLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminAuthPageController {

    private static final String LOGIN_REDIRECT_ATTRIBUTE = "ADMIN_LOGIN_REDIRECT";

    private final AdminAuthService adminAuthService;

    public AdminAuthPageController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String redirect,
            HttpServletRequest request,
            Model model
    ) {
        HttpSession session = request.getSession(false);
        String redirectPath = resolveRedirectPath(redirect, session);
        if (session != null && session.getAttribute(AdminSessionConstants.ADMIN_LOGIN_INFO) != null) {
            return "redirect:" + (redirectPath == null ? "/admin/dashboard" : redirectPath);
        }
        model.addAttribute("username", "");
        model.addAttribute("redirect", redirectPath == null ? "" : redirectPath);
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String redirect,
            HttpServletRequest request,
            Model model
    ) {
        HttpSession oldSession = request.getSession(false);
        String redirectPath = resolveRedirectPath(redirect, oldSession);
        try {
            AdminLoginVO loginInfo = adminAuthService.login(username, password);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute(AdminSessionConstants.ADMIN_LOGIN_INFO, loginInfo);
            return "redirect:" + (redirectPath == null ? "/admin/dashboard" : redirectPath);
        } catch (BusinessException e) {
            model.addAttribute("username", username == null ? "" : username);
            model.addAttribute("redirect", redirectPath == null ? "" : redirectPath);
            model.addAttribute("error", e.getMessage());
            return "admin/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/admin/login";
    }

    private String resolveRedirectPath(String requestRedirect, HttpSession session) {
        String redirectPath = normalizeAdminRedirect(requestRedirect);
        if (redirectPath != null) {
            return redirectPath;
        }
        if (session == null) {
            return null;
        }
        Object sessionRedirect = session.getAttribute(LOGIN_REDIRECT_ATTRIBUTE);
        return sessionRedirect instanceof String
                ? normalizeAdminRedirect((String) sessionRedirect)
                : null;
    }

    private String normalizeAdminRedirect(String redirectPath) {
        if (redirectPath == null) {
            return null;
        }
        String value = redirectPath.trim();
        if (value.isEmpty()
                || value.contains("\r")
                || value.contains("\n")
                || value.contains("\\")
                || value.contains("://")
                || value.startsWith("//")) {
            return null;
        }
        if (!value.equals("/admin") && !value.startsWith("/admin/")) {
            return null;
        }
        if (value.startsWith("/admin/login")) {
            return null;
        }
        return value;
    }
}
