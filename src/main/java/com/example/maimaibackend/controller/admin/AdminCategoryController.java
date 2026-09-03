package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminSaveCategoryRequest;
import com.example.maimaibackend.service.admin.AdminCategoryService;
import com.example.maimaibackend.vo.admin.AdminCategoryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping
    public Result<List<AdminCategoryVO>> getCategoryList() {
        return Result.success(adminCategoryService.getCategoryList());
    }

    @GetMapping("/{categoryId}")
    public Result<AdminCategoryVO> getCategoryDetail(@PathVariable Long categoryId) {
        return Result.success(adminCategoryService.getCategoryDetail(categoryId));
    }

    @PutMapping("/{categoryId}")
    public Result<AdminCategoryVO> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody AdminSaveCategoryRequest request
    ) {
        return Result.success(adminCategoryService.updateCategory(categoryId, request));
    }
}
