package com.example.maimaibackend.ticketsource.provider.compat;

import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderOrderStatus;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderRefundStatus;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderTicketStatus;
import com.example.maimaibackend.ticketsource.provider.enums.StockState;
import com.example.maimaibackend.ticketsource.provider.model.ProviderInventory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LegacyTicketSourceV11MapperTest {
    @Test
    void mapsPositiveStockAsAvailableAndExact() {
        ProviderInventory result = LegacyTicketSourceV11Mapper.inventory(inventory(7, "SNAPSHOT", "ON_SALE"));

        assertEquals(StockState.AVAILABLE, result.stockState());
        assertEquals(7, result.availableStock());
        assertTrue(result.exact());
    }

    @Test
    void mapsZeroStockAsSoldOut() {
        ProviderInventory result = LegacyTicketSourceV11Mapper.inventory(inventory(0, "REALTIME_QUERY", "ON_SALE"));

        assertEquals(StockState.SOLD_OUT, result.stockState());
        assertEquals(0, result.availableStock());
        assertTrue(result.exact());
    }

    @Test
    void preservesUnknownStockWithoutTreatingItAsSoldOut() {
        ProviderInventory result = LegacyTicketSourceV11Mapper.inventory(inventory(null, "STATUS_ONLY", "ON_SALE"));

        assertEquals(StockState.AVAILABLE, result.stockState());
        assertNull(result.availableStock());
        assertFalse(result.exact());
    }

    @Test
    void mapsLegacyAliasesAndUnknownValues() {
        assertEquals(ProviderOrderStatus.RESERVED, LegacyTicketSourceV11Mapper.orderStatus("WAIT_PAY"));
        assertEquals(ProviderOrderStatus.UNKNOWN, LegacyTicketSourceV11Mapper.orderStatus("UNKNOWN_RESULT"));
        assertEquals(ProviderTicketStatus.USED, LegacyTicketSourceV11Mapper.ticketStatus("CHECKED"));
        assertEquals(ProviderRefundStatus.SUCCESS, LegacyTicketSourceV11Mapper.refundStatus("REFUND_SUCCESS"));
    }

    private TicketSourceInventory inventory(Integer stock, String mode, String saleStatus) {
        TicketSourceInventory value = new TicketSourceInventory();
        value.setProviderSkuId("sku-1");
        value.setAvailableStock(stock);
        value.setInventoryMode(mode);
        value.setSaleStatus(saleStatus);
        value.setProviderUpdateTime(LocalDateTime.of(2026, 9, 3, 12, 0));
        value.setDataVersion("v1");
        return value;
    }
}
