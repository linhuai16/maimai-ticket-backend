package com.example.maimaibackend.dto.admin;

import java.time.LocalDate;

public record AdminTicketSourceSettlementCreateRequest(
        String providerCode,
        LocalDate dateFrom,
        LocalDate dateTo,
        String remark
) {}
