package com.example.maimaibackend.ai.domain.search;

import com.example.maimaibackend.ai.domain.context.AiActiveSlot;
import com.example.maimaibackend.ai.domain.context.AiSlotDelta;

import java.util.List;
import java.util.Map;

public record AiSemanticParseResult(
        Map<String, String> recognizedSlots,
        AiActiveSlot activeSlotCandidate,
        Confidence confidence,
        List<AiSlotDelta> slotDeltas
) {
    public enum Confidence {
        HIGH,
        MEDIUM,
        LOW
    }

    public static AiSemanticParseResult empty() {
        return new AiSemanticParseResult(Map.of(), AiActiveSlot.NONE, Confidence.LOW, List.of());
    }

    public boolean recognizes(AiActiveSlot slot) {
        return slot != null && recognizedSlots.containsKey(slot.name());
    }
}
