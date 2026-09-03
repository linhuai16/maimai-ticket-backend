package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminUpdateUserStatusRequest;
import com.example.maimaibackend.service.admin.AdminUserService;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminUserDetailVO;
import com.example.maimaibackend.vo.admin.AdminUserListPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Result<AdminUserListPageVO> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(adminUserService.getUserList(keyword, accountStatus, pageNo, pageSize));
    }

    @GetMapping("/{userId}")
    public Result<AdminUserDetailVO> getUserDetail(@PathVariable Long userId) {
        return Result.success(adminUserService.getUserDetail(userId));
    }

    @PutMapping("/{userId}/status")
    public Result<AdminOperateResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody AdminUpdateUserStatusRequest request
    ) {
        return Result.success(adminUserService.updateUserStatus(userId, request));
    }
}
