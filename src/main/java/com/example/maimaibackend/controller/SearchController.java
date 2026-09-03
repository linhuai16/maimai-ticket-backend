package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.search.SaveSearchHistoryRequest;
import com.example.maimaibackend.service.SearchService;
import com.example.maimaibackend.vo.search.ClearSearchHistoryResponse;
import com.example.maimaibackend.vo.search.DeleteSearchHistoryResponse;
import com.example.maimaibackend.vo.search.SaveSearchHistoryResponse;
import com.example.maimaibackend.vo.search.SearchPageVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/history")
    public Result<SearchPageVO> getSearchHistory(@RequestParam Long userId) {
        return Result.success(searchService.getSearchHistory(userId));
    }

    @PostMapping("/history")
    public Result<SaveSearchHistoryResponse> saveSearchHistory(
            @RequestBody SaveSearchHistoryRequest request
    ) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        return Result.success(searchService.saveSearchHistory(request.getUserId(), request.getKeyword()));
    }

    @DeleteMapping("/history/{historyId}")
    public Result<DeleteSearchHistoryResponse> deleteSearchHistory(
            @PathVariable Long historyId,
            @RequestParam Long userId
    ) {
        return Result.success(searchService.deleteSearchHistory(userId, historyId));
    }

    @DeleteMapping("/history")
    public Result<ClearSearchHistoryResponse> clearSearchHistory(@RequestParam Long userId) {
        return Result.success(searchService.clearSearchHistory(userId));
    }
}
