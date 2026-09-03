package com.example.maimaibackend.ai.tool;

import com.example.maimaibackend.ai.domain.search.AiSearchSemanticResolver;
import com.example.maimaibackend.ai.domain.search.SearchExecutionResult;
import com.example.maimaibackend.ai.domain.context.AiSearchContext;
import com.example.maimaibackend.service.PerformanceService;
import com.example.maimaibackend.vo.performance.CategoryPageVO;
import com.example.maimaibackend.vo.performance.PerformanceCardVO;
import com.example.maimaibackend.vo.performance.PerformanceDetailVO;
import com.example.maimaibackend.vo.performance.ProjectDetailVO;
import com.example.maimaibackend.vo.performance.SearchResultPageVO;
import com.example.maimaibackend.vo.performance.ServiceTagVO;
import com.example.maimaibackend.vo.performance.SessionItemVO;
import com.example.maimaibackend.vo.performance.TicketSelectPageVO;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiTicketToolService {
    private final PerformanceService performanceService;
    private final ObjectMapper objectMapper;

    public AiTicketToolService(PerformanceService performanceService, ObjectMapper objectMapper) {
        this.performanceService = performanceService;
        this.objectMapper = objectMapper;
    }

    public ToolResult execute(String name, JsonNode arguments, Long contextProjectId, Long contextSessionId) {
        return switch (name) {
            case "searchPerformances" -> search(arguments);
            case "getPerformanceDetail" -> {
                Long projectId = requiredProjectId(arguments, contextProjectId);
                yield detail(projectId);
            }
            case "getSessions" -> {
                Long projectId = requiredProjectId(arguments, contextProjectId);
                yield sessions(projectId);
            }
            case "getTicketSkus" -> {
                Long projectId = requiredProjectId(arguments, contextProjectId);
                Long sessionId = optionalLong(arguments, "sessionId", contextSessionId);
                yield ticketSkus(projectId, sessionId);
            }
            case "getRefundRule" -> {
                Long projectId = requiredProjectId(arguments, contextProjectId);
                yield refundRule(projectId);
            }
            default -> new ToolResult(write(Map.of("found", false, "message", "不支持的工具: " + name)), List.of(), null, null);
        };
    }

    private ToolResult search(JsonNode arguments) {
        String keyword = text(arguments, "keyword");
        String city = text(arguments, "city");
        String category = text(arguments, "category");
        String venue = text(arguments, "venue");
        String timeIntent = text(arguments, "timeIntent").toUpperCase(Locale.ROOT);
        String sort = normalizeAiSort(text(arguments, "sort"));
        LocalDateTime startTime = dateTime(arguments, "startTime", false);
        LocalDateTime endTime = dateTime(arguments, "endTime", true);
        BigDecimal minPrice = decimal(arguments, "minPrice");
        BigDecimal maxPrice = decimal(arguments, "maxPrice");
        int resultLimit = Math.max(1, Math.min(8, integer(arguments, "limit", 6)));
        Set<Long> excludedProjectIds = longSet(arguments.path("excludeProjectIds"));
        if (!"PAST".equals(timeIntent) && startTime == null) {
            startTime = LocalDateTime.now(AiSearchSemanticResolver.BUSINESS_ZONE);
        }

        String upstreamSort = "NEW".equals(sort) ? "NEW" : "HOT";
        List<PerformanceCardVO> candidates = city.isBlank()
                ? searchPages(city, keyword.isBlank() ? (venue.isBlank() ? category : venue) : keyword)
                : categoryPages(city, upstreamSort);
        List<PerformanceCardVO> filtered = new ArrayList<>();
        Set<Long> projectIds = new LinkedHashSet<>();
        for (PerformanceCardVO item : candidates) {
            if (item == null || item.getProjectId() == null || !projectIds.add(item.getProjectId())) continue;
            if (!matches(item, keyword, city, category, venue, startTime, endTime, minPrice, maxPrice)) continue;
            filtered.add(item);
        }
        filtered.sort(comparator(sort, timeIntent));
        List<PerformanceCardVO> available = filtered.stream()
                .filter(item -> !excludedProjectIds.contains(item.getProjectId())).toList();
        List<PerformanceCardVO> matched = available.size() <= resultLimit
                ? new ArrayList<>(available) : new ArrayList<>(available.subList(0, resultLimit));
        List<Long> resultIds = matched.stream().map(PerformanceCardVO::getProjectId).toList();
        boolean hasMore = available.size() > matched.size();
        AiSearchContext finalContext = new AiSearchContext(city, category, keyword, venue, minPrice, maxPrice,
                startTime == null ? "" : startTime.toString(), endTime == null ? "" : endTime.toString(),
                timeIntent, sort);
        SearchExecutionResult execution = new SearchExecutionResult(finalContext, filtered.size(), matched.size(),
                matched, resultIds, hasMore, excludedProjectIds.size() + matched.size());

        Map<String, Object> data = new LinkedHashMap<>();
        boolean continuation = !excludedProjectIds.isEmpty();
        data.put("found", !matched.isEmpty());
        data.put("message", matched.isEmpty()
                ? continuation && !filtered.isEmpty()
                    ? "当前条件下没有更多演出。"
                    : "暂时没有查询到" + searchConditionSummary(finalContext) + "。"
                : "找到" + execution.displayedCount() + "场" + searchConditionSummary(finalContext)
                    + "，" + sortDescription(sort) + "。");
        data.put("suggestions", matched.isEmpty()
                ? relaxationSuggestions(keyword, category, timeIntent, minPrice, maxPrice) : List.of());
        data.put("items", execution.items());
        data.put("performances", execution.items());
        data.put("matchedTotal", execution.matchedTotal());
        data.put("displayedCount", execution.displayedCount());
        data.put("resultCount", execution.matchedTotal());
        data.put("hasMore", execution.hasMore());
        data.put("nextCursor", execution.nextCursor());
        data.put("resultIds", execution.resultIds());
        data.put("finalContext", execution.finalContext());
        data.put("canonicalVenue", matched.isEmpty() || venue.isBlank() ? "" : matched.get(0).getVenueName());
        Long projectId = matched.isEmpty() ? null : matched.get(0).getProjectId();
        Long sessionId = matched.isEmpty() ? null : matched.get(0).getSessionId();
        return new ToolResult(write(data), execution.items(), projectId, sessionId, execution);
    }

    private String searchConditionSummary(AiSearchContext context) {
        StringBuilder summary = new StringBuilder();
        if (!context.city().isBlank()) summary.append(context.city());
        if (!context.timeIntent().isBlank()) summary.append(timeDescription(context.timeIntent()));
        if (context.minPrice() != null || context.maxPrice() != null) {
            if (context.minPrice() != null && context.maxPrice() != null) summary.append(formatPrice(context.minPrice())).append("至").append(formatPrice(context.maxPrice())).append("元");
            else if (context.maxPrice() != null) summary.append(formatPrice(context.maxPrice())).append("元以内");
            else summary.append(formatPrice(context.minPrice())).append("元以上");
        }
        if (!context.category().isBlank()) summary.append(context.category());
        else summary.append("演出");
        if (!context.venue().isBlank()) summary.append("（场馆：").append(context.venue()).append("）");
        if (!context.keyword().isBlank()) summary.append("（关键词：").append(context.keyword()).append("）");
        return summary.toString();
    }

    private String timeDescription(String timeIntent) {
        return switch (timeIntent) {
            case "RECENT", "NEXT_30_DAYS" -> "未来30天";
            case "NEXT_7_DAYS" -> "未来一周";
            case "TODAY" -> "今天";
            case "TOMORROW" -> "明天";
            case "THIS_WEEK" -> "本周";
            case "WEEKEND" -> "本周末";
            case "THIS_MONTH" -> "本月";
            case "PAST" -> "已结束";
            case "EXPLICIT_DATE" -> "指定日期";
            default -> dynamicTimeDescription(timeIntent);
        };
    }

    private String dynamicTimeDescription(String timeIntent) {
        Matcher days = Pattern.compile("NEXT_(\\d+)_DAYS").matcher(timeIntent);
        if (days.matches()) return "未来" + days.group(1) + "天";
        Matcher weeks = Pattern.compile("NEXT_(\\d+)_WEEKS").matcher(timeIntent);
        if (weeks.matches()) return "未来" + weeks.group(1) + "周";
        Matcher months = Pattern.compile("NEXT_(\\d+)_MONTHS").matcher(timeIntent);
        if (months.matches()) return "未来" + months.group(1) + "个月";
        return "未来";
    }

    private String sortDescription(String sort) {
        return switch (sort) {
            case "PRICE_ASC" -> "按价格从低到高排列";
            case "PRICE_DESC" -> "按价格从高到低排列";
            case "HOT" -> "按热度稳定排列";
            case "NEW" -> "按最新项目稳定排列";
            default -> "按开演时间由近到远排列";
        };
    }

    private List<String> relaxationSuggestions(String keyword, String category, String timeIntent,
                                               BigDecimal minPrice, BigDecimal maxPrice) {
        List<String> suggestions = new ArrayList<>();
        if (minPrice != null || maxPrice != null) suggestions.add("不限价格");
        if (!keyword.isBlank() && suggestions.size() < 3) suggestions.add("不限关键词");
        if (!category.isBlank() && suggestions.size() < 3) suggestions.add("不限分类");
        if (!"FUTURE".equals(timeIntent) && !"RECENT".equals(timeIntent) && suggestions.size() < 3) {
            suggestions.add("只看还没开演的");
        }
        return suggestions;
    }

    private String formatPrice(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private List<PerformanceCardVO> searchPages(String city, String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        List<PerformanceCardVO> result = new ArrayList<>();
        for (int offset = 0; offset < 200; offset += 50) {
            SearchResultPageVO page = performanceService.searchPerformances(city, keyword, 50, offset);
            if (page.getPerformances() != null) result.addAll(page.getPerformances());
            if (page.getTotal() == null || offset + 50 >= page.getTotal()) break;
        }
        return result;
    }

    private List<PerformanceCardVO> categoryPages(String city, String upstreamSort) {
        List<PerformanceCardVO> result = new ArrayList<>();
        for (int offset = 0; offset < 200; offset += 50) {
            CategoryPageVO page = performanceService.getCategoryPerformanceList(city, null, upstreamSort, null, 50, offset);
            if (page.getPerformances() != null) result.addAll(page.getPerformances());
            if (page.getTotal() == null || offset + 50 >= page.getTotal()) break;
        }
        return result;
    }

    private boolean matches(PerformanceCardVO item, String keyword, String city, String category, String venue,
                            LocalDateTime startTime, LocalDateTime endTime, BigDecimal minPrice, BigDecimal maxPrice) {
        if (!city.isBlank() && !sameCity(item.getCityName(), city)) return false;
        if (!category.isBlank() && !sameEntity(item.getCategoryName(), category)) return false;
        if (!venue.isBlank() && !sameEntity(item.getVenueName(), venue)) return false;
        if (!keyword.isBlank() && !containsAny(keyword, item.getTitle(), item.getCategoryName(), item.getCityName(),
                item.getStationName(), item.getVenueName())) return false;
        if (startTime != null && (item.getStartTime() == null || item.getStartTime().isBefore(startTime))) return false;
        if (endTime != null && (item.getStartTime() == null || item.getStartTime().isAfter(endTime))) return false;
        if (minPrice != null && (item.getMaxPrice() == null || item.getMaxPrice().compareTo(minPrice) < 0)) return false;
        return maxPrice == null || item.getMinPrice() != null && item.getMinPrice().compareTo(maxPrice) <= 0;
    }

    private Comparator<PerformanceCardVO> comparator(String sort, String timeIntent) {
        return (left, right) -> {
            int value;
            if ("PRICE_ASC".equals(sort)) {
                value = compareDecimal(left.getMinPrice(), right.getMinPrice(), true);
                if (value != 0) return value;
                value = compareTime(left.getStartTime(), right.getStartTime(), true);
                if (value != 0) return value;
                return compareLong(left.getProjectId(), right.getProjectId(), true);
            }
            if ("PRICE_DESC".equals(sort)) {
                value = compareDecimal(left.getMinPrice(), right.getMinPrice(), false);
                if (value != 0) return value;
                value = compareTime(left.getStartTime(), right.getStartTime(), true);
                if (value != 0) return value;
                return compareLong(left.getProjectId(), right.getProjectId(), true);
            }
            if ("HOT".equals(sort)) {
                value = compareDecimal(left.getHotScore(), right.getHotScore(), false);
                if (value != 0) return value;
                value = compareTime(left.getStartTime(), right.getStartTime(), true);
                if (value != 0) return value;
                return compareLong(left.getProjectId(), right.getProjectId(), true);
            }
            if ("NEW".equals(sort)) {
                value = compareLong(left.getProjectId(), right.getProjectId(), false);
                if (value != 0) return value;
                return compareTime(left.getStartTime(), right.getStartTime(), true);
            }
            boolean ascending = !"PAST".equals(timeIntent);
            value = compareTime(left.getStartTime(), right.getStartTime(), ascending);
            if (value != 0) return value;
            return compareLong(left.getProjectId(), right.getProjectId(), true);
        };
    }

    private int compareDecimal(BigDecimal left, BigDecimal right, boolean ascending) {
        if (left == null && right == null) return 0;
        if (left == null) return 1;
        if (right == null) return -1;
        int value = left.compareTo(right);
        return ascending ? value : -value;
    }

    private int compareTime(LocalDateTime left, LocalDateTime right, boolean ascending) {
        if (left == null && right == null) return 0;
        if (left == null) return 1;
        if (right == null) return -1;
        int value = left.compareTo(right);
        return ascending ? value : -value;
    }

    private int compareLong(Long left, Long right, boolean ascending) {
        if (left == null && right == null) return 0;
        if (left == null) return 1;
        if (right == null) return -1;
        int value = left.compareTo(right);
        return ascending ? value : -value;
    }

    private String normalizeAiSort(String value) {
        String sort = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return switch (sort) {
            case "PRICE_ASC", "PRICE_DESC", "HOT", "NEW" -> sort;
            default -> "NEAREST";
        };
    }

    private ToolResult detail(Long projectId) {
        PerformanceDetailVO detail = performanceService.getPerformanceDetail(projectId, null, null, null);
        PerformanceCardVO card = toCard(detail);
        return new ToolResult(write(detail), card == null ? List.of() : List.of(card), projectId,
                detail.getSelectedSession() == null ? null : detail.getSelectedSession().getSessionId());
    }

    private ToolResult sessions(Long projectId) {
        PerformanceDetailVO detail = performanceService.getPerformanceDetail(projectId, null, null, null);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("project", detail.getProject());
        data.put("sessions", detail.getSessions());
        Long sessionId = detail.getSelectedSession() == null ? null : detail.getSelectedSession().getSessionId();
        return new ToolResult(write(data), List.of(), projectId, sessionId);
    }

    private ToolResult ticketSkus(Long projectId, Long sessionId) {
        Long selectedSessionId = sessionId;
        if (selectedSessionId == null) {
            PerformanceDetailVO detail = performanceService.getPerformanceDetail(projectId, null, null, null);
            selectedSessionId = detail.getSelectedSession().getSessionId();
        }
        TicketSelectPageVO data = performanceService.getTicketSelect(projectId, selectedSessionId);
        return new ToolResult(write(data), List.of(), projectId, selectedSessionId);
    }

    private ToolResult refundRule(Long projectId) {
        PerformanceDetailVO detail = performanceService.getPerformanceDetail(projectId, null, null, null);
        List<ServiceTagVO> refundRules = new ArrayList<>();
        boolean refundable = false;
        if (detail.getServiceTags() != null) {
            for (ServiceTagVO tag : detail.getServiceTags()) {
                if (tag == null || tag.getTagName() == null || !tag.getTagName().contains("退")) continue;
                refundRules.add(tag);
                if (!tag.getTagName().contains("不可退") && !tag.getTagName().contains("不支持退")) refundable = true;
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("projectId", projectId);
        data.put("title", detail.getProject().getTitle());
        data.put("found", refundable);
        data.put("refundRules", refundRules);
        data.put("message", refundRules.isEmpty() ? "当前演出未配置可展示的退款规则" : "退款规则以麦麦当前项目配置为准");
        return new ToolResult(write(data), List.of(), projectId,
                detail.getSelectedSession() == null ? null : detail.getSelectedSession().getSessionId());
    }

    private PerformanceCardVO toCard(PerformanceDetailVO detail) {
        ProjectDetailVO project = detail.getProject();
        SessionItemVO session = detail.getSelectedSession();
        if (project == null || session == null) return null;
        PerformanceCardVO card = new PerformanceCardVO();
        card.setProjectId(project.getProjectId());
        card.setSessionId(session.getSessionId());
        card.setCategoryId(project.getCategoryId());
        card.setCategoryName(project.getCategoryName());
        card.setTitle(project.getTitle());
        card.setPosterUrl(project.getPosterUrl());
        card.setCityName(session.getCityName());
        card.setStationName(session.getStationName());
        card.setVenueName(session.getVenueName());
        card.setStartTime(session.getStartTime());
        card.setMinPrice(session.getMinPrice());
        card.setMaxPrice(session.getMaxPrice());
        card.setHotScore(project.getHotScore());
        card.setWantCount(project.getWantCount());
        card.setSessionStatus(session.getSessionStatus());
        return card;
    }

    private Long requiredProjectId(JsonNode arguments, Long contextProjectId) {
        Long value = optionalLong(arguments, "projectId", contextProjectId);
        if (value == null) throw new IllegalArgumentException("当前会话没有可识别的演出，请先搜索或指定演出");
        return value;
    }

    private Long optionalLong(JsonNode node, String name, Long fallback) {
        JsonNode value = node.path(name);
        if (value.isNumber()) return value.asLong();
        if (value.isTextual() && !value.asText().isBlank()) {
            try { return Long.parseLong(value.asText().trim()); } catch (NumberFormatException ignored) { return fallback; }
        }
        return fallback;
    }

    private String text(JsonNode node, String name) {
        JsonNode value = node.path(name);
        return value.isTextual() ? value.asText().trim() : "";
    }

    private int integer(JsonNode node, String name, int fallback) {
        JsonNode value = node.path(name);
        return value.isNumber() ? value.asInt() : fallback;
    }

    private BigDecimal decimal(JsonNode node, String name) {
        JsonNode value = node.path(name);
        if (value.isNumber() || value.isTextual() && !value.asText().isBlank()) {
            try { return new BigDecimal(value.asText()); } catch (NumberFormatException ignored) { return null; }
        }
        return null;
    }

    private LocalDateTime dateTime(JsonNode node, String name, boolean endOfDay) {
        String value = text(node, name);
        if (value.isBlank()) return null;
        try { return LocalDateTime.parse(value); } catch (DateTimeParseException ignored) {}
        try {
            LocalDate date = LocalDate.parse(value);
            return endOfDay ? date.plusDays(1).atStartOfDay().minusNanos(1) : date.atStartOfDay();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private boolean contains(String value, String expected) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
    }

    private boolean sameEntity(String value, String expected) {
        String left = normalizeEntity(value);
        String right = normalizeEntity(expected);
        return !left.isBlank() && left.equals(right);
    }

    private boolean sameCity(String value, String expected) {
        String left = normalizeEntity(value).replaceFirst("市$", "");
        String right = normalizeEntity(expected).replaceFirst("市$", "");
        return !left.isBlank() && left.equals(right);
    }

    private String normalizeEntity(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[\\s··()（）_-]+", "");
    }

    private Set<Long> longSet(JsonNode node) {
        Set<Long> values = new LinkedHashSet<>();
        if (node != null && node.isArray()) {
            for (JsonNode value : node) if (value.isIntegralNumber()) values.add(value.asLong());
        }
        return values;
    }

    private boolean containsAny(String keyword, String... values) {
        for (String value : values) if (contains(value, keyword)) return true;
        return false;
    }

    private String write(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception ex) { throw new IllegalStateException("票务工具结果序列化失败"); }
    }

    public record ToolResult(String content, List<PerformanceCardVO> cards, Long projectId, Long sessionId,
                             SearchExecutionResult searchExecutionResult) {
        public ToolResult(String content, List<PerformanceCardVO> cards, Long projectId, Long sessionId) {
            this(content, cards, projectId, sessionId, null);
        }
    }
}
