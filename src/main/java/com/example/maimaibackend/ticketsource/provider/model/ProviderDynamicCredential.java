package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.CredentialType;
import java.time.OffsetDateTime;

public record ProviderDynamicCredential(
        String providerTicketId,
        CredentialType credentialType,
        String credentialPayload,
        String credentialVersion,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        int refreshAfterSeconds,
        OffsetDateTime serverTime
) {
    public ProviderDynamicCredential {
        providerTicketId = ModelSupport.required(providerTicketId, "providerTicketId");
        if (credentialType != CredentialType.DYNAMIC_QR) throw new IllegalArgumentException("动态凭证类型必须为DYNAMIC_QR");
        credentialPayload = ModelSupport.required(credentialPayload, "credentialPayload");
        credentialVersion = ModelSupport.required(credentialVersion, "credentialVersion");
        if (issuedAt == null || expiresAt == null || serverTime == null) throw new IllegalArgumentException("动态码时间字段不能为空");
        if (!expiresAt.isAfter(issuedAt)) throw new IllegalArgumentException("动态码过期时间必须晚于签发时间");
        if (refreshAfterSeconds <= 0) throw new IllegalArgumentException("refreshAfterSeconds必须大于0");
    }
}
