package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.CredentialType;
import com.example.maimaibackend.ticketsource.provider.enums.DynamicQrMode;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderTicketStatus;
import com.example.maimaibackend.ticketsource.provider.enums.ValidateStatus;
import java.time.OffsetDateTime;

public record ProviderTicketCredential(
        String providerTicketId,
        String clientTicketNo,
        String holderRef,
        String ticketProductId,
        ProviderStatusValue<ProviderTicketStatus> ticketStatus,
        CredentialType credentialType,
        String credentialPayload,
        String credentialVersion,
        DynamicQrMode dynamicQrMode,
        ProviderSeatAssignment seat,
        ValidateStatus validateStatus,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        String errorCode,
        String errorMessage,
        String version
) {
    public ProviderTicketCredential {
        providerTicketId = ModelSupport.required(providerTicketId, "providerTicketId");
        if (ticketStatus == null || credentialType == null) throw new IllegalArgumentException("票状态和凭证类型不能为空");
        validateStatus = validateStatus == null ? ValidateStatus.UNKNOWN : validateStatus;
        if (credentialType == CredentialType.DYNAMIC_QR && dynamicQrMode == null) {
            throw new IllegalArgumentException("动态二维码必须声明dynamicQrMode");
        }
        if (credentialType == CredentialType.DYNAMIC_QR && credentialPayload != null) {
            // 查询履约结果不得携带/持久化动态码明文；动态内容只能通过 refresh 接口短期返回。
            credentialPayload = null;
        }
    }
}
