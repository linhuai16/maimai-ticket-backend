package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.config.AdminSessionConstants;
import com.example.maimaibackend.dto.admin.AdminLoginRequest;
import com.example.maimaibackend.service.admin.AdminAuthService;
import com.example.maimaibackend.vo.admin.AdminLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginRequest request, HttpServletRequest servletRequest) {
        AdminLoginVO loginInfo = adminAuthService.login(
                request == null ? null : request.getUsername(),
                request == null ? null : request.getPassword()
        );
        HttpSession oldSession = servletRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        servletRequest.getSession(true).setAttribute(AdminSessionConstants.ADMIN_LOGIN_INFO, loginInfo);
        return Result.success(loginInfo);
    }

    @GetMapping("/me")
    public Result<AdminLoginVO> me(HttpSession session) {
        return Result.success((AdminLoginVO) session.getAttribute(AdminSessionConstants.ADMIN_LOGIN_INFO));
    }

    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.success(true);
    }
}
