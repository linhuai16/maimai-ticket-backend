package com.example.maimaibackend.ticketsource.gateway;

import com.example.maimaibackend.ticketsource.provider.model.ProviderOrderLookupRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketSourceRequestMetadataTest {
    @Test
    void trimsAndKeepsWriteIdempotencyKey() {
        TicketSourceRequestMetadata metadata = TicketSourceRequestMetadata.write("request-1", "  order:create:1  ");

        assertEquals("order:create:1", metadata.getIdempotencyKey());
        assertNotNull(metadata.getNonce());
        assertFalse(metadata.getNonce().isBlank());
    }

    @Test
    void rejectsMissingWriteAndLookupKeys() {
        assertThrows(IllegalArgumentException.class,
                () -> TicketSourceRequestMetadata.write("request-1", "  "));
        assertThrows(IllegalArgumentException.class,
                () -> new ProviderOrderLookupRequest(" ", null));

        ProviderOrderLookupRequest lookup = new ProviderOrderLookupRequest(" order-1 ", " key-1 ");
        assertEquals("order-1", lookup.clientOrderNo());
        assertEquals("key-1", lookup.idempotencyKey());
    }
}
