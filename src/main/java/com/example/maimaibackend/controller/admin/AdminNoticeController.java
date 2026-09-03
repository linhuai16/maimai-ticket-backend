package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminSaveNoticeRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateNoticeIdsRequest;
import com.example.maimaibackend.service.admin.AdminNoticeService;
import com.example.maimaibackend.vo.admin.AdminNoticeVO;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminProjectNoticeConfigVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    public AdminNoticeController(AdminNoticeService adminNoticeService) {
        this.adminNoticeService = adminNoticeService;
    }

    @GetMapping("/notices")
    public Result<List<AdminNoticeVO>> getNoticeList(@RequestParam(required = false) String keyword) {
        return Result.success(adminNoticeService.getNoticeList(keyword));
    }

    @GetMapping("/notices/{noticeId}")
    public Result<AdminNoticeVO> getNoticeDetail(@PathVariable Long noticeId) {
        return Result.success(adminNoticeService.getNoticeDetail(noticeId));
    }

    @PostMapping("/notices")
    public Result<AdminNoticeVO> createNotice(@RequestBody AdminSaveNoticeRequest request) {
        return Result.success(adminNoticeService.createNotice(request));
    }

    @PutMapping("/notices/{noticeId}")
    public Result<AdminNoticeVO> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody AdminSaveNoticeRequest request
    ) {
        return Result.success(adminNoticeService.updateNotice(noticeId, request));
    }

    @DeleteMapping("/notices/{noticeId}")
    public Result<AdminOperateResponse> deleteNotice(@PathVariable Long noticeId) {
        return Result.success(adminNoticeService.deleteNotice(noticeId));
    }

    @GetMapping("/projects/{projectId}/notices")
    public Result<AdminProjectNoticeConfigVO> getProjectNoticeConfig(@PathVariable Long projectId) {
        return Result.success(adminNoticeService.getProjectNoticeConfig(projectId));
    }

    @PutMapping("/projects/{projectId}/notices")
    public Result<AdminProjectNoticeConfigVO> updateProjectNotices(
            @PathVariable Long projectId,
            @RequestBody AdminUpdateNoticeIdsRequest request
    ) {
        return Result.success(adminNoticeService.updateProjectNotices(projectId, request));
    }
}
