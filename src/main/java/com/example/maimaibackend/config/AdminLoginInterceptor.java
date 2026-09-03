package com.example.maimaibackend.config;

import com.example.maimaibackend.vo.admin.AdminLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AdminLoginInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public AdminLoginInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        AdminLoginVO loginInfo = session == null
                ? null
                : (AdminLoginVO) session.getAttribute(AdminSessionConstants.ADMIN_LOGIN_INFO);
        if (loginInfo != null) {
            return true;
        }

        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/api/admin/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("code", 401);
            body.put("message", "管理员登录已失效，请重新登录");
            body.put("data", null);
            objectMapper.writeValue(response.getWriter(), body);
            return false;
        }

        String queryString = request.getQueryString();
        String target = requestUri + (queryString == null ? "" : "?" + queryString);
        request.getSession(true).setAttribute("ADMIN_LOGIN_REDIRECT", target);
        response.sendRedirect(request.getContextPath() + "/admin/login");
        return false;
    }
}
