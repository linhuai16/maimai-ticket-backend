package com.example.maimaibackend.ticketsource.order.provider.model;

import java.math.BigDecimal;

public class V11OrderItemBridgeInsert {
    private Long bridgeId;
    private Long orderItemId;
    private Long skuId;
    private Long skuMappingId;
    private String providerSkuId;
    private int quantity;
    private BigDecimal providerUnitPrice;
    private BigDecimal settlementUnitPrice;

    public V11OrderItemBridgeInsert() {}

    public V11OrderItemBridgeInsert(Long bridgeId, Long orderItemId, Long skuMappingId,
                                    String providerSkuId, int quantity,
                                    BigDecimal providerUnitPrice, BigDecimal settlementUnitPrice) {
        this.bridgeId = bridgeId;
        this.orderItemId = orderItemId;
        this.skuMappingId = skuMappingId;
        this.providerSkuId = providerSkuId;
        this.quantity = quantity;
        this.providerUnitPrice = providerUnitPrice;
        this.settlementUnitPrice = settlementUnitPrice;
    }

    public Long getBridgeId() { return bridgeId; }
    public void setBridgeId(Long bridgeId) { this.bridgeId = bridgeId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Long getSkuMappingId() { return skuMappingId; }
    public void setSkuMappingId(Long skuMappingId) { this.skuMappingId = skuMappingId; }
    public String getProviderSkuId() { return providerSkuId; }
    public void setProviderSkuId(String providerSkuId) { this.providerSkuId = providerSkuId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getProviderUnitPrice() { return providerUnitPrice; }
    public void setProviderUnitPrice(BigDecimal providerUnitPrice) { this.providerUnitPrice = providerUnitPrice; }
    public BigDecimal getSettlementUnitPrice() { return settlementUnitPrice; }
    public void setSettlementUnitPrice(BigDecimal settlementUnitPrice) { this.settlementUnitPrice = settlementUnitPrice; }
}
