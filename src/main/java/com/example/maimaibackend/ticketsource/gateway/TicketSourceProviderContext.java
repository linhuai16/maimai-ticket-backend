package com.example.maimaibackend.ticketsource.gateway;

import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProvider;

/**
 * 单次第三方调用上下文。当前不携带凭证明文。
 */
public class TicketSourceProviderContext {
    private final TicketSourceProvider provider;
    private final TicketSourceRequestMetadata requestMetadata;
    private final TicketSourceOperation operation;

    public TicketSourceProviderContext(
            TicketSourceProvider provider,
            String requestId,
            TicketSourceOperation operation
    ) {
        this(provider, TicketSourceRequestMetadata.readOnly(requestId), operation);
    }

    public TicketSourceProviderContext(
            TicketSourceProvider provider,
            TicketSourceRequestMetadata requestMetadata,
            TicketSourceOperation operation
    ) {
        this.provider = provider;
        this.requestMetadata = requestMetadata;
        this.operation = operation;
    }

    public TicketSourceProvider getProvider() {
        return provider;
    }

    public String getRequestId() {
        return requestMetadata.getRequestId();
    }

    public TicketSourceRequestMetadata getRequestMetadata() {
        return requestMetadata;
    }

    public TicketSourceOperation getOperation() {
        return operation;
    }
}
