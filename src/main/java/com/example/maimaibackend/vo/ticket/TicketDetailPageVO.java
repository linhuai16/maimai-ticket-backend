package com.example.maimaibackend.vo.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TicketDetailPageVO {

    private Long orderId;
    private String orderNo;
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
    private BigDecimal venueLongitude;
    private BigDecimal venueLatitude;
    private String deliveryType;
    private String deliveryMode;
    private Integer totalQuantity;
    private BigDecimal payAmount;
    private Long defaultTicketId;
    private List<ElectronicTicketVO> tickets;
    private String customerServicePhone;

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

    public BigDecimal getVenueLongitude() { return venueLongitude; }

    public void setVenueLongitude(BigDecimal venueLongitude) { this.venueLongitude = venueLongitude; }

    public BigDecimal getVenueLatitude() { return venueLatitude; }

    public void setVenueLatitude(BigDecimal venueLatitude) { this.venueLatitude = venueLatitude; }

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

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public Long getDefaultTicketId() {
        return defaultTicketId;
    }

    public void setDefaultTicketId(Long defaultTicketId) {
        this.defaultTicketId = defaultTicketId;
    }

    public List<ElectronicTicketVO> getTickets() {
        return tickets;
    }

    public void setTickets(List<ElectronicTicketVO> tickets) {
        this.tickets = tickets;
    }

    public String getCustomerServicePhone() { return customerServicePhone; }

    public void setCustomerServicePhone(String customerServicePhone) { this.customerServicePhone = customerServicePhone; }

}
