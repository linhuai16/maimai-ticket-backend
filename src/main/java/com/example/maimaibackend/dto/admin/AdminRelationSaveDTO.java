package com.example.maimaibackend.dto.admin;

public class AdminRelationSaveDTO {
    private Long targetId;
    private Integer sortOrder;

    public AdminRelationSaveDTO() {
    }

    public AdminRelationSaveDTO(Long targetId, Integer sortOrder) {
        this.targetId = targetId;
        this.sortOrder = sortOrder;
    }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
