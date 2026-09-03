package com.example.maimaibackend.vo.admin;

import java.util.List;

public class AdminProjectServiceTagConfigVO {
    private Long projectId;
    private String projectTitle;
    private List<AdminServiceTagVO> manualTags;
    private List<AdminServiceTagVO> providerTags;
    private List<AdminServiceTagVO> automaticRefundTags;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public List<AdminServiceTagVO> getManualTags() { return manualTags; }
    public void setManualTags(List<AdminServiceTagVO> manualTags) { this.manualTags = manualTags; }
    public List<AdminServiceTagVO> getProviderTags() { return providerTags; }
    public void setProviderTags(List<AdminServiceTagVO> providerTags) { this.providerTags = providerTags; }
    public List<AdminServiceTagVO> getAutomaticRefundTags() { return automaticRefundTags; }
    public void setAutomaticRefundTags(List<AdminServiceTagVO> automaticRefundTags) { this.automaticRefundTags = automaticRefundTags; }
}
