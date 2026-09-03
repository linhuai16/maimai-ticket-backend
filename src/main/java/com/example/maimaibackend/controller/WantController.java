package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.service.WantService;
import com.example.maimaibackend.vo.want.WantActionResponse;
import com.example.maimaibackend.vo.want.WantListPageVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/wants")
public class WantController {

    private final WantService wantService;

    public WantController(WantService wantService) {
        this.wantService = wantService;
    }

    @GetMapping
    public Result<WantListPageVO> getWantList(@PathVariable Long userId) {
        return Result.success(wantService.getWantList(userId));
    }

    @PostMapping("/{projectId}")
    public Result<WantActionResponse> addWant(
            @PathVariable Long userId,
            @PathVariable Long projectId
    ) {
        return Result.success(wantService.addWant(userId, projectId));
    }

    @DeleteMapping("/{projectId}")
    public Result<WantActionResponse> cancelWant(
            @PathVariable Long userId,
            @PathVariable Long projectId
    ) {
        return Result.success(wantService.cancelWant(userId, projectId));
    }
}
