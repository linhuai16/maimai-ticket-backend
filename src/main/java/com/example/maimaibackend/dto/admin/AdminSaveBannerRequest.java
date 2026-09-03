package com.example.maimaibackend.dto.admin;

import java.time.LocalDateTime;

public class AdminSaveBannerRequest {
    private Long bannerId;
    private String bannerTitle;
    private String imageUrl;
    private Long targetProjectId;
    private Long targetSessionId;
    private String enableStatus;
    private Integer sortOrder;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getBannerId() { return bannerId; }
    public void setBannerId(Long bannerId) { this.bannerId = bannerId; }
    public String getBannerTitle() { return bannerTitle; }
    public void setBannerTitle(String bannerTitle) { this.bannerTitle = bannerTitle; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getTargetProjectId() { return targetProjectId; }
    public void setTargetProjectId(Long targetProjectId) { this.targetProjectId = targetProjectId; }
    public Long getTargetSessionId() { return targetSessionId; }
    public void setTargetSessionId(Long targetSessionId) { this.targetSessionId = targetSessionId; }
    public String getEnableStatus() { return enableStatus; }
    public void setEnableStatus(String enableStatus) { this.enableStatus = enableStatus; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
