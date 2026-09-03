package com.example.maimaibackend.util;

import com.example.maimaibackend.common.BusinessException;

public final class ValidateUtil {

    private ValidateUtil() {
    }

    public static void requirePositiveId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new BusinessException(name + "不能为空");
        }
    }

    public static String requireText(String text, String name, int maxLength) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException(name + "不能为空");
        }
        String value = text.trim();
        if (value.length() > maxLength) {
            throw new BusinessException(name + "长度不能超过" + maxLength + "个字符");
        }
        return value;
    }

    public static String requirePhone(String phone) {
        String value = requireText(phone, "手机号", 20);
        if (!value.matches("^1\\d{10}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        return value;
    }
}
