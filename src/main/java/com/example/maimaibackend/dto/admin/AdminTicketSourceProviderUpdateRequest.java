package com.example.maimaibackend.dto.admin;

public record AdminTicketSourceProviderUpdateRequest(
        String providerStatus,
        Integer priority,
        String baseUrl,
        String credentialRef,
        Integer connectTimeoutMs,
        Integer readTimeoutMs,
        String remark
) {}
