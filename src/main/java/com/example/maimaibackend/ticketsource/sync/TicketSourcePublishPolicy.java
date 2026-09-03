package com.example.maimaibackend.ticketsource.sync;

import com.example.maimaibackend.ticketsource.domain.enums.TicketSourceInventoryAuthority;
import com.example.maimaibackend.ticketsource.domain.enums.TicketSourceInventoryMode;
import com.example.maimaibackend.ticketsource.domain.enums.TicketSourceSaleStatus;

import java.util.Locale;

public final class TicketSourcePublishPolicy {
    private TicketSourcePublishPolicy() {
    }

    public static String normalizeSaleStatus(String value) {
        if (value == null || value.isBlank()) {
            return TicketSourceSaleStatus.UNKNOWN.name();
        }
        try {
            return TicketSourceSaleStatus.valueOf(value.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException ignored) {
            return TicketSourceSaleStatus.UNKNOWN.name();
        }
    }

    public static String normalizeInventoryMode(String value) {
        if (value == null || value.isBlank()) {
            return TicketSourceInventoryMode.UNKNOWN.name();
        }
        try {
            return TicketSourceInventoryMode.valueOf(value.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException ignored) {
            return TicketSourceInventoryMode.UNKNOWN.name();
        }
    }

    public static String inventoryAuthority(String inventoryMode) {
        return TicketSourceInventoryMode.REALTIME_QUERY.name().equals(normalizeInventoryMode(inventoryMode))
                ? TicketSourceInventoryAuthority.PROVIDER_REALTIME.name()
                : TicketSourceInventoryAuthority.PROVIDER_SNAPSHOT.name();
    }

    public static int initialLocalStock(Integer providerAvailableStock, boolean applyStock) {
        if (!applyStock || providerAvailableStock == null) {
            return 0;
        }
        return Math.max(providerAvailableStock, 0);
    }
}
