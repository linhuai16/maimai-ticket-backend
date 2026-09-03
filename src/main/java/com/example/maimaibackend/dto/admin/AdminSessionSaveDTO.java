package com.example.maimaibackend.dto.admin;

import java.time.LocalDateTime;

public class AdminSessionSaveDTO {
    private Long sessionId;
    private Long projectId;
    private String cityName;
    private String stationName;
    private Long venueId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private Integer issueOffsetHours;
    private String sessionStatus;
    private Integer limitPerOrder;
    private String stationDetailContent;
    private String deliveryType;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getSaleStartTime() { return saleStartTime; }
    public void setSaleStartTime(LocalDateTime saleStartTime) { this.saleStartTime = saleStartTime; }
    public LocalDateTime getSaleEndTime() { return saleEndTime; }
    public void setSaleEndTime(LocalDateTime saleEndTime) { this.saleEndTime = saleEndTime; }
    public Integer getIssueOffsetHours() { return issueOffsetHours; }
    public void setIssueOffsetHours(Integer issueOffsetHours) { this.issueOffsetHours = issueOffsetHours; }
    public String getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }
    public Integer getLimitPerOrder() { return limitPerOrder; }
    public void setLimitPerOrder(Integer limitPerOrder) { this.limitPerOrder = limitPerOrder; }
    public String getStationDetailContent() { return stationDetailContent; }
    public void setStationDetailContent(String stationDetailContent) { this.stationDetailContent = stationDetailContent; }
    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }
}
