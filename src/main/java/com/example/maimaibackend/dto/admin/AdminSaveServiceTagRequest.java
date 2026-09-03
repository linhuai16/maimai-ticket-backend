package com.example.maimaibackend.dto.admin;

public class AdminSaveServiceTagRequest {
    private Long tagId;
    private String tagName;
    private String description;

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
