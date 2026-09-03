package com.example.maimaibackend.dto.admin;

public class UpdateProjectStatusRequest {
    private String projectStatus;
    private Integer recommendFlag;

    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }
    public Integer getRecommendFlag() { return recommendFlag; }
    public void setRecommendFlag(Integer recommendFlag) { this.recommendFlag = recommendFlag; }
}
