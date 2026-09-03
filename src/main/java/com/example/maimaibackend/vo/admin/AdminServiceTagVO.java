package com.example.maimaibackend.vo.admin;

public class AdminServiceTagVO {
    private Long tagId;
    private String tagName;
    private String description;
    private Integer sortOrder;
    private Integer projectCount;
    private Integer manualProjectCount;
    private Integer automaticProjectCount;
    private Boolean systemRefundTag;

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getProjectCount() { return projectCount; }
    public void setProjectCount(Integer projectCount) { this.projectCount = projectCount; }
    public Integer getManualProjectCount() { return manualProjectCount; }
    public void setManualProjectCount(Integer manualProjectCount) { this.manualProjectCount = manualProjectCount; }
    public Integer getAutomaticProjectCount() { return automaticProjectCount; }
    public void setAutomaticProjectCount(Integer automaticProjectCount) { this.automaticProjectCount = automaticProjectCount; }
    public Boolean getSystemRefundTag() { return systemRefundTag; }
    public void setSystemRefundTag(Boolean systemRefundTag) { this.systemRefundTag = systemRefundTag; }
}
