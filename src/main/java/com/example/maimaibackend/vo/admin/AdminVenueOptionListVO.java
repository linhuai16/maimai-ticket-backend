package com.example.maimaibackend.vo.admin;

import java.util.List;

public class AdminVenueOptionListVO {
    private String resolvedCityName;
    private Boolean inferredFromStation;
    private Integer total;
    private List<AdminVenueVO> items;

    public String getResolvedCityName() {
        return resolvedCityName;
    }

    public void setResolvedCityName(String resolvedCityName) {
        this.resolvedCityName = resolvedCityName;
    }

    public Boolean getInferredFromStation() {
        return inferredFromStation;
    }

    public void setInferredFromStation(Boolean inferredFromStation) {
        this.inferredFromStation = inferredFromStation;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<AdminVenueVO> getItems() {
        return items;
    }

    public void setItems(List<AdminVenueVO> items) {
        this.items = items;
    }
}
