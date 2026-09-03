package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminRelationSaveDTO;
import com.example.maimaibackend.dto.admin.AdminSaveServiceTagRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateTagIdsRequest;
import com.example.maimaibackend.mapper.admin.AdminServiceTagMapper;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminProjectServiceTagConfigVO;
import com.example.maimaibackend.vo.admin.AdminServiceTagVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminServiceTagService {

    private static final Set<String> RESERVED_REFUND_TAG_NAMES = new HashSet<>();

    static {
        RESERVED_REFUND_TAG_NAMES.add("条件退");
        RESERVED_REFUND_TAG_NAMES.add("不可退");
    }

    private final AdminServiceTagMapper adminServiceTagMapper;

    public AdminServiceTagService(AdminServiceTagMapper adminServiceTagMapper) {
        this.adminServiceTagMapper = adminServiceTagMapper;
    }

    public List<AdminServiceTagVO> getServiceTagList(String keyword) {
        return adminServiceTagMapper.selectServiceTagList(trimToNull(keyword));
    }

    public AdminServiceTagVO getServiceTagDetail(Long tagId) {
        requireId(tagId, "tagId");
        AdminServiceTagVO vo = adminServiceTagMapper.selectServiceTagById(tagId);
        if (vo == null) throw new BusinessException("服务标签不存在");
        return vo;
    }

    public AdminServiceTagVO createServiceTag(AdminSaveServiceTagRequest request) {
        validateRequest(request, null, false);
        try {
            int inserted = adminServiceTagMapper.insertServiceTag(request);
            if (inserted != 1 || request.getTagId() == null) {
                throw new BusinessException(409, "服务标签新增失败，请刷新后重试");
            }
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(409, "服务标签名称已存在");
        }
        return getServiceTagDetail(request.getTagId());
    }

    public AdminServiceTagVO updateServiceTag(Long tagId, AdminSaveServiceTagRequest request) {
        AdminServiceTagVO existing = getServiceTagDetail(tagId);
        boolean systemTag = Boolean.TRUE.equals(existing.getSystemRefundTag());
        validateRequest(request, tagId, systemTag);
        if (systemTag && !existing.getTagName().equals(request.getTagName())) {
            throw new BusinessException("条件退和不可退的标签名称不可修改");
        }
        request.setTagId(tagId);
        try {
            if (adminServiceTagMapper.updateServiceTag(request) != 1) {
                throw new BusinessException(409, "服务标签数据已变化，请刷新后重试");
            }
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(409, "服务标签名称已存在");
        }
        return getServiceTagDetail(tagId);
    }

    public AdminOperateResponse deleteServiceTag(Long tagId) {
        AdminServiceTagVO existing = getServiceTagDetail(tagId);
        if (Boolean.TRUE.equals(existing.getSystemRefundTag())) {
            throw new BusinessException("条件退和不可退为系统退款标签，不可删除");
        }
        if (adminServiceTagMapper.countCapabilityRelations(tagId) > 0) {
            throw new BusinessException("该标签仍被 Provider 能力映射使用，请先调整能力映射");
        }
        if (adminServiceTagMapper.countProjectRelations(tagId) > 0) {
            throw new BusinessException("该标签仍被演出项目使用，请先移除项目关联");
        }
        if (adminServiceTagMapper.deleteServiceTag(tagId) != 1) {
            throw new BusinessException(409, "服务标签数据已变化，请刷新后重试");
        }
        return new AdminOperateResponse(true, "服务标签已删除");
    }

    public AdminProjectServiceTagConfigVO getProjectServiceTagConfig(Long projectId) {
        requireProject(projectId);
        AdminProjectServiceTagConfigVO vo = new AdminProjectServiceTagConfigVO();
        vo.setProjectId(projectId);
        vo.setProjectTitle(adminServiceTagMapper.selectProjectTitle(projectId));
        List<AdminServiceTagVO> manualTags = adminServiceTagMapper.selectProjectManualTags(projectId);
        List<AdminServiceTagVO> providerTags = adminServiceTagMapper.selectProjectProviderTags(projectId);
        List<AdminServiceTagVO> automaticTags = adminServiceTagMapper.selectProjectAutomaticRefundTags(projectId);
        vo.setManualTags(manualTags == null ? Collections.emptyList() : manualTags);
        vo.setProviderTags(providerTags == null ? Collections.emptyList() : providerTags);
        vo.setAutomaticRefundTags(automaticTags == null ? Collections.emptyList() : automaticTags);
        return vo;
    }

    @Transactional
    public AdminProjectServiceTagConfigVO updateProjectServiceTags(Long projectId, AdminUpdateTagIdsRequest request) {
        requireProject(projectId);
        if (request == null || request.getTagIds() == null) {
            throw new BusinessException("tagIds 不能为空；传空数组表示清空手工标签");
        }
        List<Long> tagIds = request.getTagIds();
        validateDistinctIds(tagIds, "tagIds");
        if (!tagIds.isEmpty()) {
            if (adminServiceTagMapper.countTagsByIds(tagIds) != tagIds.size()) {
                throw new BusinessException("tagIds 中存在无效服务标签");
            }
            if (adminServiceTagMapper.countSystemRefundTagsByIds(tagIds) > 0) {
                throw new BusinessException("条件退和不可退由退款规则自动生成，不能手工关联");
            }
            if (adminServiceTagMapper.countProviderTagRelationsByIds(projectId, tagIds) > 0) {
                throw new BusinessException("Provider 已同步的服务标签不能重复手工关联");
            }
        }
        adminServiceTagMapper.deleteProjectTagRelations(projectId);
        if (!tagIds.isEmpty()) {
            List<AdminRelationSaveDTO> items = new ArrayList<>();
            for (int i = 0; i < tagIds.size(); i++) items.add(new AdminRelationSaveDTO(tagIds.get(i), i + 1));
            if (adminServiceTagMapper.insertProjectTagRelations(projectId, items) != items.size()) {
                throw new BusinessException(409, "项目服务标签保存不完整，请重试");
            }
        }
        return getProjectServiceTagConfig(projectId);
    }

    private void validateRequest(AdminSaveServiceTagRequest request, Long currentTagId, boolean systemTag) {
        if (request == null) throw new BusinessException("请求参数不能为空");
        String tagName = requireText(request.getTagName(), "tagName", 50);
        String description = requireText(request.getDescription(), "description", 65535);
        validateTextBytes(description, "description");
        if (adminServiceTagMapper.countDuplicateName(currentTagId, tagName) > 0) {
            throw new BusinessException("服务标签名称已存在");
        }
        if (!systemTag && RESERVED_REFUND_TAG_NAMES.contains(tagName)) {
            throw new BusinessException("条件退和不可退为系统退款标签");
        }
        request.setTagName(tagName);
        request.setDescription(description);
    }

    private void validateTextBytes(String value, String fieldName) {
        if (value.getBytes(StandardCharsets.UTF_8).length > 65535) {
            throw new BusinessException(fieldName + " UTF-8 字节长度不能超过 65535");
        }
    }

    private void requireProject(Long projectId) {
        requireId(projectId, "projectId");
        if (adminServiceTagMapper.countProjectById(projectId) == 0) throw new BusinessException("演出项目不存在");
    }

    private void validateDistinctIds(List<Long> ids, String fieldName) {
        Set<Long> unique = new HashSet<>();
        for (Long id : ids) {
            requireId(id, fieldName);
            if (!unique.add(id)) throw new BusinessException(fieldName + " 不能包含重复 ID");
        }
    }

    private String requireText(String value, String fieldName, int maxLength) {
        String text = trimToNull(value);
        if (text == null) throw new BusinessException(fieldName + " 不能为空");
        if (text.length() > maxLength) throw new BusinessException(fieldName + " 长度不能超过 " + maxLength);
        return text;
    }

    private void requireId(Long value, String fieldName) {
        if (value == null || value <= 0) throw new BusinessException(fieldName + " 不合法");
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
