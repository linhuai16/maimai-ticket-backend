package com.example.maimaibackend.ticketsource.workflow.model;

import java.time.OffsetDateTime;

/** 仅作为一次 HTTP 响应返回；credentialPayload 禁止写入本地数据库。 */
public record V12DynamicCredentialView(
        Long ticketId,
        String providerTicketId,
        String credentialType,
        String credentialPayload,
        String credentialVersion,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        int refreshAfterSeconds,
        OffsetDateTime serverTime
) {}
