package com.example.maimaibackend.ticketsource.gateway;

import java.util.UUID;

/**
 * 单次第三方请求的追踪、签名和幂等元数据。
 */
public class TicketSourceRequestMetadata {
    private final String requestId;
    private final long timestampEpochMs;
    private final String nonce;
    private final String idempotencyKey;

    private TicketSourceRequestMetadata(
            String requestId,
            long timestampEpochMs,
            String nonce,
            String idempotencyKey
    ) {
        this.requestId = requestId;
        this.timestampEpochMs = timestampEpochMs;
        this.nonce = nonce;
        this.idempotencyKey = idempotencyKey;
    }

    public static TicketSourceRequestMetadata readOnly(String requestId) {
        return new TicketSourceRequestMetadata(
                requestId,
                System.currentTimeMillis(),
                UUID.randomUUID().toString().replace("-", ""),
                null
        );
    }

    public static TicketSourceRequestMetadata write(String requestId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("写操作幂等键不能为空");
        }
        return new TicketSourceRequestMetadata(
                requestId,
                System.currentTimeMillis(),
                UUID.randomUUID().toString().replace("-", ""),
                idempotencyKey.trim()
        );
    }

    public String getRequestId() { return requestId; }
    public long getTimestampEpochMs() { return timestampEpochMs; }
    public String getNonce() { return nonce; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
