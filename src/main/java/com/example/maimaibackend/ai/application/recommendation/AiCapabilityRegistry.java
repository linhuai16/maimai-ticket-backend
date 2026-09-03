package com.example.maimaibackend.ai.application.recommendation;

import com.example.maimaibackend.ai.domain.context.AiSearchContext;
import com.example.maimaibackend.ai.domain.context.AiSearchResultState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AiCapabilityRegistry {
    public List<String> supported(AiSearchContext context, AiSearchResultState resultState,
                                  Long selectedProjectId, Long selectedSessionId) {
        List<String> values = new ArrayList<>();
        values.add("CHANGE_CITY");
        values.add("CHANGE_TIME");
        values.add("CHANGE_PRICE");
        values.add("CHANGE_CATEGORY");
        values.add("CHANGE_SORT");
        values.add("VIEW_ALL_RESULTS");
        if (context != null) {
            String timeIntent = value(context.timeIntent()).toUpperCase();
            if (!timeIntent.isBlank() && !"FUTURE".equals(timeIntent) && !"PAST".equals(timeIntent)) values.add("BROADEN_TIME");
            if (!timeIntent.isBlank()) values.add("CLEAR_TIME");
            if (context.minPrice() != null || context.maxPrice() != null) values.add("CLEAR_PRICE");
            if (!value(context.category()).isBlank()) values.add("CLEAR_CATEGORY");
            if (!value(context.venue()).isBlank()) values.add("CLEAR_VENUE");
            if (!value(context.keyword()).isBlank()) values.add("CLEAR_KEYWORD");
        }
        int currentCount = resultState == null || resultState.currentResultIds() == null
                ? 0 : resultState.currentResultIds().size();
        if (resultState != null && resultState.hasMore() && currentCount > 0) values.add("CONTINUE_RESULTS");
        if (currentCount > 0 || selectedProjectId != null || selectedSessionId != null) {
            values.add("QUERY_SESSION");
            values.add("QUERY_TICKET");
            values.add("QUERY_REFUND");
        }
        if (currentCount >= 2) {
            values.add("COMPARE_PRICE");
            values.add("COMPARE_SESSION");
        }
        return List.copyOf(values);
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }
}
