package com.example.maimaibackend.dto.admin;

import java.math.BigDecimal;

public record AdminTicketSourceSettlementAdjustmentRequest(
        BigDecimal amount,
        String remark
) {}
