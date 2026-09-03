package com.example.maimaibackend.ticketsource.provider.model;

/**
 * R5 订单结果不确定补查请求。
 * Provider 可以按商户订单号、创建幂等键中的任意一个或两者组合查询。
 */
public record ProviderOrderLookupRequest(
        String clientOrderNo,
        String idempotencyKey
) {
    public ProviderOrderLookupRequest {
        clientOrderNo = normalize(clientOrderNo);
        idempotencyKey = normalize(idempotencyKey);
        if (clientOrderNo == null && idempotencyKey == null) {
            throw new IllegalArgumentException("clientOrderNo和idempotencyKey不能同时为空");
        }
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}
