package com.example.maimaibackend.ticketsource.gateway;

/**
 * 适配器向统一网关报告的标准异常。
 */
public class TicketSourceAdapterException extends RuntimeException {
    private final TicketSourceGatewayErrorCode gatewayErrorCode;
    private final String providerErrorCode;
    private final boolean retryable;

    public TicketSourceAdapterException(
            TicketSourceGatewayErrorCode gatewayErrorCode,
            String providerErrorCode,
            String message,
            boolean retryable
    ) {
        super(message);
        this.gatewayErrorCode = gatewayErrorCode == null
                ? TicketSourceGatewayErrorCode.REMOTE_ERROR
                : gatewayErrorCode;
        this.providerErrorCode = providerErrorCode;
        this.retryable = retryable;
    }

    public TicketSourceGatewayErrorCode getGatewayErrorCode() {
        return gatewayErrorCode;
    }

    public String getProviderErrorCode() {
        return providerErrorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
