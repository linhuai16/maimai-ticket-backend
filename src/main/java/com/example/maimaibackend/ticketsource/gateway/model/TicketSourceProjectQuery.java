package com.example.maimaibackend.ticketsource.gateway.model;

public class TicketSourceProjectQuery {
    private String keyword;
    private String cityName;
    private int pageNo = 1;
    private int pageSize = 20;

    public TicketSourceProjectQuery normalized() {
        TicketSourceProjectQuery query = new TicketSourceProjectQuery();
        query.keyword = normalizeText(keyword);
        query.cityName = normalizeText(cityName);
        query.pageNo = Math.max(1, pageNo);
        query.pageSize = Math.min(100, Math.max(1, pageSize));
        return query;
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public int offset() {
        return (pageNo - 1) * pageSize;
    }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public int getPageNo() { return pageNo; }
    public void setPageNo(int pageNo) { this.pageNo = pageNo; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
