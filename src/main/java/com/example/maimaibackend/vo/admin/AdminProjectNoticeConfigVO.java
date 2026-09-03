package com.example.maimaibackend.vo.admin;

import java.util.List;

public class AdminProjectNoticeConfigVO {
    private Long projectId;
    private String projectTitle;
    private Long categoryId;
    private String categoryName;
    private List<AdminNoticeVO> projectNotices;
    private List<AdminNoticeVO> providerNotices;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<AdminNoticeVO> getProjectNotices() { return projectNotices; }
    public void setProjectNotices(List<AdminNoticeVO> projectNotices) { this.projectNotices = projectNotices; }
    public List<AdminNoticeVO> getProviderNotices() { return providerNotices; }
    public void setProviderNotices(List<AdminNoticeVO> providerNotices) { this.providerNotices = providerNotices; }
}
