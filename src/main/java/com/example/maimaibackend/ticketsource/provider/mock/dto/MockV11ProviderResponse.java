package com.example.maimaibackend.ticketsource.provider.mock.dto;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** LOCAL_MOCK V1.1 对外响应，刻意不复用麦麦内部 Result。 */
public record MockV11ProviderResponse<T>(
        boolean success,
        String code,
        String message,
        String requestId,
        boolean retryable,
        OffsetDateTime serverTime,
        T data
) {
    public static <T> MockV11ProviderResponse<T> ok(String requestId, T data) {
        return new MockV11ProviderResponse<>(true, "SUCCESS", "success", requestId, false,
                OffsetDateTime.now(ZoneOffset.ofHours(8)), data);
    }

    public static <T> MockV11ProviderResponse<T> fail(
            String requestId, String code, String message, boolean retryable
    ) {
        return new MockV11ProviderResponse<>(false, code, message, requestId, retryable,
                OffsetDateTime.now(ZoneOffset.ofHours(8)), null);
    }
}
