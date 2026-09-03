package com.example.maimaibackend.ai.domain.context;

import java.util.List;

public record AiSearchResultState(
        List<Long> currentResultIds,
        List<Long> shownProjectIds,
        int offset,
        int resultCount,
        boolean hasMore
) {
    public static AiSearchResultState empty() {
        return new AiSearchResultState(List.of(), List.of(), 0, 0, false);
    }
}
