package com.example.maimaibackend.ticketsource.purchase.model;

import java.util.List;

/** V1.3 提交订单页初始化数据。 */
public record V13PurchaseInitView(
        Long userId,
        Long projectId,
        Long sessionId,
        Long skuId,
        String skuName,
        int quantity,
        Integer limitPerOrder,
        V13InventoryView inventoryView,
        String purchaseMode,
        List<V13FulfillmentOptionView> fulfillmentOptions,
        V13EstimatedAmountView estimatedAmount,
        List<V13AudienceEligibilityView> audienceEligibility,
        List<String> notices
) {}
