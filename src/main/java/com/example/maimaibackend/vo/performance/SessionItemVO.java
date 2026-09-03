package com.example.maimaibackend.vo.performance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SessionItemVO {
    private Long sessionId;
    private Long projectId;
    private String cityName;
    private String stationName;
    private Long venueId;
    private String venueName;
    private String venueAddress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String sessionStatus;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer limitPerOrder;
    private String stationDetailContent;
    private String deliveryType;
    private BigDecimal longitude;
    private BigDecimal latitude;

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
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public Integer getLimitPerOrder() { return limitPerOrder; }
    public void setLimitPerOrder(Integer limitPerOrder) { this.limitPerOrder = limitPerOrder; }
    public String getStationDetailContent() { return stationDetailContent; }
    public void setStationDetailContent(String stationDetailContent) { this.stationDetailContent = stationDetailContent; }
    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
}
