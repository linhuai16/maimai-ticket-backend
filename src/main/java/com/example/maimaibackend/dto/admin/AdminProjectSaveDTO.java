package com.example.maimaibackend.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminProjectSaveDTO {
    private Long projectId;
    private String title;
    private Long categoryId;
    private String posterUrl;
    private String detailContent;
    private BigDecimal hotScore;
    private String projectStatus;
    private Integer recommendFlag;
    private LocalDateTime publishTime;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getDetailContent() { return detailContent; }
    public void setDetailContent(String detailContent) { this.detailContent = detailContent; }
    public BigDecimal getHotScore() { return hotScore; }
    public void setHotScore(BigDecimal hotScore) { this.hotScore = hotScore; }
    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }
    public Integer getRecommendFlag() { return recommendFlag; }
    public void setRecommendFlag(Integer recommendFlag) { this.recommendFlag = recommendFlag; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
}
