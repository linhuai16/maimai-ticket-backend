package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.StockState;
import com.example.maimaibackend.ticketsource.provider.enums.TicketProductSaleStatus;
import java.time.OffsetDateTime;

public record ProviderInventory(
        String ticketProductId,
        ProviderStatusValue<TicketProductSaleStatus> saleStatus,
        StockState stockState,
        Integer availableStock,
        boolean exact,
        OffsetDateTime snapshotAt,
        String version
) {
    public ProviderInventory {
        ticketProductId = ModelSupport.required(ticketProductId, "ticketProductId");
        if (saleStatus == null || stockState == null) throw new IllegalArgumentException("库存状态不能为空");
        if (availableStock != null && availableStock < 0) throw new IllegalArgumentException("availableStock不能为负数");
        if (stockState == StockState.SOLD_OUT && availableStock != null && availableStock != 0) {
            throw new IllegalArgumentException("SOLD_OUT时精确库存必须为0或未知");
        }
        if (snapshotAt == null) throw new IllegalArgumentException("snapshotAt不能为空");
        version = ModelSupport.required(version, "version");
    }
}
