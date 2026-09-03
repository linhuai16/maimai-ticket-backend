package com.example.maimaibackend.ai.domain.context;

import com.example.maimaibackend.ai.domain.action.AiConversationAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AiContextUpdater {
    private static final List<String> SLOTS = List.of(
            "city", "category", "keyword", "venue", "minPrice", "maxPrice",
            "startTime", "endTime", "timeIntent", "sort");

    public UpdatedContext apply(AiSearchContext previous, String currentCity,
                                AiConversationAction action, List<AiSlotDelta> deltas,
                                LocalDateTime now) {
        AiSearchContext old = previous == null ? empty() : previous;
        boolean rebuild = action == AiConversationAction.NEW_SEARCH;
        Values values = rebuild ? new Values() : new Values(old);
        Map<String, AiSlotOperation> operations = new LinkedHashMap<>();
        for (String slot : SLOTS) operations.put(slot, AiSlotOperation.KEEP);

        if (action == AiConversationAction.CLEAR_FILTER) {
            values.category = "";
            values.keyword = "";
            values.venue = "";
            values.minPrice = null;
            values.maxPrice = null;
            values.startTime = "";
            values.endTime = "";
            values.timeIntent = "";
            values.sort = "";
            for (String slot : SLOTS) {
                if (!"city".equals(slot)) operations.put(slot, AiSlotOperation.CLEAR);
            }
        }

        if (deltas != null) {
            for (AiSlotDelta delta : deltas) apply(values, operations, delta);
        }

        if (rebuild) {
            for (String slot : List.of("category", "keyword", "venue", "minPrice", "maxPrice")) {
                if (operations.get(slot) == AiSlotOperation.KEEP) operations.put(slot, AiSlotOperation.CLEAR);
            }
        }
        if (values.city.isBlank()) {
            values.city = value(currentCity);
            if (!values.city.isBlank()) operations.put("city", AiSlotOperation.REPLACE);
        }
        if (values.timeIntent.isBlank()) {
            values.startTime = now.toString();
            values.endTime = "";
            values.timeIntent = "FUTURE";
            operations.put("startTime", AiSlotOperation.REPLACE);
            operations.put("endTime", AiSlotOperation.CLEAR);
            operations.put("timeIntent", AiSlotOperation.REPLACE);
        }
        if (values.sort.isBlank()) {
            values.sort = "NEAREST";
            operations.put("sort", AiSlotOperation.REPLACE);
        }
        AiSearchContext context = new AiSearchContext(values.city, values.category, values.keyword, values.venue,
                values.minPrice, values.maxPrice, values.startTime, values.endTime, values.timeIntent, values.sort);
        return new UpdatedContext(context, new AiSearchSlotUpdate(Map.copyOf(operations)));
    }

    private void apply(Values values, Map<String, AiSlotOperation> operations, AiSlotDelta delta) {
        if (delta == null || !operations.containsKey(delta.slot())) return;
        AiSlotOperation operation = delta.operation() == null ? AiSlotOperation.KEEP : delta.operation();
        operations.put(delta.slot(), operation);
        if (operation == AiSlotOperation.KEEP) return;
        String value = operation == AiSlotOperation.CLEAR ? "" : value(delta.value());
        switch (delta.slot()) {
            case "city" -> values.city = value;
            case "category" -> values.category = value;
            case "keyword" -> values.keyword = value;
            case "venue" -> values.venue = value;
            case "minPrice" -> values.minPrice = decimal(value);
            case "maxPrice" -> values.maxPrice = decimal(value);
            case "startTime" -> values.startTime = value;
            case "endTime" -> values.endTime = value;
            case "timeIntent" -> values.timeIntent = value;
            case "sort" -> values.sort = value;
            default -> {
            }
        }
    }

    private BigDecimal decimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private AiSearchContext empty() {
        return new AiSearchContext("", "", "", "", null, null, "", "", "", "");
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }

    public record UpdatedContext(AiSearchContext context, AiSearchSlotUpdate slotUpdate) {
    }

    private static final class Values {
        private String city = "";
        private String category = "";
        private String keyword = "";
        private String venue = "";
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private String startTime = "";
        private String endTime = "";
        private String timeIntent = "";
        private String sort = "";

        private Values() {
        }

        private Values(AiSearchContext context) {
            city = valueOf(context.city());
            category = valueOf(context.category());
            keyword = valueOf(context.keyword());
            venue = valueOf(context.venue());
            minPrice = context.minPrice();
            maxPrice = context.maxPrice();
            startTime = valueOf(context.startTime());
            endTime = valueOf(context.endTime());
            timeIntent = valueOf(context.timeIntent());
            sort = valueOf(context.sort());
        }

        private static String valueOf(String value) {
            return value == null ? "" : value.trim();
        }
    }
}
