package com.example.maimaibackend.ticketsource.provider.mock.dto;

import com.example.maimaibackend.ticketsource.provider.enums.CredentialType;
import com.example.maimaibackend.ticketsource.provider.enums.DynamicQrMode;
import java.time.OffsetDateTime;

public record MockV11IssuePlanRequest(
        String issueMode,
        CredentialType credentialType,
        DynamicQrMode dynamicQrMode,
        Integer failTicketIndex,
        OffsetDateTime availableAt
) {}
