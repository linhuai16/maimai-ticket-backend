package com.example.maimaibackend.dto.admin;

import java.util.List;

public class AdminUpdateNoticeIdsRequest {
    private List<Long> noticeIds;

    public List<Long> getNoticeIds() { return noticeIds; }
    public void setNoticeIds(List<Long> noticeIds) { this.noticeIds = noticeIds; }
}
