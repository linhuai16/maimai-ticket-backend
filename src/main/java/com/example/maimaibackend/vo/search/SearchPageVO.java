package com.example.maimaibackend.vo.search;

import java.util.List;

public class SearchPageVO {

    private List<SearchHistoryItemVO> histories;

    public List<SearchHistoryItemVO> getHistories() {
        return histories;
    }

    public void setHistories(List<SearchHistoryItemVO> histories) {
        this.histories = histories;
    }
}
