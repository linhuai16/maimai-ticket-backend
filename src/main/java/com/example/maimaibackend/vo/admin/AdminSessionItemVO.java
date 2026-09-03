package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminSessionItemVO {
    private Long sessionId;
    private Long projectId;
    private String cityName;
    private String stationName;
    private Long venueId;
    private String venueName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private Integer issueOffsetHours;
    private String sessionStatus;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer limitPerOrder;
    private String deliveryType;
    private Boolean sourceManaged;
    private String providerCode;
    private String providerSessionId;
    private String stationDetailContent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer skuCount;
    private Integer stockAvailableCount;
    private Integer soldCount;

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
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public Integer getLimitPerOrder() { return limitPerOrder; }
    public void setLimitPerOrder(Integer limitPerOrder) { this.limitPerOrder = limitPerOrder; }
    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }
    public Boolean getSourceManaged() { return sourceManaged; }
    public void setSourceManaged(Boolean sourceManaged) { this.sourceManaged = sourceManaged; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderSessionId() { return providerSessionId; }
    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public String getStationDetailContent() { return stationDetailContent; }
    public void setStationDetailContent(String stationDetailContent) { this.stationDetailContent = stationDetailContent; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getSkuCount() { return skuCount; }
    public void setSkuCount(Integer skuCount) { this.skuCount = skuCount; }
    public Integer getStockAvailableCount() { return stockAvailableCount; }
    public void setStockAvailableCount(Integer stockAvailableCount) { this.stockAvailableCount = stockAvailableCount; }
    public Integer getSoldCount() { return soldCount; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }
}
