package com.example.maimaibackend.vo.order;

import com.example.maimaibackend.vo.performance.ServiceTagVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SubmitOrderPageVO {

    private Long projectId;
    private Long sessionId;
    private Long skuId;
    private String title;
    private String posterUrl;
    private String cityName;
    private String stationName;
    private LocalDateTime startTime;
    private String venueName;
    private String venueAddress;
    private String sessionStatus;
    private String skuName;
    private String skuStatus;
    private BigDecimal unitPrice;
    private Integer quantity;
    private Integer requiredAudienceCount;
    private String deliveryType;
    private Integer limitPerOrder;
    private Integer stockAvailable;
    private BigDecimal ticketAmount;
    private BigDecimal serviceFeeAmount;
    private BigDecimal deliveryFeeAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer payExpireMinutes;
    private List<ServiceTagVO> serviceTags;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getSkuStatus() {
        return skuStatus;
    }

    public void setSkuStatus(String skuStatus) {
        this.skuStatus = skuStatus;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getRequiredAudienceCount() {
        return requiredAudienceCount;
    }

    public void setRequiredAudienceCount(Integer requiredAudienceCount) {
        this.requiredAudienceCount = requiredAudienceCount;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public Integer getLimitPerOrder() {
        return limitPerOrder;
    }

    public void setLimitPerOrder(Integer limitPerOrder) {
        this.limitPerOrder = limitPerOrder;
    }

    public Integer getStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(Integer stockAvailable) {
        this.stockAvailable = stockAvailable;
    }

    public BigDecimal getTicketAmount() {
        return ticketAmount;
    }

    public void setTicketAmount(BigDecimal ticketAmount) {
        this.ticketAmount = ticketAmount;
    }

    public BigDecimal getServiceFeeAmount() {
        return serviceFeeAmount;
    }

    public void setServiceFeeAmount(BigDecimal serviceFeeAmount) {
        this.serviceFeeAmount = serviceFeeAmount;
    }

    public BigDecimal getDeliveryFeeAmount() {
        return deliveryFeeAmount;
    }

    public void setDeliveryFeeAmount(BigDecimal deliveryFeeAmount) {
        this.deliveryFeeAmount = deliveryFeeAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public Integer getPayExpireMinutes() {
        return payExpireMinutes;
    }

    public void setPayExpireMinutes(Integer payExpireMinutes) {
        this.payExpireMinutes = payExpireMinutes;
    }

    public List<ServiceTagVO> getServiceTags() {
        return serviceTags;
    }

    public void setServiceTags(List<ServiceTagVO> serviceTags) {
        this.serviceTags = serviceTags;
    }
}
