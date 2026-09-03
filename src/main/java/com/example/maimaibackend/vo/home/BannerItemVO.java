package com.example.maimaibackend.vo.home;

public class BannerItemVO {

    private Long bannerId;
    private String title;
    private String imageUrl;
    private Long targetProjectId;
    private Long targetSessionId;
    private Integer sortOrder;

    public Long getBannerId() {
        return bannerId;
    }

    public void setBannerId(Long bannerId) {
        this.bannerId = bannerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getTargetProjectId() {
        return targetProjectId;
    }

    public void setTargetProjectId(Long targetProjectId) {
        this.targetProjectId = targetProjectId;
    }

    public Long getTargetSessionId() {
        return targetSessionId;
    }

    public void setTargetSessionId(Long targetSessionId) {
        this.targetSessionId = targetSessionId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}