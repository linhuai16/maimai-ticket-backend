package com.example.maimaibackend.controller.adminpage;

import com.example.maimaibackend.config.AdminSessionConstants;
import com.example.maimaibackend.service.admin.AdminCategoryService;
import com.example.maimaibackend.service.admin.AdminDashboardService;
import com.example.maimaibackend.vo.admin.AdminLoginVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final AdminDashboardService adminDashboardService;
    private final AdminCategoryService adminCategoryService;

    public AdminPageController(
            AdminDashboardService adminDashboardService,
            AdminCategoryService adminCategoryService
    ) {
        this.adminDashboardService = adminDashboardService;
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping({"", "/"})
    public String index() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        addCommonModel(model, session, "dashboard", "数据概览");
        model.addAttribute("summary", adminDashboardService.getDashboardSummary());
        return "admin/dashboard";
    }

    @GetMapping("/performances/projects")
    public String projectList(Model model, HttpSession session) {
        addCommonModel(model, session, "performance-projects", "演出项目");
        model.addAttribute("categories", adminCategoryService.getCategoryList());
        return "admin/performance/project-list";
    }

    @GetMapping("/performances/projects/new")
    public String createProject(Model model, HttpSession session) {
        addCommonModel(model, session, "performance-projects", "新建演出项目");
        model.addAttribute("categories", adminCategoryService.getCategoryList());
        model.addAttribute("formMode", "create");
        model.addAttribute("projectId", null);
        return "admin/performance/project-form";
    }

    @GetMapping("/performances/projects/{projectId}/edit")
    public String editProject(
            @PathVariable Long projectId,
            Model model,
            HttpSession session
    ) {
        addCommonModel(model, session, "performance-projects", "编辑演出项目");
        model.addAttribute("categories", adminCategoryService.getCategoryList());
        model.addAttribute("formMode", "edit");
        model.addAttribute("projectId", projectId);
        return "admin/performance/project-form";
    }

    @GetMapping("/performances/sessions")
    public String sessionManagement(
            @RequestParam(required = false) Long projectId,
            Model model,
            HttpSession session
    ) {
        addCommonModel(model, session, "performance-sessions", "演出场次");
        model.addAttribute("selectedProjectId", projectId);
        return "admin/performance/session-list";
    }

    @GetMapping("/performances/skus")
    public String skuManagement(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sessionId,
            Model model,
            HttpSession session
    ) {
        addCommonModel(model, session, "performance-skus", "票档管理");
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("selectedSessionId", sessionId);
        return "admin/performance/sku-list";
    }

    @GetMapping("/venues")
    public String venueManagement(Model model, HttpSession session) {
        addCommonModel(model, session, "venues", "场馆管理");
        return "admin/performance/venue-list";
    }

    @GetMapping("/orders")
    public String orderManagement(Model model, HttpSession session) {
        addCommonModel(model, session, "orders", "订单管理");
        return "admin/transaction/order-list";
    }

    @GetMapping("/refunds")
    public String refundManagement(Model model, HttpSession session) {
        addCommonModel(model, session, "refunds", "退款管理");
        return "admin/transaction/refund-list";
    }

    @GetMapping("/users")
    public String userManagement(Model model, HttpSession session) {
        addCommonModel(model, session, "users", "用户管理");
        return "admin/user/user-list";
    }


    private void addCommonModel(Model model, HttpSession session, String activeMenu, String pageTitle) {
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute(
                "adminUser",
                (AdminLoginVO) session.getAttribute(AdminSessionConstants.ADMIN_LOGIN_INFO)
        );
    }
}
