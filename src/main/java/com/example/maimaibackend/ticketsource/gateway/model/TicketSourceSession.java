package com.example.maimaibackend.ticketsource.gateway.model;

import java.time.LocalDateTime;

public class TicketSourceSession {
    private String providerSessionId;
    private String providerProjectId;
    private String sessionName;
    private String cityName;
    private String venueName;
    private String venueAddress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private String saleStatus;
    private Integer limitPerOrder;
    private String deliveryType;
    private String dataVersion;
    private LocalDateTime updateTime;

    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getProviderProjectId() { return providerProjectId; }
    public void setProviderProjectId(String providerProjectId) { this.providerProjectId = providerProjectId; }
    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getSaleStartTime() { return saleStartTime; }
    public void setSaleStartTime(LocalDateTime saleStartTime) { this.saleStartTime = saleStartTime; }
    public LocalDateTime getSaleEndTime() { return saleEndTime; }
    public void setSaleEndTime(LocalDateTime saleEndTime) { this.saleEndTime = saleEndTime; }
    public String getSaleStatus() { return saleStatus; }
    public void setSaleStatus(String saleStatus) { this.saleStatus = saleStatus; }
    public Integer getLimitPerOrder() { return limitPerOrder; }
    public void setLimitPerOrder(Integer limitPerOrder) { this.limitPerOrder = limitPerOrder; }
    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }
    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
