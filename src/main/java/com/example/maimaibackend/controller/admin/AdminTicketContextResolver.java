package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.config.AdminSessionConstants;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.vo.admin.AdminLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

final class AdminTicketContextResolver {
    private AdminTicketContextResolver() {
    }

    static TicketOperationContext resolve(HttpServletRequest request, HttpSession session) {
        AdminLoginVO admin = (AdminLoginVO) session.getAttribute(AdminSessionConstants.ADMIN_LOGIN_INFO);
        String operatorName = admin.getNickname() + "(" + admin.getUsername() + ")";
        return TicketOperationContext.admin(admin.getAdminId(), operatorName, resolveIp(request));
    }

    private static String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            String first = forwarded.split(",")[0].trim();
            return first.length() > 64 ? first.substring(0, 64) : first;
        }
        String ip = request.getRemoteAddr();
        return ip != null && ip.length() > 64 ? ip.substring(0, 64) : ip;
    }
}
