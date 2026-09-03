package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminUserItemVO {
    private Long userId;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private String accountStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer orderCount;
    private Integer waitPayOrderCount;
    private Integer paidOrderCount;
    private BigDecimal totalPayAmount;
    private Integer audienceCount;
    private Integer addressCount;
    private Integer wantCount;

    public AdminUserItemVO() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
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

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public Integer getWaitPayOrderCount() {
        return waitPayOrderCount;
    }

    public void setWaitPayOrderCount(Integer waitPayOrderCount) {
        this.waitPayOrderCount = waitPayOrderCount;
    }

    public Integer getPaidOrderCount() {
        return paidOrderCount;
    }

    public void setPaidOrderCount(Integer paidOrderCount) {
        this.paidOrderCount = paidOrderCount;
    }

    public BigDecimal getTotalPayAmount() {
        return totalPayAmount;
    }

    public void setTotalPayAmount(BigDecimal totalPayAmount) {
        this.totalPayAmount = totalPayAmount;
    }

    public Integer getAudienceCount() {
        return audienceCount;
    }

    public void setAudienceCount(Integer audienceCount) {
        this.audienceCount = audienceCount;
    }

    public Integer getAddressCount() {
        return addressCount;
    }

    public void setAddressCount(Integer addressCount) {
        this.addressCount = addressCount;
    }

    public Integer getWantCount() {
        return wantCount;
    }

    public void setWantCount(Integer wantCount) {
        this.wantCount = wantCount;
    }
}