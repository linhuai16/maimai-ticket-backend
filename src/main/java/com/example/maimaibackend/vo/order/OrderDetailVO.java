package com.example.maimaibackend.vo.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailVO {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private String orderStatus;
    private Long projectId;
    private Long sessionId;
    private String title;
    private String posterUrl;
    private String cityName;
    private String stationName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String venueName;
    private String venueAddress;
    private String deliveryType;
    private String deliveryMode;
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
    private String ticketActionType;
    private Long defaultTicketId;
    private List<OrderDetailItemVO> items;
    private List<OrderAudienceVO> audiences;
    private OrderAddressVO address;
    private List<OrderTicketVO> tickets;
    private OrderRefundRecordVO refundRecord;

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

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

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

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
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

    public String getTicketActionType() {
        return ticketActionType;
    }

    public void setTicketActionType(String ticketActionType) {
        this.ticketActionType = ticketActionType;
    }

    public Long getDefaultTicketId() {
        return defaultTicketId;
    }

    public void setDefaultTicketId(Long defaultTicketId) {
        this.defaultTicketId = defaultTicketId;
    }

    public List<OrderDetailItemVO> getItems() {
        return items;
    }

    public void setItems(List<OrderDetailItemVO> items) {
        this.items = items;
    }

    public List<OrderAudienceVO> getAudiences() {
        return audiences;
    }

    public void setAudiences(List<OrderAudienceVO> audiences) {
        this.audiences = audiences;
    }

    public OrderAddressVO getAddress() {
        return address;
    }

    public void setAddress(OrderAddressVO address) {
        this.address = address;
    }

    public List<OrderTicketVO> getTickets() {
        return tickets;
    }

    public void setTickets(List<OrderTicketVO> tickets) {
        this.tickets = tickets;
    }

    public OrderRefundRecordVO getRefundRecord() {
        return refundRecord;
    }

    public void setRefundRecord(OrderRefundRecordVO refundRecord) {
        this.refundRecord = refundRecord;
    }
}
