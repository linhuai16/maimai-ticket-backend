package com.example.maimaibackend.ai.domain.context;

public record AiSlotDelta(String slot, AiSlotOperation operation, String value) {
    public static AiSlotDelta replace(String slot, String value) {
        return new AiSlotDelta(slot, AiSlotOperation.REPLACE, value == null ? "" : value);
    }

    public static AiSlotDelta clear(String slot) {
        return new AiSlotDelta(slot, AiSlotOperation.CLEAR, "");
    }
}
