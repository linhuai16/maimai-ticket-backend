package com.example.maimaibackend.ticketsource.provider.mock.dto;

public record MockV11BehaviorRequest(
        Boolean enabled,
        Integer delayMs,
        String forcedErrorCode,
        String forcedErrorMessage,
        Boolean retryable
) {}
