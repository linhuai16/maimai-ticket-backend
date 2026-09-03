package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminCreateSkuRequest;
import com.example.maimaibackend.dto.admin.AdminSaveProjectRequest;
import com.example.maimaibackend.dto.admin.AdminSaveSessionRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateSkuRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateSkuStockRequest;
import com.example.maimaibackend.dto.admin.AdminUpdatePlatformPriceRequest;
import com.example.maimaibackend.dto.admin.UpdateProjectStatusRequest;
import com.example.maimaibackend.dto.admin.UpdateSessionStatusRequest;
import com.example.maimaibackend.dto.admin.UpdateSkuStatusRequest;
import com.example.maimaibackend.service.admin.AdminPerformanceService;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminProjectDetailVO;
import com.example.maimaibackend.vo.admin.AdminProjectListPageVO;
import com.example.maimaibackend.vo.admin.AdminSessionItemVO;
import com.example.maimaibackend.vo.admin.AdminSkuItemVO;
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
@RequestMapping("/api/admin/performances")
public class AdminPerformanceController {

    private final AdminPerformanceService adminPerformanceService;

    public AdminPerformanceController(AdminPerformanceService adminPerformanceService) {
        this.adminPerformanceService = adminPerformanceService;
    }

    @GetMapping("/projects")
    public Result<AdminProjectListPageVO> getProjectList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String projectStatus,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(adminPerformanceService.getProjectList(
                keyword, categoryId, projectStatus, pageNo, pageSize
        ));
    }

    @GetMapping("/projects/{projectId}")
    public Result<AdminProjectDetailVO> getProjectDetail(@PathVariable Long projectId) {
        return Result.success(adminPerformanceService.getProjectDetail(projectId));
    }

    @PostMapping("/projects")
    public Result<AdminProjectDetailVO> createProject(@RequestBody AdminSaveProjectRequest request) {
        return Result.success(adminPerformanceService.createProject(request));
    }

    @PutMapping("/projects/{projectId}")
    public Result<AdminProjectDetailVO> updateProject(
            @PathVariable Long projectId,
            @RequestBody AdminSaveProjectRequest request
    ) {
        return Result.success(adminPerformanceService.updateProject(projectId, request));
    }

    @PutMapping("/projects/{projectId}/status")
    public Result<AdminOperateResponse> updateProjectStatus(
            @PathVariable Long projectId,
            @RequestBody UpdateProjectStatusRequest request
    ) {
        return Result.success(adminPerformanceService.updateProjectStatus(projectId, request));
    }

    @GetMapping("/projects/{projectId}/sessions")
    public Result<List<AdminSessionItemVO>> getProjectSessions(@PathVariable Long projectId) {
        return Result.success(adminPerformanceService.getProjectSessions(projectId));
    }

    @GetMapping("/sessions/{sessionId}")
    public Result<AdminSessionItemVO> getSessionDetail(@PathVariable Long sessionId) {
        return Result.success(adminPerformanceService.getSessionDetail(sessionId));
    }

    @PostMapping("/projects/{projectId}/sessions")
    public Result<AdminSessionItemVO> createSession(
            @PathVariable Long projectId,
            @RequestBody AdminSaveSessionRequest request
    ) {
        return Result.success(adminPerformanceService.createSession(projectId, request));
    }

    @PutMapping("/sessions/{sessionId}")
    public Result<AdminSessionItemVO> updateSession(
            @PathVariable Long sessionId,
            @RequestBody AdminSaveSessionRequest request
    ) {
        return Result.success(adminPerformanceService.updateSession(sessionId, request));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<AdminOperateResponse> deleteSession(@PathVariable Long sessionId) {
        return Result.success(adminPerformanceService.deleteSession(sessionId));
    }

    @PutMapping("/sessions/{sessionId}/status")
    public Result<AdminOperateResponse> updateSessionStatus(
            @PathVariable Long sessionId,
            @RequestBody UpdateSessionStatusRequest request
    ) {
        return Result.success(adminPerformanceService.updateSessionStatus(sessionId, request));
    }

    @GetMapping("/sessions/{sessionId}/skus")
    public Result<List<AdminSkuItemVO>> getSessionSkus(@PathVariable Long sessionId) {
        return Result.success(adminPerformanceService.getSessionSkus(sessionId));
    }

    @GetMapping("/skus/{skuId}")
    public Result<AdminSkuItemVO> getSkuDetail(@PathVariable Long skuId) {
        return Result.success(adminPerformanceService.getSkuDetail(skuId));
    }

    @PostMapping("/sessions/{sessionId}/skus")
    public Result<AdminSkuItemVO> createSku(
            @PathVariable Long sessionId,
            @RequestBody AdminCreateSkuRequest request
    ) {
        return Result.success(adminPerformanceService.createSku(sessionId, request));
    }

    @PutMapping("/skus/{skuId}")
    public Result<AdminSkuItemVO> updateSku(
            @PathVariable Long skuId,
            @RequestBody AdminUpdateSkuRequest request
    ) {
        return Result.success(adminPerformanceService.updateSku(skuId, request));
    }

    @PutMapping("/skus/{skuId}/platform-price")
    public Result<AdminSkuItemVO> updateSourceSkuPlatformPrice(
            @PathVariable Long skuId,
            @RequestBody AdminUpdatePlatformPriceRequest request
    ) {
        return Result.success(adminPerformanceService.updateSourceSkuPlatformPrice(skuId, request));
    }

    @PutMapping("/skus/{skuId}/stock")
    public Result<AdminSkuItemVO> updateSkuStock(
            @PathVariable Long skuId,
            @RequestBody AdminUpdateSkuStockRequest request
    ) {
        return Result.success(adminPerformanceService.updateSkuStock(skuId, request));
    }

    @PutMapping("/skus/{skuId}/status")
    public Result<AdminOperateResponse> updateSkuStatus(
            @PathVariable Long skuId,
            @RequestBody UpdateSkuStatusRequest request
    ) {
        return Result.success(adminPerformanceService.updateSkuStatus(skuId, request));
    }

    @DeleteMapping("/skus/{skuId}")
    public Result<AdminOperateResponse> deleteSku(@PathVariable Long skuId) {
        return Result.success(adminPerformanceService.deleteSku(skuId));
    }
}
