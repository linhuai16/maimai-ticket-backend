package com.example.maimaibackend.vo.want;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WantPerformanceItemVO {
    private Long wantId;
    private LocalDateTime wantTime;
    private Long projectId;
    private Long sessionId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String posterUrl;
    private String cityName;
    private String stationName;
    private String venueName;
    private LocalDateTime startTime;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer wantCount;

    public Long getWantId() { return wantId; }
    public void setWantId(Long wantId) { this.wantId = wantId; }
    public LocalDateTime getWantTime() { return wantTime; }
    public void setWantTime(LocalDateTime wantTime) { this.wantTime = wantTime; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public Integer getWantCount() { return wantCount; }
    public void setWantCount(Integer wantCount) { this.wantCount = wantCount; }
}
