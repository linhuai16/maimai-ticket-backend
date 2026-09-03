package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminProjectDetailVO {
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
    private Integer recommendFlag;
    private Boolean sourceManaged;
    private String providerCode;
    private String providerProjectId;
    private String providerProjectName;
    private String providerSubtitle;
    private String providerIntroduction;
    private String providerPosterUrl;
    private String providerDetailContent;
    private String providerSaleStatus;
    private String providerStatusText;
    private String mappingStatus;
    private String lastSyncStatus;
    private LocalDateTime lastSyncTime;
    private LocalDateTime sourceUpdatedTime;
    private Boolean autoPublishEnabled;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> serviceTags;
    private List<String> noticeTitles;

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
    public Integer getRecommendFlag() { return recommendFlag; }
    public void setRecommendFlag(Integer recommendFlag) { this.recommendFlag = recommendFlag; }
    public Boolean getSourceManaged() { return sourceManaged; }
    public void setSourceManaged(Boolean sourceManaged) { this.sourceManaged = sourceManaged; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getProviderProjectName() { return providerProjectName; }
    public void setProviderProjectName(String providerProjectName) { this.providerProjectName = providerProjectName; }
    public String getProviderSubtitle() { return providerSubtitle; }
    public void setProviderSubtitle(String providerSubtitle) { this.providerSubtitle = providerSubtitle; }
    public String getProviderIntroduction() { return providerIntroduction; }
    public void setProviderIntroduction(String providerIntroduction) { this.providerIntroduction = providerIntroduction; }
    public String getProviderPosterUrl() { return providerPosterUrl; }
    public void setProviderPosterUrl(String providerPosterUrl) { this.providerPosterUrl = providerPosterUrl; }
    public String getProviderDetailContent() { return providerDetailContent; }
    public void setProviderDetailContent(String providerDetailContent) { this.providerDetailContent = providerDetailContent; }
    public String getProviderSaleStatus() { return providerSaleStatus; }
    public void setProviderSaleStatus(String providerSaleStatus) { this.providerSaleStatus = providerSaleStatus; }
    public String getProviderStatusText() { return providerStatusText; }
    public void setProviderStatusText(String providerStatusText) { this.providerStatusText = providerStatusText; }
    public String getMappingStatus() { return mappingStatus; }
    public void setMappingStatus(String mappingStatus) { this.mappingStatus = mappingStatus; }
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    public LocalDateTime getSourceUpdatedTime() { return sourceUpdatedTime; }
    public void setSourceUpdatedTime(LocalDateTime sourceUpdatedTime) { this.sourceUpdatedTime = sourceUpdatedTime; }
    public Boolean getAutoPublishEnabled() { return autoPublishEnabled; }
    public void setAutoPublishEnabled(Boolean autoPublishEnabled) { this.autoPublishEnabled = autoPublishEnabled; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<String> getServiceTags() { return serviceTags; }
    public void setServiceTags(List<String> serviceTags) { this.serviceTags = serviceTags; }
    public List<String> getNoticeTitles() { return noticeTitles; }
    public void setNoticeTitles(List<String> noticeTitles) { this.noticeTitles = noticeTitles; }
}
