package com.example.maimaibackend.vo.admin;

import java.time.LocalDateTime;

public class AdminBannerVO {
    private Long bannerId;
    private String bannerTitle;
    private String imageUrl;
    private Long targetProjectId;
    private String targetProjectTitle;
    private String targetProjectStatus;
    private Long targetSessionId;
    private String targetSessionCityName;
    private String targetSessionStationName;
    private LocalDateTime targetSessionStartTime;
    private String targetSessionStatus;
    private String enableStatus;
    private Integer sortOrder;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String effectiveStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getBannerId() { return bannerId; }
    public void setBannerId(Long bannerId) { this.bannerId = bannerId; }
    public String getBannerTitle() { return bannerTitle; }
    public void setBannerTitle(String bannerTitle) { this.bannerTitle = bannerTitle; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getTargetProjectId() { return targetProjectId; }
    public void setTargetProjectId(Long targetProjectId) { this.targetProjectId = targetProjectId; }
    public String getTargetProjectTitle() { return targetProjectTitle; }
    public void setTargetProjectTitle(String targetProjectTitle) { this.targetProjectTitle = targetProjectTitle; }
    public String getTargetProjectStatus() { return targetProjectStatus; }
    public void setTargetProjectStatus(String targetProjectStatus) { this.targetProjectStatus = targetProjectStatus; }
    public Long getTargetSessionId() { return targetSessionId; }
    public void setTargetSessionId(Long targetSessionId) { this.targetSessionId = targetSessionId; }
    public String getTargetSessionCityName() { return targetSessionCityName; }
    public void setTargetSessionCityName(String targetSessionCityName) { this.targetSessionCityName = targetSessionCityName; }
    public String getTargetSessionStationName() { return targetSessionStationName; }
    public void setTargetSessionStationName(String targetSessionStationName) { this.targetSessionStationName = targetSessionStationName; }
    public LocalDateTime getTargetSessionStartTime() { return targetSessionStartTime; }
    public void setTargetSessionStartTime(LocalDateTime targetSessionStartTime) { this.targetSessionStartTime = targetSessionStartTime; }
    public String getTargetSessionStatus() { return targetSessionStatus; }
    public void setTargetSessionStatus(String targetSessionStatus) { this.targetSessionStatus = targetSessionStatus; }
    public String getEnableStatus() { return enableStatus; }
    public void setEnableStatus(String enableStatus) { this.enableStatus = enableStatus; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getEffectiveStatus() { return effectiveStatus; }
    public void setEffectiveStatus(String effectiveStatus) { this.effectiveStatus = effectiveStatus; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
