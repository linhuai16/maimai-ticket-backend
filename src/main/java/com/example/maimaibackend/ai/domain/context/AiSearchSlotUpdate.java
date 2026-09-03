package com.example.maimaibackend.ai.domain.context;

import java.util.Map;

public record AiSearchSlotUpdate(Map<String, AiSlotOperation> operations) {
    public AiSlotOperation operation(String slot) {
        return operations.getOrDefault(slot, AiSlotOperation.KEEP);
    }
}
