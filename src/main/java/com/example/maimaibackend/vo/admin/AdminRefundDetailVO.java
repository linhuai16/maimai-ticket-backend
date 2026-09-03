package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminRefundDetailVO {
    private Long refundId;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String userPhone;
    private String nickname;
    private Long projectId;
    private String projectTitle;
    private String posterUrl;
    private Long sessionId;
    private String cityName;
    private String stationName;
    private String venueName;
    private String orderStatus;
    private BigDecimal orderPayAmount;
    private String refundStatus;
    private String refundTypeSnapshot;
    private BigDecimal feeRateSnapshot;
    private BigDecimal refundAmount;
    private BigDecimal feeAmount;
    private String reason;
    private String failReason;
    private LocalDateTime applyTime;
    private LocalDateTime refundTime;
    private LocalDateTime updateTime;
    private List<AdminOrderTicketVO> tickets;

    public AdminRefundDetailVO() {
    }

    public Long getRefundId() {
        return refundId;
    }

    public void setRefundId(Long refundId) {
        this.refundId = refundId;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
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

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public BigDecimal getOrderPayAmount() {
        return orderPayAmount;
    }

    public void setOrderPayAmount(BigDecimal orderPayAmount) {
        this.orderPayAmount = orderPayAmount;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getRefundTypeSnapshot() {
        return refundTypeSnapshot;
    }

    public void setRefundTypeSnapshot(String refundTypeSnapshot) {
        this.refundTypeSnapshot = refundTypeSnapshot;
    }

    public BigDecimal getFeeRateSnapshot() {
        return feeRateSnapshot;
    }

    public void setFeeRateSnapshot(BigDecimal feeRateSnapshot) {
        this.feeRateSnapshot = feeRateSnapshot;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public List<AdminOrderTicketVO> getTickets() {
        return tickets;
    }

    public void setTickets(List<AdminOrderTicketVO> tickets) {
        this.tickets = tickets;
    }
}