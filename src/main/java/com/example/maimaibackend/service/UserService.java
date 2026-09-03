package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.auth.LoginRequest;
import com.example.maimaibackend.dto.auth.SendSmsCodeRequest;
import com.example.maimaibackend.dto.user.UpdateUserProfileRequest;
import com.example.maimaibackend.mapper.UserMapper;
import com.example.maimaibackend.util.ValidateUtil;
import com.example.maimaibackend.vo.user.LoginResponse;
import com.example.maimaibackend.vo.user.MinePageVO;
import com.example.maimaibackend.vo.user.MineUserVO;
import com.example.maimaibackend.vo.user.SendSmsCodeResponse;
import com.example.maimaibackend.vo.user.UpdateUserProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final String MOCK_SMS_CODE = "123456";

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public SendSmsCodeResponse sendSmsCode(SendSmsCodeRequest request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        ValidateUtil.requirePhone(request.getPhone());
        return new SendSmsCodeResponse(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String phone = ValidateUtil.requirePhone(request.getPhone());
        String smsCode = ValidateUtil.requireText(request.getSmsCode(), "验证码", 6);
        if (!MOCK_SMS_CODE.equals(smsCode)) {
            throw new BusinessException("验证码不正确");
        }

        MineUserVO user = userMapper.selectUserByPhone(phone);
        if (user == null) {
            user = new MineUserVO();
            user.setPhone(phone);
            user.setNickname(buildDefaultNickname(phone));
            user.setAvatarUrl(null);
            user.setAccountStatus("NORMAL");
            userMapper.insertUser(user);
        }

        if (!"NORMAL".equals(user.getAccountStatus())) {
            throw new BusinessException("账号状态异常，无法登录");
        }

        String token = "mock-token-" + user.getUserId() + "-" + System.currentTimeMillis();
        return new LoginResponse(token, user);
    }

    public MinePageVO getMinePage(Long userId) {
        MineUserVO user = requireUser(userId);
        MinePageVO vo = new MinePageVO();
        vo.setUser(user);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public UpdateUserProfileResponse updateProfile(Long userId, UpdateUserProfileRequest request) {
        ValidateUtil.requirePositiveId(userId, "用户ID");
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        requireUser(userId);
        String nickname = ValidateUtil.requireText(request.getNickname(), "昵称", 50);
        String phone = ValidateUtil.requirePhone(request.getPhone());
        String avatarUrl = request.getAvatarUrl() == null ? null : request.getAvatarUrl().trim();
        if (avatarUrl != null && avatarUrl.length() > 500) {
            throw new BusinessException("头像地址长度不能超过500个字符");
        }
        if (userMapper.countPhoneUsedByOther(phone, userId) > 0) {
            throw new BusinessException("手机号已被其他账号占用");
        }
        userMapper.updateUserProfile(userId, nickname, phone, avatarUrl);
        MineUserVO user = userMapper.selectUserById(userId);
        return new UpdateUserProfileResponse(true, "保存成功", user);
    }

    public MineUserVO requireUser(Long userId) {
        ValidateUtil.requirePositiveId(userId, "用户ID");
        MineUserVO user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private String buildDefaultNickname(String phone) {
        if (phone != null && phone.length() >= 4) {
            return "用户" + phone.substring(phone.length() - 4);
        }
        return "新用户";
    }
}
