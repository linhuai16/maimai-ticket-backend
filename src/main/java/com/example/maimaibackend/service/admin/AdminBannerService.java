package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminSaveBannerRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateBannerStatusRequest;
import com.example.maimaibackend.mapper.admin.AdminBannerMapper;
import com.example.maimaibackend.media.MediaBusinessType;
import com.example.maimaibackend.vo.admin.AdminBannerListPageVO;
import com.example.maimaibackend.vo.admin.AdminBannerVO;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminBannerService {

    private static final Set<String> ENABLE_STATUS_SET = new HashSet<>(Arrays.asList("ENABLED", "DISABLED"));

    private final AdminBannerMapper adminBannerMapper;
    private final AdminMediaService adminMediaService;

    public AdminBannerService(
            AdminBannerMapper adminBannerMapper,
            AdminMediaService adminMediaService
    ) {
        this.adminBannerMapper = adminBannerMapper;
        this.adminMediaService = adminMediaService;
    }

    public AdminBannerListPageVO getBannerList(String keyword, String enableStatus, Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        String safeKeyword = trimToNull(keyword);
        String safeStatus = normalizeStatus(enableStatus, false);
        int offset = (safePageNo - 1) * safePageSize;
        Integer total = adminBannerMapper.countBannerList(safeKeyword, safeStatus);
        List<AdminBannerVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminBannerMapper.selectBannerList(safeKeyword, safeStatus, safePageSize, offset);
        AdminBannerListPageVO vo = new AdminBannerListPageVO();
        vo.setTotal(total == null ? 0 : total);
        vo.setPageNo(safePageNo);
        vo.setPageSize(safePageSize);
        vo.setItems(items);
        return vo;
    }

    public AdminBannerVO getBannerDetail(Long bannerId) {
        requireId(bannerId, "bannerId");
        AdminBannerVO vo = adminBannerMapper.selectBannerById(bannerId);
        if (vo == null) {
            throw new BusinessException("Banner 不存在");
        }
        return vo;
    }

    public AdminBannerVO createBanner(AdminSaveBannerRequest request) {
        validateSaveRequest(request);
        int inserted = adminBannerMapper.insertBanner(request);
        if (inserted != 1 || request.getBannerId() == null) {
            throw new BusinessException(409, "Banner 新增失败，请刷新后重试");
        }
        return getBannerDetail(request.getBannerId());
    }

    public AdminBannerVO updateBanner(Long bannerId, AdminSaveBannerRequest request) {
        requireId(bannerId, "bannerId");
        if (adminBannerMapper.countBannerById(bannerId) == 0) {
            throw new BusinessException("Banner 不存在");
        }
        validateSaveRequest(request);
        request.setBannerId(bannerId);
        if (adminBannerMapper.updateBanner(request) != 1) {
            throw new BusinessException(409, "Banner 数据已变化，请刷新后重试");
        }
        return getBannerDetail(bannerId);
    }

    public AdminOperateResponse updateBannerStatus(Long bannerId, AdminUpdateBannerStatusRequest request) {
        requireId(bannerId, "bannerId");
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        AdminBannerVO banner = getBannerDetail(bannerId);
        String status = normalizeStatus(request.getEnableStatus(), true);
        if ("ENABLED".equals(status)) {
            validateEnabledTarget(banner.getTargetProjectId(), banner.getTargetSessionId());
        }
        if (adminBannerMapper.updateBannerStatus(bannerId, status) != 1) {
            throw new BusinessException(409, "Banner 数据已变化，请刷新后重试");
        }
        return new AdminOperateResponse(true, "Banner 状态已更新");
    }

    public AdminOperateResponse deleteBanner(Long bannerId) {
        getBannerDetail(bannerId);
        if (adminBannerMapper.deleteBanner(bannerId) != 1) {
            throw new BusinessException(409, "Banner 数据已变化，请刷新后重试");
        }
        return new AdminOperateResponse(true, "Banner 已删除");
    }

    private void validateSaveRequest(AdminSaveBannerRequest request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        request.setBannerTitle(requireText(request.getBannerTitle(), "bannerTitle", 100));
        request.setImageUrl(adminMediaService.requireImageReference(
                request.getImageUrl(), MediaBusinessType.BANNER_IMAGE, "Banner 图片"
        ));
        requireId(request.getTargetProjectId(), "targetProjectId");
        if (adminBannerMapper.countProjectById(request.getTargetProjectId()) == 0) {
            throw new BusinessException("目标演出项目不存在");
        }
        if (request.getTargetSessionId() != null) {
            requireId(request.getTargetSessionId(), "targetSessionId");
            if (adminBannerMapper.countSessionBelongProject(request.getTargetSessionId(), request.getTargetProjectId()) == 0) {
                throw new BusinessException("目标场次不存在，或不属于目标演出项目");
            }
        }
        request.setEnableStatus(normalizeStatus(request.getEnableStatus(), true));
        if ("ENABLED".equals(request.getEnableStatus())) {
            validateEnabledTarget(request.getTargetProjectId(), request.getTargetSessionId());
        }
        if (request.getSortOrder() == null || request.getSortOrder() < 0) {
            throw new BusinessException("sortOrder 不能小于 0");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BusinessException("startTime 和 endTime 不能为空");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException("endTime 必须晚于 startTime");
        }
    }

    private void validateEnabledTarget(Long projectId, Long sessionId) {
        String projectStatus = adminBannerMapper.selectProjectStatus(projectId);
        if (projectStatus == null) {
            throw new BusinessException("目标演出项目不存在");
        }
        if ("OFFLINE".equals(projectStatus)) {
            throw new BusinessException("目标演出项目已下架，Banner 不能启用");
        }
        if (sessionId != null) {
            String sessionStatus = adminBannerMapper.selectSessionStatusBelongProject(sessionId, projectId);
            if (sessionStatus == null) {
                throw new BusinessException("目标场次不存在，或不属于目标演出项目");
            }
            if ("OFFLINE".equals(sessionStatus)) {
                throw new BusinessException("目标场次已下架，Banner 不能启用");
            }
        }
    }

    private String normalizeStatus(String value, boolean required) {
        String status = trimToNull(value);
        if (status == null) {
            if (required) throw new BusinessException("enableStatus 不能为空");
            return null;
        }
        status = status.toUpperCase();
        if (!ENABLE_STATUS_SET.contains(status)) {
            throw new BusinessException("enableStatus 仅支持 ENABLED 或 DISABLED");
        }
        return status;
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
