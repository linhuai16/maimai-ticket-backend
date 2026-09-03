package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminSaveServiceTagRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateTagIdsRequest;
import com.example.maimaibackend.service.admin.AdminServiceTagService;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminProjectServiceTagConfigVO;
import com.example.maimaibackend.vo.admin.AdminServiceTagVO;
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
public class AdminServiceTagController {

    private final AdminServiceTagService adminServiceTagService;

    public AdminServiceTagController(AdminServiceTagService adminServiceTagService) {
        this.adminServiceTagService = adminServiceTagService;
    }

    @GetMapping("/service-tags")
    public Result<List<AdminServiceTagVO>> getServiceTagList(@RequestParam(required = false) String keyword) {
        return Result.success(adminServiceTagService.getServiceTagList(keyword));
    }

    @GetMapping("/service-tags/{tagId}")
    public Result<AdminServiceTagVO> getServiceTagDetail(@PathVariable Long tagId) {
        return Result.success(adminServiceTagService.getServiceTagDetail(tagId));
    }

    @PostMapping("/service-tags")
    public Result<AdminServiceTagVO> createServiceTag(@RequestBody AdminSaveServiceTagRequest request) {
        return Result.success(adminServiceTagService.createServiceTag(request));
    }

    @PutMapping("/service-tags/{tagId}")
    public Result<AdminServiceTagVO> updateServiceTag(
            @PathVariable Long tagId,
            @RequestBody AdminSaveServiceTagRequest request
    ) {
        return Result.success(adminServiceTagService.updateServiceTag(tagId, request));
    }

    @DeleteMapping("/service-tags/{tagId}")
    public Result<AdminOperateResponse> deleteServiceTag(@PathVariable Long tagId) {
        return Result.success(adminServiceTagService.deleteServiceTag(tagId));
    }

    @GetMapping("/projects/{projectId}/service-tags")
    public Result<AdminProjectServiceTagConfigVO> getProjectServiceTagConfig(@PathVariable Long projectId) {
        return Result.success(adminServiceTagService.getProjectServiceTagConfig(projectId));
    }

    @PutMapping("/projects/{projectId}/service-tags")
    public Result<AdminProjectServiceTagConfigVO> updateProjectServiceTags(
            @PathVariable Long projectId,
            @RequestBody AdminUpdateTagIdsRequest request
    ) {
        return Result.success(adminServiceTagService.updateProjectServiceTags(projectId, request));
    }
}
