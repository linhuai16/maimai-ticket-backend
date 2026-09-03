package com.example.maimaibackend.dto.admin;

import java.util.List;

public class AdminUpdateTagIdsRequest {
    private List<Long> tagIds;

    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
}
