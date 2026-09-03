package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminRelationSaveDTO;
import com.example.maimaibackend.dto.admin.AdminSaveNoticeRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateNoticeIdsRequest;
import com.example.maimaibackend.mapper.admin.AdminNoticeMapper;
import com.example.maimaibackend.media.MediaBusinessType;
import com.example.maimaibackend.vo.admin.AdminNoticeVO;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminProjectNoticeConfigVO;
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
public class AdminNoticeService {

    private final AdminNoticeMapper adminNoticeMapper;
    private final AdminMediaService adminMediaService;

    public AdminNoticeService(
            AdminNoticeMapper adminNoticeMapper,
            AdminMediaService adminMediaService
    ) {
        this.adminNoticeMapper = adminNoticeMapper;
        this.adminMediaService = adminMediaService;
    }

    public List<AdminNoticeVO> getNoticeList(String keyword) {
        return adminNoticeMapper.selectNoticeList(trimToNull(keyword));
    }

    public AdminNoticeVO getNoticeDetail(Long noticeId) {
        requireId(noticeId, "noticeId");
        AdminNoticeVO vo = adminNoticeMapper.selectNoticeById(noticeId);
        if (vo == null) throw new BusinessException("观演须知不存在");
        return vo;
    }

    public AdminNoticeVO createNotice(AdminSaveNoticeRequest request) {
        validateSaveRequest(request, null);
        try {
            int inserted = adminNoticeMapper.insertNotice(request);
            if (inserted != 1 || request.getNoticeId() == null) {
                throw new BusinessException(409, "观演须知新增失败，请刷新后重试");
            }
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(409, "观演须知标题已存在");
        }
        return getNoticeDetail(request.getNoticeId());
    }

    public AdminNoticeVO updateNotice(Long noticeId, AdminSaveNoticeRequest request) {
        getNoticeDetail(noticeId);
        validateSaveRequest(request, noticeId);
        request.setNoticeId(noticeId);
        try {
            if (adminNoticeMapper.updateNotice(request) != 1) {
                throw new BusinessException(409, "观演须知数据已变化，请刷新后重试");
            }
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(409, "观演须知标题已存在");
        }
        return getNoticeDetail(noticeId);
    }

    public AdminOperateResponse deleteNotice(Long noticeId) {
        getNoticeDetail(noticeId);
        if (adminNoticeMapper.countProjectRelations(noticeId) > 0) {
            throw new BusinessException("该观演须知仍被演出项目使用，请先移除关联");
        }
        if (adminNoticeMapper.deleteNotice(noticeId) != 1) {
            throw new BusinessException(409, "观演须知数据已变化，请刷新后重试");
        }
        return new AdminOperateResponse(true, "观演须知已删除");
    }

    public AdminProjectNoticeConfigVO getProjectNoticeConfig(Long projectId) {
        requireProject(projectId);
        AdminProjectNoticeConfigVO vo = adminNoticeMapper.selectProjectNoticeBase(projectId);
        List<AdminNoticeVO> projectNotices = adminNoticeMapper.selectProjectNotices(projectId);
        List<AdminNoticeVO> providerNotices = adminNoticeMapper.selectProviderProjectNotices(projectId);
        vo.setProjectNotices(projectNotices == null ? Collections.emptyList() : projectNotices);
        vo.setProviderNotices(providerNotices == null ? Collections.emptyList() : providerNotices);
        return vo;
    }

    @Transactional
    public AdminProjectNoticeConfigVO updateProjectNotices(Long projectId, AdminUpdateNoticeIdsRequest request) {
        requireProject(projectId);
        List<Long> noticeIds = validateNoticeIdsRequest(request);
        if (!noticeIds.isEmpty() && adminNoticeMapper.countProviderProjectNoticeRelationsByIds(projectId, noticeIds) > 0) {
            throw new BusinessException("Provider 已同步的项目须知不能重复手工关联");
        }
        adminNoticeMapper.deleteProjectNoticeRelations(projectId);
        if (!noticeIds.isEmpty()) {
            List<AdminRelationSaveDTO> items = toRelations(noticeIds);
            if (adminNoticeMapper.insertProjectNoticeRelations(projectId, items) != items.size()) {
                throw new BusinessException(409, "项目特殊须知保存不完整，请重试");
            }
        }
        return getProjectNoticeConfig(projectId);
    }

    private List<Long> validateNoticeIdsRequest(AdminUpdateNoticeIdsRequest request) {
        if (request == null || request.getNoticeIds() == null) {
            throw new BusinessException("noticeIds 不能为空；传空数组表示清空关联");
        }
        List<Long> noticeIds = request.getNoticeIds();
        validateDistinctIds(noticeIds, "noticeIds");
        if (!noticeIds.isEmpty() && adminNoticeMapper.countNoticesByIds(noticeIds) != noticeIds.size()) {
            throw new BusinessException("noticeIds 中存在无效观演须知");
        }
        return noticeIds;
    }

    private List<AdminRelationSaveDTO> toRelations(List<Long> ids) {
        List<AdminRelationSaveDTO> items = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) items.add(new AdminRelationSaveDTO(ids.get(i), i + 1));
        return items;
    }

    private void validateSaveRequest(AdminSaveNoticeRequest request, Long currentNoticeId) {
        if (request == null) throw new BusinessException("请求参数不能为空");
        String title = requireText(request.getTitle(), "title", 50);
        String description = requireText(request.getDescription(), "description", 65535);
        if (description.getBytes(StandardCharsets.UTF_8).length > 65535) {
            throw new BusinessException("description UTF-8 字节长度不能超过 65535");
        }
        String iconUrl = adminMediaService.requireImageReference(
                request.getIconUrl(), MediaBusinessType.NOTICE_ICON, "观演须知图标"
        );
        if (adminNoticeMapper.countDuplicateTitle(currentNoticeId, title) > 0) {
            throw new BusinessException("观演须知标题已存在");
        }
        request.setTitle(title);
        request.setDescription(description);
        request.setIconUrl(iconUrl);
    }

    private void requireProject(Long projectId) {
        requireId(projectId, "projectId");
        if (adminNoticeMapper.countProjectById(projectId) == 0) throw new BusinessException("演出项目不存在");
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
