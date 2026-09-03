package com.example.maimaibackend.vo.order;

import java.util.List;

public class OrderListPageVO {

    private Long userId;
    private String tab;
    private Integer pageNo;
    private Integer pageSize;
    private Integer total;
    private Boolean hasMore;
    private List<OrderListItemVO> orders;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<OrderListItemVO> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderListItemVO> orders) {
        this.orders = orders;
    }
}
