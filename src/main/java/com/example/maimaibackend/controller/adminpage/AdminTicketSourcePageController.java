package com.example.maimaibackend.controller.adminpage;

import com.example.maimaibackend.config.AdminSessionConstants;
import com.example.maimaibackend.vo.admin.AdminLoginVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/ticket-sources")
public class AdminTicketSourcePageController {

    @GetMapping({"", "/"})
    public String overview(Model model, HttpSession session) { return page(model,session,"ticket-source-overview","第三方票源概览","admin/ticket-source/overview"); }
    @GetMapping("/resources")
    public String resources() { return "redirect:/admin/performances/projects"; }
    @GetMapping("/campaigns")
    public String campaigns(Model model, HttpSession session) { return page(model,session,"ticket-source-campaigns","活动与优惠","admin/ticket-source/campaigns"); }
    @GetMapping("/orders")
    public String orders(Model model, HttpSession session) { return page(model,session,"ticket-source-orders","订单监控","admin/ticket-source/orders"); }
    @GetMapping("/fulfillment")
    public String fulfillment(Model model, HttpSession session) { return page(model,session,"ticket-source-fulfillment","履约监控","admin/ticket-source/fulfillment"); }
    @GetMapping("/refunds")
    public String refunds(Model model, HttpSession session) { return page(model,session,"ticket-source-refunds","退票 / 退款协同","admin/ticket-source/refunds"); }
    @GetMapping("/operations")
    public String operations(Model model, HttpSession session) { return page(model,session,"ticket-source-operations","回调、对账与网关日志","admin/ticket-source/operations"); }
    @GetMapping("/settlements")
    public String settlements(Model model, HttpSession session) { return page(model,session,"ticket-source-settlements","账期结算","admin/ticket-source/settlements"); }
    @GetMapping("/mock")
    public String mock(Model model, HttpSession session) { return page(model,session,"ticket-source-mock","MOCK_DAMAI 测试控制","admin/ticket-source/mock"); }

    private String page(Model model,HttpSession session,String active,String title,String template){
        model.addAttribute("activeMenu",active);
        model.addAttribute("pageTitle",title);
        model.addAttribute("adminUser",(AdminLoginVO) session.getAttribute(AdminSessionConstants.ADMIN_LOGIN_INFO));
        return template;
    }
}
