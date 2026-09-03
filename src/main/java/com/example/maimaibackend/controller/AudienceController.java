package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.audience.CreateAudienceRequest;
import com.example.maimaibackend.dto.audience.UpdateAudienceRequest;
import com.example.maimaibackend.service.AudienceService;
import com.example.maimaibackend.vo.audience.AudienceDetailVO;
import com.example.maimaibackend.vo.audience.AudienceListPageVO;
import com.example.maimaibackend.vo.audience.AudienceOperateResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/audiences")
public class AudienceController {

    private final AudienceService audienceService;

    public AudienceController(AudienceService audienceService) {
        this.audienceService = audienceService;
    }

    @GetMapping
    public Result<AudienceListPageVO> getAudienceList(@PathVariable Long userId) {
        return Result.success(audienceService.getAudienceList(userId));
    }

    @GetMapping("/{audienceId}")
    public Result<AudienceDetailVO> getAudienceDetail(
            @PathVariable Long userId,
            @PathVariable Long audienceId
    ) {
        return Result.success(audienceService.getAudienceDetail(userId, audienceId));
    }

    @PostMapping
    public Result<AudienceDetailVO> createAudience(
            @PathVariable Long userId,
            @RequestBody CreateAudienceRequest request
    ) {
        return Result.success(audienceService.createAudience(userId, request));
    }

    @PutMapping("/{audienceId}")
    public Result<AudienceDetailVO> updateAudience(
            @PathVariable Long userId,
            @PathVariable Long audienceId,
            @RequestBody UpdateAudienceRequest request
    ) {
        return Result.success(audienceService.updateAudience(userId, audienceId, request));
    }

    @DeleteMapping("/{audienceId}")
    public Result<AudienceOperateResponse> deleteAudience(
            @PathVariable Long userId,
            @PathVariable Long audienceId
    ) {
        return Result.success(audienceService.deleteAudience(userId, audienceId));
    }

    @PutMapping("/{audienceId}/default")
    public Result<AudienceOperateResponse> setDefaultAudience(
            @PathVariable Long userId,
            @PathVariable Long audienceId
    ) {
        return Result.success(audienceService.setDefaultAudience(userId, audienceId));
    }
}
