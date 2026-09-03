package com.example.maimaibackend.ticketsource.fulfillment.shipment;

import com.example.maimaibackend.ticketsource.provider.enums.ShipmentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class V11ShipmentTransitionPolicyTest {
    @Test
    void permitsForwardDeliveryTransitionsAndRejectsTerminalRollback() {
        assertTrue(V11ShipmentTransitionPolicy.canTransition(ShipmentStatus.SHIPPED, ShipmentStatus.IN_TRANSIT));
        assertTrue(V11ShipmentTransitionPolicy.canTransition(ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED));
        assertFalse(V11ShipmentTransitionPolicy.canTransition(ShipmentStatus.DELIVERED, ShipmentStatus.IN_TRANSIT));
        assertFalse(V11ShipmentTransitionPolicy.canTransition(ShipmentStatus.RETURNED, ShipmentStatus.DELIVERED));
    }
}
