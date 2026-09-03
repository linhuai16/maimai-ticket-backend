package com.example.maimaibackend.ai.api.dto;

import com.example.maimaibackend.ai.domain.context.AiEntityContext;
import com.example.maimaibackend.ai.domain.context.AiActiveSlot;
import com.example.maimaibackend.ai.domain.context.AiSearchContext;
import com.example.maimaibackend.ai.domain.context.AiSearchResultState;
import com.example.maimaibackend.ai.domain.context.AiSearchResultReference;

import java.util.List;

public record AiChatRequest(
        List<AiChatMessage> messages,
        Long contextProjectId,
        Long contextSessionId,
        String currentCity,
        AiSearchContext searchContext,
        List<AiSearchResultReference> lastSearchResults,
        Long selectedProjectId,
        Long selectedSessionId,
        AiSearchResultState searchResultState,
        AiEntityContext entityContext,
        AiActiveSlot activeSlot,
        String conversationId
) {
    public record AiChatMessage(String role, String content) {}

}
