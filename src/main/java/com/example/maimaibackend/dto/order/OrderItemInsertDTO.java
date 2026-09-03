package com.example.maimaibackend.dto.order;

import java.math.BigDecimal;

public class OrderItemInsertDTO {

    private Long orderItemId;
    private Long orderId;
    private Long skuId;
    private String skuName;
    private BigDecimal unitPrice;
    private BigDecimal facePrice;
    private BigDecimal providerSalePrice;
    private BigDecimal settlementPrice;
    private Integer quantity;
    private BigDecimal subtotalAmount;

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getFacePrice() { return facePrice; }

    public void setFacePrice(BigDecimal facePrice) { this.facePrice = facePrice; }

    public BigDecimal getProviderSalePrice() { return providerSalePrice; }

    public void setProviderSalePrice(BigDecimal providerSalePrice) { this.providerSalePrice = providerSalePrice; }

    public BigDecimal getSettlementPrice() { return settlementPrice; }

    public void setSettlementPrice(BigDecimal settlementPrice) { this.settlementPrice = settlementPrice; }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }
}
