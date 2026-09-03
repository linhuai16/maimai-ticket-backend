package com.example.maimaibackend.vo.admin;

import java.time.LocalDateTime;

public class AdminTicketItemVO {
    private Long ticketId;
    private String ticketNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String userPhone;
    private String nickname;
    private Long projectId;
    private String projectTitle;
    private Long sessionId;
    private String cityName;
    private String stationName;
    private String venueName;
    private LocalDateTime startTime;
    private Long orderItemId;
    private Long skuId;
    private String skuName;
    private Long orderAudienceId;
    private String audienceName;
    private String certificateType;
    private String certificateNo;
    private String ticketStatus;
    private String fulfillmentMode;
    private Long sourceProviderId;
    private String sourceProviderCode;
    private String providerTicketId;
    private String credentialType;
    private String seatInfo;
    private String seatZone;
    private String seatRow;
    private String seatNumber;
    private LocalDateTime generateTime;
    private LocalDateTime providerIssueTime;
    private LocalDateTime checkTime;
    private LocalDateTime expireTime;
    private String abnormalReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public AdminTicketItemVO() {
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

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
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

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
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
    public Long getSourceProviderId() { return sourceProviderId; }
    public void setSourceProviderId(Long sourceProviderId) { this.sourceProviderId = sourceProviderId; }
    public String getProviderTicketId() { return providerTicketId; }
    public void setProviderTicketId(String providerTicketId) { this.providerTicketId = providerTicketId; }
    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }
    public String getSeatZone() { return seatZone; }
    public void setSeatZone(String seatZone) { this.seatZone = seatZone; }
    public String getSeatRow() { return seatRow; }
    public void setSeatRow(String seatRow) { this.seatRow = seatRow; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public LocalDateTime getProviderIssueTime() { return providerIssueTime; }
    public void setProviderIssueTime(LocalDateTime providerIssueTime) { this.providerIssueTime = providerIssueTime; }

    public String getFulfillmentMode() { return fulfillmentMode; }
    public void setFulfillmentMode(String fulfillmentMode) { this.fulfillmentMode = fulfillmentMode; }
    public String getSourceProviderCode() { return sourceProviderCode; }
    public void setSourceProviderCode(String sourceProviderCode) { this.sourceProviderCode = sourceProviderCode; }

}
