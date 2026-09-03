package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminSaveCategoryRequest;
import com.example.maimaibackend.mapper.admin.AdminCategoryMapper;
import com.example.maimaibackend.media.MediaBusinessType;
import com.example.maimaibackend.vo.admin.AdminCategoryVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCategoryService {

    private final AdminCategoryMapper adminCategoryMapper;
    private final AdminMediaService adminMediaService;

    public AdminCategoryService(
            AdminCategoryMapper adminCategoryMapper,
            AdminMediaService adminMediaService
    ) {
        this.adminCategoryMapper = adminCategoryMapper;
        this.adminMediaService = adminMediaService;
    }

    public List<AdminCategoryVO> getCategoryList() {
        return adminCategoryMapper.selectCategoryList();
    }

    public AdminCategoryVO getCategoryDetail(Long categoryId) {
        requireId(categoryId, "categoryId");
        AdminCategoryVO vo = adminCategoryMapper.selectCategoryById(categoryId);
        if (vo == null) {
            throw new BusinessException("分类不存在");
        }
        return vo;
    }

    public AdminCategoryVO updateCategory(Long categoryId, AdminSaveCategoryRequest request) {
        requireId(categoryId, "categoryId");
        if (adminCategoryMapper.countCategoryById(categoryId) == 0) {
            throw new BusinessException("分类不存在");
        }
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String categoryName = requireText(request.getCategoryName(), "categoryName", 50);
        String iconUrl = adminMediaService.requireImageReference(
                request.getIconUrl(), MediaBusinessType.CATEGORY_ICON, "分类图标"
        );
        if (request.getSortOrder() == null || request.getSortOrder() < 0) {
            throw new BusinessException("sortOrder 不能小于 0");
        }
        if (adminCategoryMapper.countDuplicateName(categoryId, categoryName) > 0) {
            throw new BusinessException("分类名称已存在");
        }
        request.setCategoryName(categoryName);
        request.setIconUrl(iconUrl);
        try {
            int updated = adminCategoryMapper.updateCategory(categoryId, request);
            if (updated != 1) {
                throw new BusinessException(409, "分类数据已变化，请刷新后重试");
            }
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(409, "分类名称已存在");
        }
        return getCategoryDetail(categoryId);
    }

    private String requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(fieldName + " 不能为空");
        }
        String text = value.trim();
        if (text.length() > maxLength) {
            throw new BusinessException(fieldName + " 长度不能超过 " + maxLength);
        }
        return text;
    }

    private void requireId(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new BusinessException(fieldName + " 不合法");
        }
    }
}
