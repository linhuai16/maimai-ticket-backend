package com.example.maimaibackend.vo.performance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProjectDetailVO {
    private Long projectId;
    private String title;
    private Long categoryId;
    private String categoryName;
    private String posterUrl;
    private String detailContent;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer wantCount;
    private BigDecimal hotScore;
    private String projectStatus;
    private LocalDateTime publishTime;
    private Boolean wanted;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getDetailContent() { return detailContent; }
    public void setDetailContent(String detailContent) { this.detailContent = detailContent; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public Integer getWantCount() { return wantCount; }
    public void setWantCount(Integer wantCount) { this.wantCount = wantCount; }
    public BigDecimal getHotScore() { return hotScore; }
    public void setHotScore(BigDecimal hotScore) { this.hotScore = hotScore; }
    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public Boolean getWanted() { return wanted; }
    public void setWanted(Boolean wanted) { this.wanted = wanted; }
}
