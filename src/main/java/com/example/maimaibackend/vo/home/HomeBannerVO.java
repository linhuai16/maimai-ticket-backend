package com.example.maimaibackend.vo.home;

public class HomeBannerVO {

    private Long bannerId;
    private String bannerTitle;
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

    public String getBannerTitle() {
        return bannerTitle;
    }

    public void setBannerTitle(String bannerTitle) {
        this.bannerTitle = bannerTitle;
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