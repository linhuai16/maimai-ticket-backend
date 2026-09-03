package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminUserDetailVO {
    private Long userId;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private String accountStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer orderCount;
    private Integer paidOrderCount;
    private BigDecimal totalPayAmount;
    private List<AdminUserWantVO> wants;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    public Integer getPaidOrderCount() { return paidOrderCount; }
    public void setPaidOrderCount(Integer paidOrderCount) { this.paidOrderCount = paidOrderCount; }
    public BigDecimal getTotalPayAmount() { return totalPayAmount; }
    public void setTotalPayAmount(BigDecimal totalPayAmount) { this.totalPayAmount = totalPayAmount; }
    public List<AdminUserWantVO> getWants() { return wants; }
    public void setWants(List<AdminUserWantVO> wants) { this.wants = wants; }
}
