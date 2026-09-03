package com.example.maimaibackend.ticketsource.sync.model;

import java.math.BigDecimal;

public class TicketSourceLocalProject {
    private Long projectId;
    private String title;
    private Long categoryId;
    private String posterUrl;
    private String detailContent;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String projectStatus;

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
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }
}
