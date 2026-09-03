package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.auth.LoginRequest;
import com.example.maimaibackend.dto.auth.SendSmsCodeRequest;
import com.example.maimaibackend.service.UserService;
import com.example.maimaibackend.vo.user.LoginResponse;
import com.example.maimaibackend.vo.user.SendSmsCodeResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sms-code")
    public Result<SendSmsCodeResponse> sendSmsCode(@RequestBody SendSmsCodeRequest request) {
        return Result.success(userService.sendSmsCode(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }
}
