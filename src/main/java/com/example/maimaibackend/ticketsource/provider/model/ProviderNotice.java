package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderNotice(
        String noticeCode,
        String title,
        String content,
        String scope,
        String scopeId,
        int priority
) {}
