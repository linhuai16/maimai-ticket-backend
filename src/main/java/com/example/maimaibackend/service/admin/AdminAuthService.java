package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.admin.AdminAuthMapper;
import com.example.maimaibackend.util.PasswordUtil;
import com.example.maimaibackend.vo.admin.AdminAccountVO;
import com.example.maimaibackend.vo.admin.AdminLoginVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminAuthService {

    private final AdminAuthMapper adminAuthMapper;

    public AdminAuthService(AdminAuthMapper adminAuthMapper) {
        this.adminAuthMapper = adminAuthMapper;
    }

    public AdminLoginVO login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        if (password == null || password.isEmpty()) {
            throw new BusinessException("请输入密码");
        }
        AdminAccountVO account = adminAuthMapper.selectByUsername(normalizedUsername);
        if (account == null || !PasswordUtil.matches(password, account.getPasswordHash())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        if (!"ENABLED".equals(account.getAccountStatus())) {
            throw new BusinessException(403, "管理员账号已被禁用");
        }
        adminAuthMapper.updateLastLoginTime(account.getAdminId());
        return new AdminLoginVO(
                account.getAdminId(),
                account.getUsername(),
                account.getNickname(),
                LocalDateTime.now()
        );
    }

    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("请输入管理员账号");
        }
        String value = username.trim();
        if (value.length() > 50) {
            throw new BusinessException("管理员账号长度不能超过 50");
        }
        return value;
    }
}
