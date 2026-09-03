package com.example.maimaibackend.ticketsource.user.model;

import java.time.LocalDateTime;

/** 用户侧逐票履约视图；不包含 providerTicketId/providerTicketProductId。 */
public record V12UserTicketView(
        Long ticketId,
        String ticketNo,
        String ticketStatus,
        String credentialType,
        String dynamicQrMode,
        String credentialVersion,
        LocalDateTime credentialExpireTime,
        Integer refreshAfterSeconds,
        String seatInfo,
        String clientTicketNo,
        String holderRef,
        String holderName
) {}
