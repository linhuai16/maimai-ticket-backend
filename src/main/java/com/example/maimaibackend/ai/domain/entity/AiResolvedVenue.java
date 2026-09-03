package com.example.maimaibackend.ai.domain.entity;

public record AiResolvedVenue(Long entityId, String canonicalName, String city, String matchedText, String source) {
    public static AiResolvedVenue empty() {
        return new AiResolvedVenue(null, "", "", "", "NONE");
    }

    public boolean found() {
        return canonicalName != null && !canonicalName.isBlank();
    }
}
