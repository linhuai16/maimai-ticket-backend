package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminOrderDetailVO {
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
    private String venueAddress;
    private String orderStatus;
    private String deliveryType;
    private BigDecimal ticketAmount;
    private BigDecimal serviceFeeAmount;
    private BigDecimal deliveryFeeAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private String payMethod;
    private LocalDateTime payExpireTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private LocalDateTime ticketIssuedTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<AdminOrderItemDetailVO> items;
    private List<AdminOrderAudienceVO> audiences;
    private AdminOrderAddressVO address;
    private List<AdminOrderTicketVO> tickets;
    private List<AdminRefundItemVO> refunds;

    public AdminOrderDetailVO() {
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

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
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

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public LocalDateTime getPayExpireTime() {
        return payExpireTime;
    }

    public void setPayExpireTime(LocalDateTime payExpireTime) {
        this.payExpireTime = payExpireTime;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public LocalDateTime getTicketIssuedTime() {
        return ticketIssuedTime;
    }

    public void setTicketIssuedTime(LocalDateTime ticketIssuedTime) {
        this.ticketIssuedTime = ticketIssuedTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
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

    public List<AdminOrderItemDetailVO> getItems() {
        return items;
    }

    public void setItems(List<AdminOrderItemDetailVO> items) {
        this.items = items;
    }

    public List<AdminOrderAudienceVO> getAudiences() {
        return audiences;
    }

    public void setAudiences(List<AdminOrderAudienceVO> audiences) {
        this.audiences = audiences;
    }

    public AdminOrderAddressVO getAddress() {
        return address;
    }

    public void setAddress(AdminOrderAddressVO address) {
        this.address = address;
    }

    public List<AdminOrderTicketVO> getTickets() {
        return tickets;
    }

    public void setTickets(List<AdminOrderTicketVO> tickets) {
        this.tickets = tickets;
    }

    public List<AdminRefundItemVO> getRefunds() {
        return refunds;
    }

    public void setRefunds(List<AdminRefundItemVO> refunds) {
        this.refunds = refunds;
    }
}