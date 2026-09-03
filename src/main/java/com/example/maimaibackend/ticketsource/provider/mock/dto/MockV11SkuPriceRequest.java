package com.example.maimaibackend.ticketsource.provider.mock.dto;

import java.math.BigDecimal;

public record MockV11SkuPriceRequest(
        BigDecimal facePrice,
        BigDecimal salePrice,
        BigDecimal settlementPrice
) {}
