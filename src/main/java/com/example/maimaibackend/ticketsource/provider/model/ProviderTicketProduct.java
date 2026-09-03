package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.InventoryMode;
import com.example.maimaibackend.ticketsource.provider.enums.TicketProductSaleStatus;
import com.example.maimaibackend.ticketsource.provider.enums.TicketProductType;
import java.time.OffsetDateTime;

public record ProviderTicketProduct(
        String ticketProductId,
        String projectId,
        String sessionId,
        String productName,
        TicketProductType productType,
        ProviderMoney facePrice,
        ProviderMoney salePrice,
        ProviderMoney settlementPrice,
        ProviderStatusValue<TicketProductSaleStatus> saleStatus,
        String subStatus,
        InventoryMode inventoryMode,
        Integer availableStock,
        Integer maxQuantityPerOrder,
        String version,
        OffsetDateTime updatedAt
) {
    public ProviderTicketProduct {
        ticketProductId = ModelSupport.required(ticketProductId, "ticketProductId");
        projectId = ModelSupport.required(projectId, "projectId");
        sessionId = ModelSupport.required(sessionId, "sessionId");
        productName = ModelSupport.required(productName, "productName");
        productType = productType == null ? TicketProductType.SINGLE : productType;
        if (saleStatus == null) throw new IllegalArgumentException("saleStatus不能为空");
        inventoryMode = inventoryMode == null ? InventoryMode.STATUS_ONLY : inventoryMode;
        if (availableStock != null && availableStock < 0) throw new IllegalArgumentException("availableStock不能为负数");
        version = ModelSupport.required(version, "version");
        if (updatedAt == null) throw new IllegalArgumentException("updatedAt不能为空");
    }
}
