package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.SearchMapper;
import com.example.maimaibackend.vo.search.ClearSearchHistoryResponse;
import com.example.maimaibackend.vo.search.DeleteSearchHistoryResponse;
import com.example.maimaibackend.vo.search.SaveSearchHistoryResponse;
import com.example.maimaibackend.vo.search.SearchPageVO;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private static final int MAX_KEYWORD_LENGTH = 100;

    private final SearchMapper searchMapper;

    public SearchService(SearchMapper searchMapper) {
        this.searchMapper = searchMapper;
    }

    public SearchPageVO getSearchHistory(Long userId) {
        checkUserId(userId);
        SearchPageVO vo = new SearchPageVO();
        vo.setHistories(searchMapper.selectSearchHistory(userId));
        return vo;
    }

    public SaveSearchHistoryResponse saveSearchHistory(Long userId, String keyword) {
        checkUserId(userId);
        String realKeyword = normalizeKeyword(keyword);
        searchMapper.upsertSearchHistory(userId, realKeyword);
        return new SaveSearchHistoryResponse(true);
    }

    public DeleteSearchHistoryResponse deleteSearchHistory(Long userId, Long historyId) {
        checkUserId(userId);
        if (historyId == null) {
            throw new BusinessException("搜索历史ID不能为空");
        }
        int rows = searchMapper.deleteSearchHistory(userId, historyId);
        return new DeleteSearchHistoryResponse(rows > 0);
    }

    public ClearSearchHistoryResponse clearSearchHistory(Long userId) {
        checkUserId(userId);
        searchMapper.clearSearchHistory(userId);
        return new ClearSearchHistoryResponse(true);
    }

    private void checkUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException("搜索关键词不能为空");
        }
        String realKeyword = keyword.trim();
        if (realKeyword.length() > MAX_KEYWORD_LENGTH) {
            throw new BusinessException("搜索关键词长度不能超过100个字符");
        }
        return realKeyword;
    }
}
