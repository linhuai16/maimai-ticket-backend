package com.example.maimaibackend.vo.performance;

import java.util.List;

public class SearchResultPageVO {
    private String keyword;
    private List<PerformanceCardVO> performances;
    private Integer total;
    private Integer limit;
    private Integer offset;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public List<PerformanceCardVO> getPerformances() { return performances; }
    public void setPerformances(List<PerformanceCardVO> performances) { this.performances = performances; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
}
