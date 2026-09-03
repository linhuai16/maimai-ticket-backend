package com.example.maimaibackend.ticketsource.gateway;

/**
 * 平台内部统一错误码，不直接暴露任何一家第三方的原始错误码语义。
 */
public enum TicketSourceGatewayErrorCode {
    SUCCESS,
    INVALID_REQUEST,
    PROVIDER_NOT_FOUND,
    PROVIDER_DISABLED,
    ADAPTER_NOT_FOUND,
    GATEWAY_BUSY,
    TIMEOUT,
    REMOTE_ERROR,
    REMOTE_NOT_FOUND,
    INVALID_RESPONSE,
    INTERNAL_ERROR
}
