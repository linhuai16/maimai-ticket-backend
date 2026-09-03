package com.example.maimaibackend.controller.adminpage;

import com.example.maimaibackend.common.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "com.example.maimaibackend.controller.adminpage")
public class AdminPageExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusinessException(BusinessException e, HttpServletResponse response) {
        response.setStatus(e.getCode() == null ? 400 : e.getCode());
        return buildErrorPage(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e, HttpServletResponse response) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return buildErrorPage("页面加载失败，请检查数据库连接或稍后重试");
    }

    private ModelAndView buildErrorPage(String message) {
        ModelAndView modelAndView = new ModelAndView("admin/error");
        modelAndView.addObject("message", message);
        return modelAndView;
    }
}
