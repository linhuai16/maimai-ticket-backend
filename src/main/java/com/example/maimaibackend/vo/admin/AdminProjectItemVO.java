package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminProjectItemVO {
    private Long projectId;
    private String title;
    private Long categoryId;
    private String categoryName;
    private String posterUrl;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer wantCount;
    private BigDecimal hotScore;
    private String projectStatus;
    private Integer recommendFlag;
    private Boolean sourceManaged;
    private String providerCode;
    private String providerProjectId;
    private LocalDateTime publishTime;
    private LocalDateTime updateTime;
    private Integer sessionCount;
    private Integer onSaleSessionCount;
    private Integer stockAvailableCount;
    private Integer soldCount;

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
    public Integer getRecommendFlag() { return recommendFlag; }
    public void setRecommendFlag(Integer recommendFlag) { this.recommendFlag = recommendFlag; }
    public Boolean getSourceManaged() { return sourceManaged; }
    public void setSourceManaged(Boolean sourceManaged) { this.sourceManaged = sourceManaged; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getSessionCount() { return sessionCount; }
    public void setSessionCount(Integer sessionCount) { this.sessionCount = sessionCount; }
    public Integer getOnSaleSessionCount() { return onSaleSessionCount; }
    public void setOnSaleSessionCount(Integer onSaleSessionCount) { this.onSaleSessionCount = onSaleSessionCount; }
    public Integer getStockAvailableCount() { return stockAvailableCount; }
    public void setStockAvailableCount(Integer stockAvailableCount) { this.stockAvailableCount = stockAvailableCount; }
    public Integer getSoldCount() { return soldCount; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }
}
