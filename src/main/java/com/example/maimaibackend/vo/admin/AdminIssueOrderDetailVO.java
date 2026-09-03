package com.example.maimaibackend.vo.admin;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public class AdminIssueOrderDetailVO {
    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private BigDecimal payAmount;
    private Long projectId;
    private String projectTitle;
    private Long sessionId;
    private String cityName;
    private String stationName;
    private String venueName;
    private LocalDateTime startTime;
    private LocalDateTime ticketIssuedTime;
    private Integer totalTicketCount;
    private Integer generatingCount;
    private Integer errorCount;
    private Integer unusedCount;
    private Integer checkedCount;
    private Integer expiredCount;
    private String issueStatus;
    private List<AdminTicketItemVO> tickets;

    public AdminIssueOrderDetailVO() {
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

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
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

    public LocalDateTime getTicketIssuedTime() {
        return ticketIssuedTime;
    }

    public void setTicketIssuedTime(LocalDateTime ticketIssuedTime) {
        this.ticketIssuedTime = ticketIssuedTime;
    }

    public Integer getTotalTicketCount() {
        return totalTicketCount;
    }

    public void setTotalTicketCount(Integer totalTicketCount) {
        this.totalTicketCount = totalTicketCount;
    }

    public Integer getGeneratingCount() {
        return generatingCount;
    }

    public void setGeneratingCount(Integer generatingCount) {
        this.generatingCount = generatingCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Integer getUnusedCount() {
        return unusedCount;
    }

    public void setUnusedCount(Integer unusedCount) {
        this.unusedCount = unusedCount;
    }

    public Integer getCheckedCount() {
        return checkedCount;
    }

    public void setCheckedCount(Integer checkedCount) {
        this.checkedCount = checkedCount;
    }

    public Integer getExpiredCount() {
        return expiredCount;
    }

    public void setExpiredCount(Integer expiredCount) {
        this.expiredCount = expiredCount;
    }

    public String getIssueStatus() {
        return issueStatus;
    }

    public void setIssueStatus(String issueStatus) {
        this.issueStatus = issueStatus;
    }

    public List<AdminTicketItemVO> getTickets() {
        return tickets;
    }

    public void setTickets(List<AdminTicketItemVO> tickets) {
        this.tickets = tickets;
    }
}
