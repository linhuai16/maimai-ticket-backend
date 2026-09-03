package com.example.maimaibackend.ai.domain.context;

public enum AiActiveSlot {
    NONE,
    TIME,
    PRICE,
    CITY,
    CATEGORY,
    VENUE,
    SORT,
    RESULT_REFERENCE;

    public static AiActiveSlot from(String value) {
        if (value == null || value.isBlank()) return NONE;
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
