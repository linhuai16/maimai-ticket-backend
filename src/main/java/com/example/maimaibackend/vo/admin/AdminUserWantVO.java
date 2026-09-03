package com.example.maimaibackend.vo.admin;

import java.time.LocalDateTime;

public class AdminUserWantVO {
    private Long wantId;
    private Long projectId;
    private String projectTitle;
    private String posterUrl;
    private LocalDateTime wantTime;

    public AdminUserWantVO() {
    }

    public Long getWantId() {
        return wantId;
    }

    public void setWantId(Long wantId) {
        this.wantId = wantId;
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

    public LocalDateTime getWantTime() {
        return wantTime;
    }

    public void setWantTime(LocalDateTime wantTime) {
        this.wantTime = wantTime;
    }
}