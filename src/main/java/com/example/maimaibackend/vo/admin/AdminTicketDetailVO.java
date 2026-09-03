package com.example.maimaibackend.vo.admin;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class AdminTicketDetailVO {
    private Long ticketId;
    private String ticketNo;
    private String ticketStatus;
    private String fulfillmentMode;
    private Long sourceProviderId;
    private String sourceProviderCode;
    private String providerOrderId;
    private String providerTicketId;
    private String credentialType;
    private String credentialPayload;
    private String credentialVersion;
    private String qrCodeValue;
    private String seatInfo;
    private String seatZone;
    private String seatRow;
    private String seatNumber;
    private String entranceInfo;
    private LocalDateTime generateTime;
    private LocalDateTime providerIssueTime;
    private LocalDateTime lastSourceSyncTime;
    private LocalDateTime checkTime;
    private LocalDateTime expireTime;
    private String abnormalReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private String deliveryType;
    private BigDecimal payAmount;
    private Long userId;
    private String userPhone;
    private String nickname;
    private Long projectId;
    private String projectTitle;
    private String posterUrl;
    private Long sessionId;
    private String cityName;
    private String stationName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long venueId;
    private String venueName;
    private String venueAddress;
    private Long orderItemId;
    private Long skuId;
    private String skuName;
    private BigDecimal unitPrice;
    private Long orderAudienceId;
    private String audienceName;
    private String certificateType;
    private String certificateNo;
    private String audiencePhone;

    public AdminTicketDetailVO() {
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public String getQrCodeValue() {
        return qrCodeValue;
    }

    public void setQrCodeValue(String qrCodeValue) {
        this.qrCodeValue = qrCodeValue;
    }

    public String getSeatInfo() {
        return seatInfo;
    }

    public void setSeatInfo(String seatInfo) {
        this.seatInfo = seatInfo;
    }

    public LocalDateTime getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(LocalDateTime generateTime) {
        this.generateTime = generateTime;
    }

    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public String getAbnormalReason() {
        return abnormalReason;
    }

    public void setAbnormalReason(String abnormalReason) {
        this.abnormalReason = abnormalReason;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
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

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getOrderAudienceId() {
        return orderAudienceId;
    }

    public void setOrderAudienceId(Long orderAudienceId) {
        this.orderAudienceId = orderAudienceId;
    }

    public String getAudienceName() {
        return audienceName;
    }

    public void setAudienceName(String audienceName) {
        this.audienceName = audienceName;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public String getAudiencePhone() {
        return audiencePhone;
    }

    public void setAudiencePhone(String audiencePhone) {
        this.audiencePhone = audiencePhone;
    }
    public Long getSourceProviderId() { return sourceProviderId; }
    public void setSourceProviderId(Long sourceProviderId) { this.sourceProviderId = sourceProviderId; }
    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getProviderTicketId() { return providerTicketId; }
    public void setProviderTicketId(String providerTicketId) { this.providerTicketId = providerTicketId; }
    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }
    public String getCredentialPayload() { return credentialPayload; }
    public void setCredentialPayload(String credentialPayload) { this.credentialPayload = credentialPayload; }
    public String getCredentialVersion() { return credentialVersion; }
    public void setCredentialVersion(String credentialVersion) { this.credentialVersion = credentialVersion; }
    public String getSeatZone() { return seatZone; }
    public void setSeatZone(String seatZone) { this.seatZone = seatZone; }
    public String getSeatRow() { return seatRow; }
    public void setSeatRow(String seatRow) { this.seatRow = seatRow; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getEntranceInfo() { return entranceInfo; }
    public void setEntranceInfo(String entranceInfo) { this.entranceInfo = entranceInfo; }
    public LocalDateTime getProviderIssueTime() { return providerIssueTime; }
    public void setProviderIssueTime(LocalDateTime providerIssueTime) { this.providerIssueTime = providerIssueTime; }
    public LocalDateTime getLastSourceSyncTime() { return lastSourceSyncTime; }
    public void setLastSourceSyncTime(LocalDateTime lastSourceSyncTime) { this.lastSourceSyncTime = lastSourceSyncTime; }

    public String getFulfillmentMode() { return fulfillmentMode; }
    public void setFulfillmentMode(String fulfillmentMode) { this.fulfillmentMode = fulfillmentMode; }
    public String getSourceProviderCode() { return sourceProviderCode; }
    public void setSourceProviderCode(String sourceProviderCode) { this.sourceProviderCode = sourceProviderCode; }

}
