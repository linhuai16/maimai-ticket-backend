package com.example.maimaibackend.ticketsource.provider.mock.dto;

import java.time.OffsetDateTime;

public record MockV11RefundPlanRequest(String refundMode, OffsetDateTime availableAt) {}
