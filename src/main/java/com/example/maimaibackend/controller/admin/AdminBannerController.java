package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminSaveBannerRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateBannerStatusRequest;
import com.example.maimaibackend.service.admin.AdminBannerService;
import com.example.maimaibackend.vo.admin.AdminBannerListPageVO;
import com.example.maimaibackend.vo.admin.AdminBannerVO;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/banners")
public class AdminBannerController {

    private final AdminBannerService adminBannerService;

    public AdminBannerController(AdminBannerService adminBannerService) {
        this.adminBannerService = adminBannerService;
    }

    @GetMapping
    public Result<AdminBannerListPageVO> getBannerList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String enableStatus,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(adminBannerService.getBannerList(keyword, enableStatus, pageNo, pageSize));
    }

    @GetMapping("/{bannerId}")
    public Result<AdminBannerVO> getBannerDetail(@PathVariable Long bannerId) {
        return Result.success(adminBannerService.getBannerDetail(bannerId));
    }

    @PostMapping
    public Result<AdminBannerVO> createBanner(@RequestBody AdminSaveBannerRequest request) {
        return Result.success(adminBannerService.createBanner(request));
    }

    @PutMapping("/{bannerId}")
    public Result<AdminBannerVO> updateBanner(
            @PathVariable Long bannerId,
            @RequestBody AdminSaveBannerRequest request
    ) {
        return Result.success(adminBannerService.updateBanner(bannerId, request));
    }

    @PutMapping("/{bannerId}/status")
    public Result<AdminOperateResponse> updateBannerStatus(
            @PathVariable Long bannerId,
            @RequestBody AdminUpdateBannerStatusRequest request
    ) {
        return Result.success(adminBannerService.updateBannerStatus(bannerId, request));
    }

    @DeleteMapping("/{bannerId}")
    public Result<AdminOperateResponse> deleteBanner(@PathVariable Long bannerId) {
        return Result.success(adminBannerService.deleteBanner(bannerId));
    }
}
