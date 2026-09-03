package com.example.maimaibackend.ticketsource.provider.compat;

import com.example.maimaibackend.ticketsource.provider.enums.FrontendOrderState;
import com.example.maimaibackend.ticketsource.provider.enums.FrontendTicketState;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderOrderStatus;
import com.example.maimaibackend.ticketsource.provider.enums.ProviderTicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProviderStateCompatibilityMapperTest {
    @Test
    void keepsProviderUnknownAndFailureOutOfWaitUse() {
        assertEquals(FrontendOrderState.CANCELED,
                ProviderStateCompatibilityMapper.toFrontendOrderState(ProviderOrderStatus.UNKNOWN));
        assertEquals(FrontendOrderState.CANCELED,
                ProviderStateCompatibilityMapper.toFrontendOrderState(ProviderOrderStatus.FAILED));
    }

    @Test
    void mapsProviderTicketLifecycleToFrontendStates() {
        assertEquals(FrontendTicketState.UNUSED,
                ProviderStateCompatibilityMapper.toFrontendTicketState(ProviderTicketStatus.UNUSED));
        assertEquals(FrontendTicketState.CHECKED,
                ProviderStateCompatibilityMapper.toFrontendTicketState(ProviderTicketStatus.USED));
        assertEquals(FrontendTicketState.EXPIRED,
                ProviderStateCompatibilityMapper.toFrontendTicketState(ProviderTicketStatus.VOIDED));
    }
}
