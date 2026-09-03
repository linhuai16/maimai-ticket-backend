package com.example.maimaibackend.ticketsource.order.provider.model;

import java.util.List;

public record V11UnknownCreateRecoveryBatchResult(
        int requested,
        int recovered,
        int unresolved,
        int manualReview,
        List<Long> recoveredOrderIds,
        List<String> failures
) {
    public V11UnknownCreateRecoveryBatchResult {
        recoveredOrderIds = recoveredOrderIds == null ? List.of() : List.copyOf(recoveredOrderIds);
        failures = failures == null ? List.of() : List.copyOf(failures);
    }
}
