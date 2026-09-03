package com.example.maimaibackend.vo.search;

import java.time.LocalDateTime;

public class SearchHistoryItemVO {

    private Long historyId;
    private String keyword;
    private LocalDateTime lastSearchTime;

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public LocalDateTime getLastSearchTime() {
        return lastSearchTime;
    }

    public void setLastSearchTime(LocalDateTime lastSearchTime) {
        this.lastSearchTime = lastSearchTime;
    }
}
