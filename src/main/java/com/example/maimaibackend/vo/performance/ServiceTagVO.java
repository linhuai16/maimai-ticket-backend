package com.example.maimaibackend.vo.performance;

import java.util.List;

public class ServiceTagVO {
    private Long tagId;
    private String tagName;
    private String description;
    private Integer sortOrder;
    private List<String> detailItems;

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public List<String> getDetailItems() { return detailItems; }
    public void setDetailItems(List<String> detailItems) { this.detailItems = detailItems; }
}
