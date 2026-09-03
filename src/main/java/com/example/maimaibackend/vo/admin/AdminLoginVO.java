package com.example.maimaibackend.vo.admin;

import java.time.LocalDateTime;

public class AdminLoginVO {
    private Long adminId;
    private String username;
    private String nickname;
    private LocalDateTime loginTime;

    public AdminLoginVO() {
    }

    public AdminLoginVO(Long adminId, String username, String nickname, LocalDateTime loginTime) {
        this.adminId = adminId;
        this.username = username;
        this.nickname = nickname;
        this.loginTime = loginTime;
    }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
}
