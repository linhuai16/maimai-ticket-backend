package com.example.maimaibackend.vo.admin;

import java.util.List;

public class AdminMediaPageVO {
    private Integer total;
    private Integer pageNo;
    private Integer pageSize;
    private List<AdminMediaItemVO> items;

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Integer getPageNo() { return pageNo; }
    public void setPageNo(Integer pageNo) { this.pageNo = pageNo; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public List<AdminMediaItemVO> getItems() { return items; }
    public void setItems(List<AdminMediaItemVO> items) { this.items = items; }
}
