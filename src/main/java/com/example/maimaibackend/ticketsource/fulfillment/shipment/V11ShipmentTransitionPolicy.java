package com.example.maimaibackend.ticketsource.fulfillment.shipment;

import com.example.maimaibackend.ticketsource.provider.enums.ShipmentStatus;

import java.util.List;

public final class V11ShipmentTransitionPolicy {
    private V11ShipmentTransitionPolicy() {}

    public static boolean canTransition(ShipmentStatus current, ShipmentStatus next) {
        if (next == null) return false;
        if (current == null || current == next) return true;
        return switch (current) {
            case NOT_REQUIRED -> next == ShipmentStatus.NOT_REQUIRED;
            case WAIT_SHIPMENT -> next != ShipmentStatus.NOT_REQUIRED;
            case SHIPPED -> List.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED,
                    ShipmentStatus.EXCEPTION, ShipmentStatus.RETURNED).contains(next);
            case IN_TRANSIT -> List.of(ShipmentStatus.DELIVERED, ShipmentStatus.EXCEPTION,
                    ShipmentStatus.RETURNED).contains(next);
            case EXCEPTION -> List.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED,
                    ShipmentStatus.RETURNED).contains(next);
            case DELIVERED -> next == ShipmentStatus.RETURNED;
            case RETURNED -> false;
        };
    }

    public static boolean requiresWaybill(ShipmentStatus status) {
        return List.of(ShipmentStatus.SHIPPED, ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.DELIVERED, ShipmentStatus.RETURNED).contains(status);
    }
}
