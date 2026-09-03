package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.service.PerformanceService;
import com.example.maimaibackend.vo.performance.CategoryPageVO;
import com.example.maimaibackend.vo.performance.PerformanceDetailVO;
import com.example.maimaibackend.vo.performance.SearchResultPageVO;
import com.example.maimaibackend.vo.performance.TicketSelectPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performances")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/category")
    public Result<CategoryPageVO> getCategoryPerformanceList(
            @RequestParam String cityName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortType,
            @RequestParam(required = false) String filterTime,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset
    ) {
        return Result.success(performanceService.getCategoryPerformanceList(
                cityName, categoryId, sortType, filterTime, limit, offset
        ));
    }

    @GetMapping("/search")
    public Result<SearchResultPageVO> searchPerformances(
            @RequestParam String cityName,
            @RequestParam String keyword,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset
    ) {
        return Result.success(performanceService.searchPerformances(cityName, keyword, limit, offset));
    }

    @GetMapping("/{projectId}/detail")
    public Result<PerformanceDetailVO> getPerformanceDetail(
            @PathVariable Long projectId,
            @RequestParam String cityName,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long userId
    ) {
        return Result.success(performanceService.getPerformanceDetail(projectId, cityName, sessionId, userId));
    }

    @GetMapping("/{projectId}/sessions/{sessionId}/ticket-select")
    public Result<TicketSelectPageVO> getTicketSelect(
            @PathVariable Long projectId,
            @PathVariable Long sessionId
    ) {
        return Result.success(performanceService.getTicketSelect(projectId, sessionId));
    }
}
