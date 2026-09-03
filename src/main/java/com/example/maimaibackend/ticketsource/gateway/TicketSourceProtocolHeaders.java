package com.example.maimaibackend.ticketsource.gateway;

/**
 * 真实 HTTP 票源适配器的协议头占位规范。
 * 各平台签名算法不同，具体适配器负责计算签名值。
 */
public final class TicketSourceProtocolHeaders {
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String TIMESTAMP = "X-Timestamp";
    public static final String NONCE = "X-Nonce";
    public static final String SIGNATURE = "X-Signature";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private TicketSourceProtocolHeaders() {
    }
}
