package com.example.maimaibackend.ticketsource.provider.mock.dto;

/** LOCAL_MOCK 库存快照修改请求。 */
public record MockV11SkuInventoryRequest(
        Integer availableStock,
        String inventoryMode,
        String saleStatus
) {}
