package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.DynamicQrMode;
import com.example.maimaibackend.ticketsource.provider.enums.IssueTriggerMode;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderCode;
import com.example.maimaibackend.ticketsource.provider.enums.RefundCapabilityScope;

public record ProviderCapabilities(
        ProviderCode providerCode,
        ProviderCode adapterCode,
        String apiVersion,
        boolean resourceIncrementalSync,
        boolean exactInventory,
        boolean venueQuery,
        boolean paperTicket,
        boolean expressDelivery,
        boolean shipmentQuery,
        boolean systemSeatAssignment,
        boolean userSeatSelection,
        IssueTriggerMode issueTriggerMode,
        boolean electronicTicket,
        boolean dynamicQr,
        DynamicQrMode dynamicQrMode,
        boolean refundQuote,
        RefundCapabilityScope refundScope,
        boolean promotionRuleFeed,
        boolean campaignAssetFeed,
        CallbackCapabilities callbacks
) {
    public ProviderCapabilities {
        if (providerCode == null || adapterCode == null) throw new IllegalArgumentException("providerCode/adapterCode不能为空");
        apiVersion = ModelSupport.required(apiVersion, "apiVersion");
        if (issueTriggerMode == null) throw new IllegalArgumentException("issueTriggerMode不能为空");
        if (refundScope == null) throw new IllegalArgumentException("refundScope不能为空");
        callbacks = callbacks == null ? CallbackCapabilities.none() : callbacks;
        if (!dynamicQr && dynamicQrMode != null) throw new IllegalArgumentException("未启用动态二维码时不能声明dynamicQrMode");
        if (userSeatSelection) throw new IllegalArgumentException("麦麦V1.2不允许用户选座");
    }
}
