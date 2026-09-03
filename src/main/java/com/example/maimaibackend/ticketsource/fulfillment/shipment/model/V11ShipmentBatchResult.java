package com.example.maimaibackend.ticketsource.fulfillment.shipment.model;

import java.util.List;

public record V11ShipmentBatchResult(
        int requested,
        int succeeded,
        int failed,
        List<Long> succeededOrderIds,
        List<String> failures
) {
    public V11ShipmentBatchResult {
        succeededOrderIds = succeededOrderIds == null ? List.of() : List.copyOf(succeededOrderIds);
        failures = failures == null ? List.of() : List.copyOf(failures);
    }
}
