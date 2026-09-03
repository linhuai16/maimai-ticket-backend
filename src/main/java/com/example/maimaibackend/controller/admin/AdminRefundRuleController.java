package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.service.admin.AdminRefundRuleService;
import com.example.maimaibackend.vo.admin.AdminRefundRuleDetailVO;
import com.example.maimaibackend.vo.admin.AdminRefundRuleListPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminRefundRuleController {

    private final AdminRefundRuleService adminRefundRuleService;

    public AdminRefundRuleController(AdminRefundRuleService adminRefundRuleService) {
        this.adminRefundRuleService = adminRefundRuleService;
    }

    @GetMapping("/refund-rules")
    public Result<AdminRefundRuleListPageVO> getRefundRuleList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String refundType,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(adminRefundRuleService.getRefundRuleList(keyword, refundType, pageNo, pageSize));
    }

    @GetMapping("/projects/{projectId}/refund-rule")
    public Result<AdminRefundRuleDetailVO> getProjectRefundRule(@PathVariable Long projectId) {
        return Result.success(adminRefundRuleService.getProjectRefundRule(projectId));
    }
}
