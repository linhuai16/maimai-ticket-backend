package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminUpdateUserStatusRequest;
import com.example.maimaibackend.mapper.admin.AdminUserMapper;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminUserDetailVO;
import com.example.maimaibackend.vo.admin.AdminUserItemVO;
import com.example.maimaibackend.vo.admin.AdminUserListPageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminUserService {

    private static final Set<String> ACCOUNT_STATUS_SET = new HashSet<>(Arrays.asList("NORMAL", "DISABLED"));
    private final AdminUserMapper adminUserMapper;

    public AdminUserService(AdminUserMapper adminUserMapper) {
        this.adminUserMapper = adminUserMapper;
    }

    public AdminUserListPageVO getUserList(String keyword, String accountStatus, Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        String safeKeyword = trimToNull(keyword);
        String safeStatus = trimToNull(accountStatus);
        if (safeStatus != null && !ACCOUNT_STATUS_SET.contains(safeStatus)) {
            throw new BusinessException("账号状态不合法");
        }
        int offset = (safePageNo - 1) * safePageSize;
        Integer total = adminUserMapper.countUserList(safeKeyword, safeStatus);
        List<AdminUserItemVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminUserMapper.selectUserList(safeKeyword, safeStatus, safePageSize, offset);
        AdminUserListPageVO vo = new AdminUserListPageVO();
        vo.setTotal(total == null ? 0 : total);
        vo.setPageNo(safePageNo);
        vo.setPageSize(safePageSize);
        vo.setItems(items);
        return vo;
    }

    public AdminUserDetailVO getUserDetail(Long userId) {
        validateUserId(userId);
        AdminUserDetailVO vo = adminUserMapper.selectUserDetail(userId);
        if (vo == null) {
            throw new BusinessException("用户不存在");
        }
        vo.setWants(adminUserMapper.selectUserWants(userId));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminOperateResponse updateUserStatus(Long userId, AdminUpdateUserStatusRequest request) {
        validateUserId(userId);
        String targetStatus = trimToNull(request == null ? null : request.getAccountStatus());
        if (targetStatus == null) {
            throw new BusinessException("accountStatus 不能为空");
        }
        if (!ACCOUNT_STATUS_SET.contains(targetStatus)) {
            throw new BusinessException("账号状态不合法");
        }
        String currentStatus = adminUserMapper.selectUserStatus(userId);
        if (currentStatus == null) {
            throw new BusinessException("用户不存在");
        }
        if (targetStatus.equals(currentStatus)) {
            return new AdminOperateResponse(true, "用户状态未变化");
        }
        int updated = adminUserMapper.updateUserStatusIfCurrent(userId, currentStatus, targetStatus);
        if (updated != 1) {
            throw new BusinessException(409, "用户状态已变化，请刷新后重试");
        }
        return new AdminOperateResponse(true, "用户状态已更新");
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("userId 必须为正整数");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
