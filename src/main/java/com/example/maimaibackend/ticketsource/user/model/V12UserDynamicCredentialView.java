package com.example.maimaibackend.ticketsource.user.model;

import java.time.OffsetDateTime;

/** 动态二维码一次性用户响应；不包含 providerTicketId，payload 禁止本地持久化。 */
public record V12UserDynamicCredentialView(
        Long ticketId,
        String credentialType,
        String credentialPayload,
        String credentialVersion,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        int refreshAfterSeconds,
        OffsetDateTime serverTime
) {}
