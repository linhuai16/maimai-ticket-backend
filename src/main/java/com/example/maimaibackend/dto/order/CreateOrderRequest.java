package com.example.maimaibackend.dto.order;

import java.util.List;

public class CreateOrderRequest {

    private Long userId;
    private Long projectId;
    private Long sessionId;
    private Long skuId;
    private Integer quantity;
    private List<Long> audienceIds;
    private String deliveryType;
    private Long addressId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<Long> getAudienceIds() {
        return audienceIds;
    }

    public void setAudienceIds(List<Long> audienceIds) {
        this.audienceIds = audienceIds;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
}
