package com.example.maimaibackend.ai.domain.search;

import com.example.maimaibackend.ai.domain.context.AiSearchContext;
import com.example.maimaibackend.vo.performance.PerformanceCardVO;

import java.util.List;

public record SearchExecutionResult(
        AiSearchContext finalContext,
        int matchedTotal,
        int displayedCount,
        List<PerformanceCardVO> items,
        List<Long> resultIds,
        boolean hasMore,
        int nextCursor
) {
    public SearchExecutionResult {
        items = items == null ? List.of() : List.copyOf(items);
        resultIds = resultIds == null ? List.of() : List.copyOf(resultIds);
    }
}
