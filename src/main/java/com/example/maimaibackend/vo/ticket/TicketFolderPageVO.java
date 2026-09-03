package com.example.maimaibackend.vo.ticket;

import java.util.List;

public class TicketFolderPageVO {

    private Long userId;
    private Integer total;
    private List<TicketFolderItemVO> items;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<TicketFolderItemVO> getItems() {
        return items;
    }

    public void setItems(List<TicketFolderItemVO> items) {
        this.items = items;
    }

}