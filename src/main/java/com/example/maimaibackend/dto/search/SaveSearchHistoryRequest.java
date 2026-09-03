package com.example.maimaibackend.dto.search;

public class SaveSearchHistoryRequest {

    private Long userId;
    private String keyword;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
