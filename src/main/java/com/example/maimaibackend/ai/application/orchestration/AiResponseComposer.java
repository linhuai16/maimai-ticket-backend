package com.example.maimaibackend.ai.application.orchestration;

import com.example.maimaibackend.ai.domain.context.AiSearchResultReference;
import com.example.maimaibackend.ai.domain.context.AiSearchContext;
import com.example.maimaibackend.ai.domain.search.SearchExecutionResult;
import com.example.maimaibackend.ai.domain.intent.AiIntent;
import com.example.maimaibackend.ai.tool.AiTicketToolService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class AiResponseComposer {
    private final ObjectMapper objectMapper;

    public AiResponseComposer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String search(AiTicketToolService.ToolResult result) {
        SearchExecutionResult execution = result == null ? null : result.searchExecutionResult();
        if (execution != null) return searchResponse(execution).text();
        String summary = "";
        try {
            JsonNode content = objectMapper.readTree(result == null ? "{}" : result.content());
            summary = content.path("message").asText("");
        } catch (Exception ignored) {
        }
        return summary == null || summary.isBlank() ? "暂未查询到符合当前条件的演出。" : summary;
    }

    public ComposedResponse searchResponse(SearchExecutionResult result) {
        if (result == null) return new ComposedResponse("暂未查询到符合当前条件的演出。", 0);
        if (result.displayedCount() == 0 && result.matchedTotal() > 0) {
            return new ComposedResponse("当前条件下没有更多演出。", 0);
        }
        String condition = condition(result.finalContext());
        if (result.displayedCount() == 0) {
            return new ComposedResponse("暂未查询到" + condition + "。", 0);
        }
        String countDescription = result.matchedTotal() > result.displayedCount()
                ? "找到" + result.matchedTotal() + "场" + condition + "，先为你展示" + result.displayedCount() + "场"
                : "找到" + result.displayedCount() + "场" + condition;
        return new ComposedResponse(countDescription + "，"
                + sortDescription(result.finalContext().sort()) + "。", result.displayedCount());
    }

    public String business(AiIntent intent, AiTicketToolService.ToolResult result) {
        return business(intent, result, "");
    }

    public String business(AiIntent intent, AiTicketToolService.ToolResult result, String requestedAttribute) {
        try {
            JsonNode content = objectMapper.readTree(result == null ? "{}" : result.content());
            return switch (intent) {
                case PERFORMANCE_DETAIL -> detail(result, content, requestedAttribute);
                case SESSION_QUERY -> sessions(content);
                case TICKET_QUERY -> tickets(content, requestedAttribute);
                case REFUND_QUERY -> refund(content);
                case SEARCH_PERFORMANCE -> search(result);
                case GENERAL_CHAT, ENTITY_QA -> "";
            };
        } catch (Exception ignored) {
            return toolFailure("票务信息");
        }
    }

    public String missingReference() {
        return "当前没有可引用的演出，请先搜索演出。";
    }

    public String missingComparison() {
        return "当前没有可比较的演出，请先查询演出。";
    }

    public String ticketFactUnavailable(AiIntent intent) {
        if (intent == null) return "暂时无法获取当前票务信息，请稍后重试。";
        return switch (intent) {
            case SEARCH_PERFORMANCE -> "暂未查询到符合当前条件的演出。";
            case PERFORMANCE_DETAIL -> "暂时无法获取该演出的详情，请稍后重试。";
            case SESSION_QUERY -> "暂时无法获取该演出的场次，请稍后重试。";
            case TICKET_QUERY -> "暂时无法获取当前票价和库存，请稍后重试。";
            case REFUND_QUERY -> "暂时无法获取当前退款规则，请稍后重试。";
            case GENERAL_CHAT, ENTITY_QA -> "";
        };
    }

    public String refundFilter(List<AiSearchResultReference> matched, int candidateCount) {
        if (matched == null || matched.isEmpty()) return "当前这批搜索结果中暂未找到支持退款的演出。";
        return "找到" + matched.size() + "场支持退款的演出：" + titles(matched) + "。退款以当前项目规则为准。";
    }

    public String availabilityFilter(List<AiSearchResultReference> matched) {
        if (matched == null || matched.isEmpty()) return "当前这批搜索结果中暂未找到有可售库存的演出。";
        return "找到" + matched.size() + "场当前还有可售票档的演出：" + titles(matched) + "。库存可能实时变化。";
    }

    public String toolFailure(String tool) {
        return "暂时无法完成" + tool + "查询，请稍后重试。";
    }

    private String detail(AiTicketToolService.ToolResult result, JsonNode content, String requestedAttribute) {
        String title = content.path("project").path("title").asText("");
        if (title.isBlank() && result != null && result.cards() != null && !result.cards().isEmpty()) {
            title = result.cards().get(0).getTitle();
        }
        if (title.isBlank()) return "当前没有查询到该演出的详情。";
        JsonNode session = content.path("selectedSession");
        String question = requestedAttribute == null ? "" : requestedAttribute;
        if (containsAny(question, "场馆", "在哪", "地址")) {
            String venue = session.path("venueName").asText("");
            String address = session.path("venueAddress").asText("");
            if (!venue.isBlank()) return "《" + title + "》在" + venue + (address.isBlank() ? "。" : "（" + address + "）。");
        }
        String category = content.path("project").path("categoryName").asText("");
        return "《" + title + "》当前项目状态为" + content.path("project").path("projectStatus").asText("可展示")
                + (category.isBlank() ? "。" : "，分类为" + category + "。");
    }

    private String sessions(JsonNode content) {
        JsonNode values = content.path("sessions");
        String title = content.path("project").path("title").asText("");
        int count = values.isArray() ? values.size() : 0;
        String subject = title.isBlank() ? "当前演出" : "《" + title + "》";
        if (count == 0) return subject + "当前没有查询到可用场次。";
        List<String> times = new ArrayList<>();
        for (JsonNode session : values) {
            String formatted = formatDateTime(session.path("startTime").asText(""));
            if (!formatted.isBlank()) times.add(formatted);
            if (times.size() >= 6) break;
        }
        return times.isEmpty() ? subject + "查询到" + count + "个场次，但当前未提供可展示的开演时间。"
                : subject + (count == 1 ? "开演时间是" : "当前场次时间为") + String.join("、", times) + "。";
    }

    private String tickets(JsonNode content, String requestedAttribute) {
        JsonNode skus = content.path("ticketSkus");
        int available = 0;
        BigDecimal minimum = null;
        BigDecimal maximum = null;
        if (skus.isArray()) {
            for (JsonNode sku : skus) {
                String status = sku.path("skuStatus").asText("").toUpperCase();
                if (sku.path("stockAvailable").asInt(0) <= 0 || "OFFLINE".equals(status) || "SOLD_OUT".equals(status)) continue;
                available++;
                try {
                    BigDecimal price = new BigDecimal(sku.path("price").asText(""));
                    if (minimum == null || price.compareTo(minimum) < 0) minimum = price;
                    if (maximum == null || price.compareTo(maximum) > 0) maximum = price;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (available == 0) return "当前场次暂未查询到可售票档。";
        boolean requestMaximum = containsAny(requestedAttribute == null ? "" : requestedAttribute,
                "最高价", "最高票价", "最贵", "最贵票档");
        BigDecimal selected = requestMaximum ? maximum : minimum;
        String priceLabel = requestMaximum ? "最高价" : "最低价";
        return "当前场次有" + available + "个可售票档"
                + (selected == null ? "。" : "，" + priceLabel + selected.stripTrailingZeros().toPlainString() + "元。")
                + "库存可能实时变化。";
    }

    private String refund(JsonNode content) {
        String title = content.path("title").asText("");
        String subject = title.isBlank() ? "当前演出" : "《" + title + "》";
        if (!content.path("found").asBoolean(false)) return subject + "当前未查询到支持退款的规则。";
        List<String> rules = new ArrayList<>();
        JsonNode values = content.path("refundRules");
        if (values.isArray()) {
            for (JsonNode rule : values) {
                String name = rule.path("tagName").asText("");
                String description = rule.path("description").asText("");
                if (!name.isBlank()) rules.add(description.isBlank() ? name : name + "：" + description);
                if (rules.size() >= 3) break;
            }
        }
        return rules.isEmpty() ? subject + "当前支持退款，具体条件以麦麦当前退款规则为准。"
                : subject + "当前退款规则为" + String.join("；", rules) + "。";
    }

    private String formatDateTime(String value) {
        if (value == null || value.isBlank()) return "";
        try {
            return LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern("yyyy年M月d日HH:mm"));
        } catch (RuntimeException ignored) {
            return value.replace('T', ' ');
        }
    }

    private boolean containsAny(String value, String... tokens) {
        for (String token : tokens) if (value.contains(token)) return true;
        return false;
    }

    private String titles(List<AiSearchResultReference> results) {
        List<String> titles = new ArrayList<>();
        for (AiSearchResultReference result : results) {
            if (result != null && result.title() != null && !result.title().isBlank()) titles.add(result.title());
        }
        return titles.isEmpty() ? "符合条件的演出" : String.join("、", titles);
    }

    private String condition(AiSearchContext context) {
        if (context == null) return "符合当前条件的演出";
        StringBuilder value = new StringBuilder();
        if (context.city() != null) value.append(context.city());
        value.append(timeDescription(context.timeIntent()));
        if (context.minPrice() != null && context.maxPrice() != null) {
            value.append(context.minPrice().stripTrailingZeros().toPlainString()).append("至")
                    .append(context.maxPrice().stripTrailingZeros().toPlainString()).append("元");
        } else if (context.maxPrice() != null) {
            value.append(context.maxPrice().stripTrailingZeros().toPlainString()).append("元以内");
        } else if (context.minPrice() != null) {
            value.append(context.minPrice().stripTrailingZeros().toPlainString()).append("元以上");
        }
        value.append(context.category() == null || context.category().isBlank() ? "演出" : context.category());
        if (context.venue() != null && !context.venue().isBlank()) value.append("（场馆：").append(context.venue()).append("）");
        if (context.keyword() != null && !context.keyword().isBlank()) value.append("（关键词：").append(context.keyword()).append("）");
        return value.toString();
    }

    private String timeDescription(String intent) {
        String value = intent == null ? "" : intent;
        if (value.equals("NEXT_7_DAYS")) return "未来一周";
        if (value.equals("NEXT_30_DAYS") || value.equals("RECENT")) return "未来30天";
        if (value.equals("TODAY")) return "今天";
        if (value.equals("TOMORROW")) return "明天";
        if (value.equals("DAY_AFTER_TOMORROW")) return "后天";
        if (value.equals("THIS_WEEK")) return "本周";
        if (value.equals("WEEKEND")) return "本周末";
        if (value.equals("THIS_MONTH")) return "本月";
        if (value.equals("PAST")) return "已结束";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("NEXT_(\\d+)_(DAYS|WEEKS|MONTHS)").matcher(value);
        if (matcher.matches()) {
            String unit = switch (matcher.group(2)) {
                case "WEEKS" -> "周";
                case "MONTHS" -> "个月";
                default -> "天";
            };
            return "未来" + matcher.group(1) + unit;
        }
        return value.isBlank() ? "" : "未来";
    }

    private String sortDescription(String sort) {
        return switch (sort == null ? "" : sort) {
            case "PRICE_ASC" -> "按价格从低到高排列";
            case "PRICE_DESC" -> "按价格从高到低排列";
            case "HOT" -> "按热度稳定排列";
            case "NEW" -> "按最新项目稳定排列";
            default -> "按开演时间由近到远排列";
        };
    }

    public record ComposedResponse(String text, int displayedCount) {
    }
}
