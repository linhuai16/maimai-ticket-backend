package com.example.maimaibackend.ticketsource.provider.model;

/**
 * 同时保存统一状态和平台原始状态，避免 Adapter 丢失对账信息。
 */
public record ProviderStatusValue<E extends Enum<E>>(
        E status,
        String sourceStatusCode,
        String sourceStatusText
) {
    public ProviderStatusValue {
        if (status == null) throw new IllegalArgumentException("统一状态不能为空");
    }
}
