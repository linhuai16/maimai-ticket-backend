package com.example.maimaibackend.ticketsource.order.provider.model;

import java.math.BigDecimal;
import java.util.List;

/** 服务端确认后的单个票档计价快照。 */
public record V11OrderQuoteItem(
        Long skuId,
        String skuName,
        String providerSkuId,
        int quantity,
        BigDecimal faceUnitPrice,
        BigDecimal providerSaleUnitPrice,
        BigDecimal settlementUnitPrice,
        BigDecimal subtotalAmount,
        List<V11TicketSelection> tickets,
        String providerPriceVersion
) {}
