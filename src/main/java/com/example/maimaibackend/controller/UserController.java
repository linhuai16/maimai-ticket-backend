package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.user.UpdateUserProfileRequest;
import com.example.maimaibackend.service.UserService;
import com.example.maimaibackend.vo.user.MinePageVO;
import com.example.maimaibackend.vo.user.UpdateUserProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<MinePageVO> getMinePage(@RequestParam Long userId) {
        return Result.success(userService.getMinePage(userId));
    }

    @PutMapping("/{userId}/profile")
    public Result<UpdateUserProfileResponse> updateProfile(
            @PathVariable Long userId,
            @RequestBody UpdateUserProfileRequest request
    ) {
        return Result.success(userService.updateProfile(userId, request));
    }
}
