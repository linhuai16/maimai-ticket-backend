package com.example.maimaibackend.ai.domain.search;

import com.example.maimaibackend.ai.domain.action.AiConversationAction;
import com.example.maimaibackend.ai.domain.context.AiActiveSlot;
import com.example.maimaibackend.ai.domain.context.AiContextUpdater;
import com.example.maimaibackend.ai.domain.context.AiSearchContext;
import com.example.maimaibackend.ai.domain.context.AiSearchResultReference;
import com.example.maimaibackend.ai.domain.context.AiSearchResultState;
import com.example.maimaibackend.ai.domain.context.AiSearchSlotUpdate;
import com.example.maimaibackend.ai.domain.context.AiSlotOperation;
import com.example.maimaibackend.ai.domain.context.AiSlotDelta;
import com.example.maimaibackend.ai.domain.entity.AiResolvedVenue;
import com.example.maimaibackend.ai.domain.intent.AiIntent;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AiSearchSemanticResolver {
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Pattern MAX_PRICE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*元?\\s*(?:以内|以下|以下的|之内)");
    private static final Pattern MIN_PRICE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*元?\\s*(?:以上|起)");
    private static final Pattern RANGE_PRICE_FULL_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:元)?\\s*(?:~|～|—|-|到|至)\\s*(\\d+(?:\\.\\d+)?)\\s*元?");
    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("(20\\d{2})[-/.年](\\d{1,2})[-/.月](\\d{1,2})日?");
    private static final Pattern MONTH_DAY_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,2})月(\\d{1,2})日?");
    private static final Pattern SLASH_MONTH_DAY_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,2})/(\\d{1,2})(?!\\d)");
    private static final Pattern QUOTED_ENTITY_PATTERN = Pattern.compile("《([^》]{2,40})》");
    private static final Pattern POSITIVE_KEYWORD_PATTERN = Pattern.compile(
            "(?:有|找|看|查|想看)([\\p{IsHan}A-Za-z0-9·]{2,30}?)(?:的演出|的项目|什么时候演|何时演|在哪演|有演出)");
    private static final Pattern DIRECT_ENTITY_PATTERN = Pattern.compile(
            "^([\\p{IsHan}A-Za-z0-9·]{2,30}?)(?:什么时候演|何时演|在哪演|有演出)");
    private static final Pattern PREFIX_ENTITY_PATTERN = Pattern.compile(
            "^([\\p{IsHan}A-Za-z0-9·]{2,30}?)(?:最近|近期|未来|接下来|这周|本周|周末).{0,8}(?:演出|项目|活动)");
    private static final String[] CATEGORIES = {
            "演唱会", "音乐会", "音乐节", "话剧", "歌剧", "舞蹈", "芭蕾", "戏曲", "相声", "脱口秀", "儿童剧", "展览", "体育"
    };
    private static final String[] COMMON_CITIES = {
            "北京", "上海", "广州", "深圳", "杭州", "南京", "苏州", "成都", "重庆", "武汉", "西安", "天津", "长沙", "郑州", "青岛", "厦门", "福州", "济南", "合肥", "宁波", "无锡", "佛山", "东莞", "昆明", "南昌", "南宁", "贵阳", "太原", "沈阳", "大连", "哈尔滨", "长春", "石家庄", "珠海", "海口", "三亚", "呼和浩特", "拉萨", "兰州", "西宁", "银川", "乌鲁木齐"
    };
    private static final String[] GENERIC_KEYWORD_PARTS = {
            "随便看看", "最近一周", "未来一周", "接下来一周", "这个周末", "本周末", "价格从低到高", "价格从高到低",
            "好看的", "好看", "不错的", "不错", "推荐一下", "推荐", "演出", "活动", "门票", "最近", "近期", "一周",
            "周末", "今天", "明天", "本周", "本月", "便宜的", "便宜", "看看", "看一下", "看下", "有哪些", "有什么",
            "想看", "帮我", "找找", "找一下", "价格", "以内", "以下", "以上", "热门", "最新", "最便宜", "随便"
    };

    private final ObjectMapper objectMapper;
    private final AiSemanticParser semanticParser;
    private final AiContextUpdater contextUpdater;

    public AiSearchSemanticResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.semanticParser = new AiSemanticParser();
        this.contextUpdater = new AiContextUpdater();
    }

    public ResolvedSearch resolveSearch(String userText, JsonNode candidateArguments,
                                        AiSearchContext previous, String currentCity,
                                        AiConversationAction action, AiSearchResultState resultState,
                                        AiResolvedVenue resolvedVenue) {
        String candidateCity = candidateArguments == null ? "" : string(candidateArguments, "city");
        String legacyResolvedCity = explicitCandidateCity(normalize(userText), candidateCity);
        return resolveSearch(userText, candidateArguments, previous, currentCity, action, resultState,
                resolvedVenue, legacyResolvedCity, AiActiveSlot.NONE);
    }

    public ResolvedSearch resolveSearch(String userText, JsonNode candidateArguments,
                                        AiSearchContext previous, String currentCity,
                                        AiConversationAction action, AiSearchResultState resultState,
                                        AiResolvedVenue resolvedVenue, String resolvedCity,
                                        AiActiveSlot activeSlot) {
        String text = normalize(userText);
        ObjectNode candidate = candidateArguments != null && candidateArguments.isObject()
                ? (ObjectNode) candidateArguments.deepCopy() : objectMapper.createObjectNode();
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE).withSecond(0).withNano(0);
        if (action == AiConversationAction.CONTINUE_RESULTS && previous != null) {
            return continuation(previous, resultState, AiActiveSlot.RESULT_REFERENCE);
        }
        AiResolvedVenue verifiedVenue = resolvedVenue == null ? AiResolvedVenue.empty() : resolvedVenue;
        AiSemanticParseResult parsed = semanticParser.parse(text, candidate, previous,
                activeSlot == null ? AiActiveSlot.NONE : activeSlot, action, verifiedVenue, resolvedCity, now);
        List<AiSlotDelta> deltas = new ArrayList<>(parsed.slotDeltas());
        String cityForKeyword = parsed.recognizedSlots().getOrDefault(AiActiveSlot.CITY.name(),
                previous == null ? value(currentCity) : value(previous.city()));
        String categoryForKeyword = parsed.recognizedSlots().getOrDefault(AiActiveSlot.CATEGORY.name(),
                previous == null ? "" : value(previous.category()));
        String keyword = explicitCandidateKeyword(removeVenueExpression(text, verifiedVenue), candidate,
                cityForKeyword, categoryForKeyword, verifiedVenue.canonicalName(), verifiedVenue.matchedText(), action);
        if (!keyword.isBlank()) deltas.add(AiSlotDelta.replace("keyword", keyword));
        if (containsAny(text, "不限关键词", "关键词不限", "随便看看") || isAllPerformancesRequest(text)) {
            deltas.removeIf(delta -> "keyword".equals(delta.slot()));
            deltas.add(AiSlotDelta.clear("keyword"));
        }
        AiContextUpdater.UpdatedContext updated = contextUpdater.apply(previous, currentCity, action, deltas, now);
        AiSearchContext context = updated.context();
        int limit = resolveLimit(text);

        ObjectNode arguments = objectMapper.createObjectNode();
        putText(arguments, "keyword", context.keyword());
        putText(arguments, "city", context.city());
        putText(arguments, "category", context.category());
        putText(arguments, "venue", context.venue());
        putText(arguments, "startTime", context.startTime());
        putText(arguments, "endTime", context.endTime());
        if (context.minPrice() != null) arguments.put("minPrice", context.minPrice());
        if (context.maxPrice() != null) arguments.put("maxPrice", context.maxPrice());
        putText(arguments, "timeIntent", context.timeIntent());
        putText(arguments, "sort", context.sort());
        arguments.put("limit", limit);

        AiActiveSlot nextActiveSlot = parsed.activeSlotCandidate() == AiActiveSlot.NONE
                ? activeSlot == null ? AiActiveSlot.NONE : activeSlot : parsed.activeSlotCandidate();
        return new ResolvedSearch(arguments, context, updated.slotUpdate(), nextActiveSlot, parsed);
    }

    public AiConversationAction resolveAction(AiIntent intent, String userText,
                                              AiSearchContext previous,
                                              AiSearchResultState resultState) {
        AiSemanticParseResult semantic = parseSemantic(userText, null, previous, AiActiveSlot.NONE,
                AiConversationAction.REFINE_SEARCH, AiResolvedVenue.empty(), "");
        return resolveAction(intent, userText, previous, resultState, semantic);
    }

    public AiConversationAction resolveAction(AiIntent intent, String userText,
                                              AiSearchContext previous,
                                              AiSearchResultState resultState,
                                              AiSemanticParseResult semantic) {
        String text = normalize(userText);
        AiConversationAction action;
        if (intent == AiIntent.GENERAL_CHAT || intent == AiIntent.ENTITY_QA) action = AiConversationAction.GENERAL_CHAT;
        else if (intent == AiIntent.PERFORMANCE_DETAIL) action = containsOrdinalReference(text)
                ? AiConversationAction.SELECT_RESULT : AiConversationAction.QUERY_DETAIL;
        else if (intent == AiIntent.SESSION_QUERY) action = isComparison(text)
                ? AiConversationAction.COMPARE_RESULTS : AiConversationAction.QUERY_SESSION;
        else if (intent == AiIntent.TICKET_QUERY) action = isComparison(text)
                ? AiConversationAction.COMPARE_RESULTS : AiConversationAction.QUERY_TICKET;
        else if (intent == AiIntent.REFUND_QUERY) action = AiConversationAction.QUERY_REFUND;
        else if (isAllPerformancesRequest(text)) action = AiConversationAction.BROADEN_SEARCH;
        else if (isContinueSemantic(text, semantic) && hasResultState(resultState)) action = AiConversationAction.CONTINUE_RESULTS;
        else if (isContinueSemantic(text, semantic)) action = AiConversationAction.BROADEN_SEARCH;
        else if (isCompleteSearchRequest(text)) action = AiConversationAction.NEW_SEARCH;
        else if (!hasSearchContext(previous)) action = AiConversationAction.NEW_SEARCH;
        else if (containsAny(text, "清空条件", "重置筛选", "重新开始")) action = AiConversationAction.CLEAR_FILTER;
        else if (containsAny(text, "放宽", "不限", "全部", "所有", "随便")) action = AiConversationAction.BROADEN_SEARCH;
        else if (semantic.recognizes(AiActiveSlot.VENUE)) action = AiConversationAction.CHANGE_VENUE;
        else if (semantic.recognizes(AiActiveSlot.CITY)) action = AiConversationAction.CHANGE_CITY;
        else if (semantic.recognizes(AiActiveSlot.TIME)) action = AiConversationAction.CHANGE_TIME;
        else if (semantic.recognizes(AiActiveSlot.CATEGORY)) action = AiConversationAction.CHANGE_CATEGORY;
        else if (semantic.recognizes(AiActiveSlot.SORT)) action = AiConversationAction.CHANGE_SORT;
        else if (semantic.recognizes(AiActiveSlot.PRICE)) action = AiConversationAction.CHANGE_PRICE;
        else if (containsVenueExpression(text)) action = AiConversationAction.CHANGE_VENUE;
        else if (containsCity(text)) action = AiConversationAction.CHANGE_CITY;
        else action = AiConversationAction.REFINE_SEARCH;
        return action;
    }

    public boolean cannotBroadenFutureTime(String userText, AiSearchContext context) {
        return context != null
                && "FUTURE".equals(value(context.timeIntent()).toUpperCase(Locale.ROOT))
                && containsAny(normalize(userText), "放宽时间", "扩大时间", "时间范围放宽");
    }

    public AiIntent detectIntent(String userText, AiSearchContext previous,
                                 List<AiSearchResultReference> lastSearchResults,
                                 Long selectedProjectId, Long selectedSessionId) {
        AiSemanticParseResult semantic = parseSemantic(userText, null, previous, AiActiveSlot.NONE,
                AiConversationAction.REFINE_SEARCH, AiResolvedVenue.empty(), "");
        return detectIntent(userText, previous, lastSearchResults, selectedProjectId, selectedSessionId,
                semantic);
    }

    public AiIntent detectIntent(String userText, AiSearchContext previous,
                                 List<AiSearchResultReference> lastSearchResults,
                                 Long selectedProjectId, Long selectedSessionId,
                                 AiSemanticParseResult semantic) {
        String text = normalize(userText);
        if (text.isBlank() || isTimeKnowledgeQuestion(text)) return AiIntent.GENERAL_CHAT;
        if (isEntityKnowledgeQuestion(text)) return AiIntent.ENTITY_QA;
        if (isGeneralQuestion(text)) return AiIntent.GENERAL_CHAT;
        if (containsAny(text, "退款", "退票", "能退", "可退", "不可退", "退款规则")) return AiIntent.REFUND_QUERY;
        if (isPriceSelection(text)) return AiIntent.TICKET_QUERY;
        if (isSessionSelection(text)) return AiIntent.SESSION_QUERY;
        if (isSessionAttributeQuery(text)) return AiIntent.SESSION_QUERY;
        if (containsAny(text, "票档", "库存", "还有票", "有票吗", "有余票", "没票", "售罄", "余票", "多少钱", "最低价", "价格最低",
                "哪个便宜", "哪个更便宜", "哪个更贵")) {
            return AiIntent.TICKET_QUERY;
        }
        boolean hasReference = selectedProjectId != null || selectedSessionId != null
                || lastSearchResults != null && !lastSearchResults.isEmpty();
        if (hasReference && text.contains("价格") && !containsAny(text, "按价格", "价格从", "不限价格", "价格不限")) {
            return AiIntent.TICKET_QUERY;
        }
        boolean hasSearchContext = hasSearchContext(previous) || lastSearchResults != null && !lastSearchResults.isEmpty();
        if (hasSearchContext && isSearchAdjustment(text)) return AiIntent.SEARCH_PERFORMANCE;
        if (hasSearchContext && containsAny(text, "不限场馆", "场馆不限", "不限场地", "不限时间", "时间不限", "放宽时间", "扩大时间")) {
            return AiIntent.SEARCH_PERFORMANCE;
        }
        if (containsAny(text, "详情", "项目介绍", "演出介绍", "场馆", "在哪里演", "在哪演", "地址在哪")) {
            return AiIntent.PERFORMANCE_DETAIL;
        }
        if (hasSearchContext && isContinueSemantic(text, semantic)) return AiIntent.SEARCH_PERFORMANCE;
        if (hasSearchContext && semantic.activeSlotCandidate() != AiActiveSlot.NONE
                && semantic.activeSlotCandidate() != AiActiveSlot.RESULT_REFERENCE) return AiIntent.SEARCH_PERFORMANCE;
        if (hasSearchContext && containsCity(text)) return AiIntent.SEARCH_PERFORMANCE;
        if (hasSearchContext && (containsCategory(text) || containsAny(text, "换", "改成", "不限价格", "价格不限", "不限分类",
                "不限类型", "不限关键词", "关键词不限", "最近一周", "未来一周", "这个周末", "本周末", "按价格", "最便宜",
                "最贵", "热门", "最新", "还没开演", "未开演", "放宽时间", "扩大时间"))) {
            return AiIntent.SEARCH_PERFORMANCE;
        }
        boolean performanceSubject = containsCategory(text) || containsAny(text, "演出", "活动", "门票");
        boolean discoveryRequest = isDiscoveryRequest(text);
        boolean structuredSearchSubject = semantic != null && (semantic.recognizes(AiActiveSlot.CITY)
                || semantic.recognizes(AiActiveSlot.TIME) || semantic.recognizes(AiActiveSlot.PRICE)
                || semantic.recognizes(AiActiveSlot.VENUE) || semantic.recognizes(AiActiveSlot.CATEGORY));
        if (performanceSubject && structuredSearchSubject) return AiIntent.SEARCH_PERFORMANCE;
        if (discoveryRequest && (performanceSubject || structuredSearchSubject)) return AiIntent.SEARCH_PERFORMANCE;
        if (hasSearchContext && (selectedProjectId != null || selectedSessionId != null) && containsAny(text, "这个", "它")) {
            return AiIntent.PERFORMANCE_DETAIL;
        }
        if (lastSearchResults != null && !lastSearchResults.isEmpty()
                && containsAny(text, "第一个", "第1个", "第二个", "第2个", "最后一个")) {
            return AiIntent.PERFORMANCE_DETAIL;
        }
        return AiIntent.GENERAL_CHAT;
    }

    public AiSemanticParseResult parseSemantic(String userText, JsonNode candidate,
                                               AiSearchContext previous, AiActiveSlot activeSlot,
                                               AiConversationAction action, AiResolvedVenue venue,
                                               String resolvedCity) {
        return semanticParser.parse(userText, candidate, previous,
                activeSlot == null ? AiActiveSlot.NONE : activeSlot,
                action == null ? AiConversationAction.REFINE_SEARCH : action,
                venue == null ? AiResolvedVenue.empty() : venue, resolvedCity,
                LocalDateTime.now(BUSINESS_ZONE).withSecond(0).withNano(0));
    }

    public boolean hasMissingOrdinalReference(String userText,
                                              List<AiSearchResultReference> lastSearchResults) {
        return containsOrdinalReference(normalize(userText))
                && (lastSearchResults == null || lastSearchResults.isEmpty());
    }

    public AiIntent resolveEllipticalReferenceIntent(AiIntent current, AiIntent previous,
                                                      AiSemanticParseResult semantic) {
        if (current != AiIntent.PERFORMANCE_DETAIL || semantic == null
                || !semantic.recognizes(AiActiveSlot.RESULT_REFERENCE)
                || semantic.recognizedSlots().size() != 1) return current;
        return switch (previous) {
            case SESSION_QUERY, TICKET_QUERY, REFUND_QUERY, PERFORMANCE_DETAIL -> previous;
            default -> current;
        };
    }

    public String toolForIntent(AiIntent intent) {
        return switch (intent) {
            case SEARCH_PERFORMANCE -> "searchPerformances";
            case PERFORMANCE_DETAIL -> "getPerformanceDetail";
            case SESSION_QUERY -> "getSessions";
            case TICKET_QUERY -> "getTicketSkus";
            case REFUND_QUERY -> "getRefundRule";
            case GENERAL_CHAT, ENTITY_QA -> "";
        };
    }

    public ResolvedReference resolveReference(String toolName, JsonNode candidateArguments, String userText,
                                              List<AiSearchResultReference> lastSearchResults,
                                              Long selectedProjectId, Long selectedSessionId,
                                              Long legacyProjectId, Long legacySessionId,
                                              Long mentionedProjectId) {
        ObjectNode arguments = candidateArguments != null && candidateArguments.isObject()
                ? (ObjectNode) candidateArguments.deepCopy() : objectMapper.createObjectNode();
        AiSearchResultReference ordinalResult = ordinalResult(userText, lastSearchResults);
        Long candidateProjectId = longValue(arguments, "projectId");
        Long candidateSessionId = longValue(arguments, "sessionId");
        Long projectId;
        Long sessionId;

        if (ordinalResult != null) {
            projectId = ordinalResult.projectId();
            sessionId = ordinalResult.sessionId();
        } else {
            projectId = validKnownProject(candidateProjectId, userText, lastSearchResults, selectedProjectId, legacyProjectId)
                    ? candidateProjectId : firstNonNull(selectedProjectId, mentionedProjectId,
                    legacyProjectId, onlyProject(lastSearchResults));
            sessionId = validKnownSession(candidateSessionId, lastSearchResults, selectedSessionId, legacySessionId)
                    ? candidateSessionId : firstNonNull(selectedSessionId, legacySessionId, sessionForProject(lastSearchResults, projectId));
        }

        if (projectId != null) arguments.put("projectId", projectId);
        else arguments.remove("projectId");
        if ("getTicketSkus".equals(toolName)) {
            if (sessionId != null) arguments.put("sessionId", sessionId);
            else arguments.remove("sessionId");
        }
        return new ResolvedReference(arguments, projectId, sessionId);
    }

    private ResolvedSearch continuation(AiSearchContext context, AiSearchResultState resultState,
                                        AiActiveSlot activeSlot) {
        ObjectNode arguments = objectMapper.createObjectNode();
        putText(arguments, "keyword", context.keyword());
        putText(arguments, "city", context.city());
        putText(arguments, "category", context.category());
        putText(arguments, "venue", context.venue());
        putText(arguments, "startTime", context.startTime());
        putText(arguments, "endTime", context.endTime());
        if (context.minPrice() != null) arguments.put("minPrice", context.minPrice());
        if (context.maxPrice() != null) arguments.put("maxPrice", context.maxPrice());
        putText(arguments, "timeIntent", context.timeIntent());
        putText(arguments, "sort", context.sort());
        arguments.put("limit", 6);
        Map<String, AiSlotOperation> operations = new LinkedHashMap<>();
        for (String slot : List.of("city", "category", "keyword", "venue", "minPrice", "maxPrice",
                "startTime", "endTime", "timeIntent", "sort")) operations.put(slot, AiSlotOperation.KEEP);
        if (resultState != null) {
            arguments.put("offset", Math.max(0, resultState.offset()));
            if (resultState.shownProjectIds() != null) {
                arguments.set("excludeProjectIds", objectMapper.valueToTree(resultState.shownProjectIds()));
            }
        }
        AiSemanticParseResult semantic = new AiSemanticParseResult(
                Map.of(AiActiveSlot.RESULT_REFERENCE.name(), "CONTINUE"), activeSlot,
                AiSemanticParseResult.Confidence.HIGH, List.of());
        return new ResolvedSearch(arguments, context, new AiSearchSlotUpdate(Map.copyOf(operations)), activeSlot, semantic);
    }

    private int resolveLimit(String text) {
        Matcher matcher = Pattern.compile("(?:前|推荐|来|给我)?\\s*([1-8])\\s*个").matcher(text);
        if (matcher.find()) return integer(matcher.group(1));
        return 6;
    }

    private PriceResolution resolvePrice(String text, ObjectNode candidate, BigDecimal previousMin, BigDecimal previousMax,
                                         boolean clearAll, Map<String, AiSlotOperation> operations) {
        if (clearAll || containsAny(text, "不限价格", "价格不限", "不看价格", "不限制价格")) {
            operations.put("minPrice", AiSlotOperation.CLEAR);
            operations.put("maxPrice", AiSlotOperation.CLEAR);
            return new PriceResolution(null, null);
        }
        Matcher range = RANGE_PRICE_FULL_PATTERN.matcher(text);
        if (range.find()) {
            BigDecimal first = decimal(range.group(1));
            BigDecimal second = decimal(range.group(2));
            if (first != null && second != null) {
                operations.put("minPrice", AiSlotOperation.REPLACE);
                operations.put("maxPrice", AiSlotOperation.REPLACE);
                return first.compareTo(second) <= 0 ? new PriceResolution(first, second) : new PriceResolution(second, first);
            }
        }
        Matcher max = MAX_PRICE_PATTERN.matcher(text);
        if (max.find()) {
            operations.put("minPrice", AiSlotOperation.CLEAR);
            operations.put("maxPrice", AiSlotOperation.REPLACE);
            return new PriceResolution(null, decimal(max.group(1)));
        }
        Matcher min = MIN_PRICE_PATTERN.matcher(text);
        if (min.find()) {
            operations.put("minPrice", AiSlotOperation.REPLACE);
            operations.put("maxPrice", AiSlotOperation.CLEAR);
            return new PriceResolution(decimal(min.group(1)), null);
        }
        if (text.contains("元") || text.contains("价格") || text.contains("预算")) {
            BigDecimal candidateMin = decimal(candidate, "minPrice");
            BigDecimal candidateMax = decimal(candidate, "maxPrice");
            if (candidateMin != null || candidateMax != null) {
                operations.put("minPrice", candidateMin == null ? AiSlotOperation.CLEAR : AiSlotOperation.REPLACE);
                operations.put("maxPrice", candidateMax == null ? AiSlotOperation.CLEAR : AiSlotOperation.REPLACE);
                return new PriceResolution(candidateMin, candidateMax);
            }
        }
        operations.put("minPrice", AiSlotOperation.KEEP);
        operations.put("maxPrice", AiSlotOperation.KEEP);
        return new PriceResolution(previousMin, previousMax);
    }

    private TimeResolution resolveTime(String text, ObjectNode candidate, String previousStart, String previousEnd,
                                       String previousIntent, LocalDateTime now, boolean clearAll,
                                       boolean allPerformances, boolean broaden,
                                       Map<String, AiSlotOperation> operations) {
        if (allPerformances || clearAll || containsAny(text, "不限时间", "时间不限", "清除时间")) {
            putGroupOperation(operations, AiSlotOperation.CLEAR, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(now), "", "FUTURE");
        }
        if (broaden && containsAny(text, "放宽时间", "扩大时间", "时间范围放宽")) {
            return broadenTime(previousIntent, previousStart, previousEnd, now, operations);
        }
        if (containsAny(text, "过去", "之前", "已经结束", "已结束", "以前的")) {
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution("", iso(now), "PAST");
        }
        if (containsAny(text, "还没开演", "未开演", "没开演", "尚未开演")) {
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(now), "", "FUTURE");
        }
        LocalDate explicitDate = extractExplicitDate(text, now.toLocalDate());
        if (explicitDate != null) {
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(explicitDate.atStartOfDay()), iso(explicitDate.atTime(LocalTime.MAX)), "EXPLICIT_DATE");
        }
        if (containsAny(text, "最近一周", "未来一周", "接下来一周", "最近7天", "最近七天", "未来7天", "未来七天")) {
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(now), iso(now.plusDays(7)), "NEXT_7_DAYS");
        }
        if (text.contains("未来")) {
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(now), "", "FUTURE");
        }
        if (containsAny(text, "这个周末", "本周末", "周末")) {
            LocalDate today = now.toLocalDate();
            LocalDate saturday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
            LocalDate sunday = saturday.plusDays(1);
            if (today.getDayOfWeek().getValue() < DayOfWeek.SATURDAY.getValue()) {
                saturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
                sunday = saturday.plusDays(1);
            }
            LocalDateTime start = today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY
                    ? now : saturday.atStartOfDay();
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(start), iso(sunday.atTime(LocalTime.MAX)), "WEEKEND");
        }
        if (containsAny(text, "今天", "今日")) {
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(now), iso(now.toLocalDate().atTime(LocalTime.MAX)), "TODAY");
        }
        if (containsAny(text, "明天", "明日")) {
            LocalDate tomorrow = now.toLocalDate().plusDays(1);
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(tomorrow.atStartOfDay()), iso(tomorrow.atTime(LocalTime.MAX)), "TOMORROW");
        }
        if (containsAny(text, "本周", "这周")) {
            LocalDate sunday = now.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(now), iso(sunday.atTime(LocalTime.MAX)), "THIS_WEEK");
        }
        if (containsAny(text, "本月", "这个月")) {
            LocalDate monthEnd = now.toLocalDate().with(TemporalAdjusters.lastDayOfMonth());
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(now), iso(monthEnd.atTime(LocalTime.MAX)), "THIS_MONTH");
        }
        if (containsAny(text, "最近", "近期", "时间最近", "最近的")) {
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(iso(now), iso(now.plusDays(30)), "RECENT");
        }
        if (!previousIntent.isBlank()) {
            putGroupOperation(operations, AiSlotOperation.KEEP, "startTime", "endTime", "timeIntent");
            return new TimeResolution(previousStart, previousEnd, previousIntent);
        }
        String candidateStart = string(candidate, "startTime");
        String candidateEnd = string(candidate, "endTime");
        if (containsAny(text, "日期", "几号", "哪天") && (!candidateStart.isBlank() || !candidateEnd.isBlank())) {
            putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
            return new TimeResolution(candidateStart, candidateEnd, "EXPLICIT_DATE");
        }
        putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
        return new TimeResolution(iso(now), "", "FUTURE");
    }

    private TimeResolution broadenTime(String previousIntent, String previousStart, String previousEnd,
                                       LocalDateTime now, Map<String, AiSlotOperation> operations) {
        return switch (value(previousIntent).toUpperCase(Locale.ROOT)) {
            case "TODAY", "TOMORROW", "WEEKEND", "THIS_WEEK", "EXPLICIT_DATE" -> {
                putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
                yield new TimeResolution(iso(now), iso(now.plusDays(7)), "NEXT_7_DAYS");
            }
            case "NEXT_7_DAYS" -> {
                putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
                yield new TimeResolution(iso(now), iso(now.plusDays(30)), "RECENT");
            }
            case "RECENT", "THIS_MONTH" -> {
                putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
                yield new TimeResolution(iso(now), "", "FUTURE");
            }
            case "FUTURE" -> {
                putGroupOperation(operations, AiSlotOperation.KEEP, "startTime", "endTime", "timeIntent");
                yield new TimeResolution(previousStart, previousEnd, "FUTURE");
            }
            default -> {
                putGroupOperation(operations, AiSlotOperation.REPLACE, "startTime", "endTime", "timeIntent");
                yield new TimeResolution(iso(now), iso(now.plusDays(7)), "NEXT_7_DAYS");
            }
        };
    }

    private String resolveSort(String text, ObjectNode candidate, String previousSort, String timeIntent,
                               boolean clearAll, boolean allPerformances,
                               Map<String, AiSlotOperation> operations) {
        if (allPerformances) {
            operations.put("sort", AiSlotOperation.REPLACE);
            return "NEAREST";
        }
        if (clearAll || containsAny(text, "不限排序", "默认排序")) {
            operations.put("sort", AiSlotOperation.CLEAR);
            return "";
        }
        if (containsAny(text, "价格从低到高", "按价格从低到高", "最便宜", "价格最低", "便宜的")) {
            operations.put("sort", AiSlotOperation.REPLACE); return "PRICE_ASC";
        }
        if (containsAny(text, "价格从高到低", "按价格从高到低", "最贵", "价格最高")) {
            operations.put("sort", AiSlotOperation.REPLACE); return "PRICE_DESC";
        }
        if (containsAny(text, "时间最近", "最近的", "最近", "近期")) {
            operations.put("sort", AiSlotOperation.REPLACE); return "NEAREST";
        }
        if (containsAny(text, "热门", "最火", "热度")) {
            operations.put("sort", AiSlotOperation.REPLACE); return "HOT";
        }
        if (containsAny(text, "最新", "新上架", "新发布")) {
            operations.put("sort", AiSlotOperation.REPLACE); return "NEW";
        }
        if (!previousSort.isBlank()) {
            operations.put("sort", AiSlotOperation.KEEP); return previousSort;
        }
        String candidateSort = string(candidate, "sort").toUpperCase(Locale.ROOT);
        if (isSupportedSort(candidateSort) && containsAny(text, "排序", "价格", "热门", "最新", "最近")) {
            operations.put("sort", AiSlotOperation.REPLACE); return candidateSort;
        }
        operations.put("sort", AiSlotOperation.REPLACE);
        if ("PAST".equals(timeIntent)) return "NEAREST";
        return "NEAREST";
    }

    private String extractExplicitCity(String text, String candidateCity) {
        String fromCandidate = explicitCandidateCity(text, candidateCity);
        if (!fromCandidate.isBlank()) return fromCandidate;
        for (String city : COMMON_CITIES) {
            if (text.contains(city)) return city;
        }
        return "";
    }

    private String extractCategory(String text, String candidateCategory) {
        for (String category : CATEGORIES) {
            if (isCategoryMention(text, category)) return category;
        }
        String candidate = value(candidateCategory);
        for (String category : CATEGORIES) {
            if (category.equals(candidate) && isCategoryMention(text, category)) return category;
        }
        return "";
    }

    private boolean isCategoryMention(String text, String category) {
        if (!text.contains(category)) return false;
        if (!"体育".equals(category)) return true;
        String withoutVenueTerms = text.replaceAll("体育(?:场|馆|中心)", "");
        return withoutVenueTerms.contains("体育");
    }

    private boolean looksLikeVenue(String value) {
        String normalized = value(value);
        return containsAny(normalized, "场馆", "剧院", "大剧院", "体育馆", "体育场", "中心", "会堂", "鸟巢");
    }

    private String explicitCandidateKeyword(String text, ObjectNode candidate, String city, String category,
                                             String canonicalVenue, String matchedVenue, AiConversationAction action) {
        String candidateKeyword = string(candidate, "keyword");
        String extracted = extractEntityKeyword(text, city, category);
        String accepted = "";
        if (!candidateKeyword.isBlank()
                && (canonicalVenue.isBlank() || !normalizeEntity(candidateKeyword).equals(normalizeEntity(canonicalVenue)))
                && (matchedVenue.isBlank() || !normalizeEntity(candidateKeyword).equals(normalizeEntity(matchedVenue)))
                && text.contains(candidateKeyword)
                && sameNormalized(candidateKeyword, extracted)) {
            accepted = sanitizeKeyword(candidateKeyword, city, category);
        } else if (!extracted.isBlank() && action == AiConversationAction.NEW_SEARCH) {
            accepted = extracted;
        }
        return accepted;
    }

    private String extractEntityKeyword(String text, String city, String category) {
        String entityText = text;
        if (!city.isBlank()) entityText = entityText.replace(city + "市", "").replace(city, "");
        if (!category.isBlank()) entityText = entityText.replace(category, "");
        Matcher quoted = QUOTED_ENTITY_PATTERN.matcher(entityText);
        if (quoted.find()) return sanitizeKeyword(quoted.group(1), city, category);
        Matcher positive = POSITIVE_KEYWORD_PATTERN.matcher(entityText);
        while (positive.find()) {
            String value = sanitizeKeyword(positive.group(1), city, category);
            if (!value.isBlank() && !looksLikeVenue(value) && !isInterrogativeResidue(value)) return value;
        }
        Matcher direct = DIRECT_ENTITY_PATTERN.matcher(entityText);
        if (direct.find()) {
            String value = sanitizeKeyword(direct.group(1), city, category);
            if (!value.isBlank() && !looksLikeVenue(value) && !isInterrogativeResidue(value)) return value;
        }
        Matcher prefix = PREFIX_ENTITY_PATTERN.matcher(entityText);
        if (prefix.find()) {
            String value = sanitizeKeyword(prefix.group(1), city, category);
            if (!value.isBlank() && !looksLikeVenue(value) && !isInterrogativeResidue(value)) return value;
        }
        return "";
    }

    private boolean isInterrogativeResidue(String value) {
        return containsAny(value, "什么", "哪些", "哪个", "哪一个", "有没有", "怎么");
    }

    private String sanitizeKeyword(String keyword, String city, String category) {
        String normalized = value(keyword);
        if (!city.isBlank()) normalized = normalized.replace(city, "").replace(city + "市", "");
        if (!category.isBlank()) normalized = normalized.replace(category, "");
        for (String generic : GENERIC_KEYWORD_PARTS) {
            normalized = normalized.replace(generic, "");
        }
        normalized = normalized.replaceAll("\\d+(?:\\.\\d+)?\\s*元?", "")
                .replaceAll("[\\s?？!！,，。~～—\\-]+", "")
                .replaceAll("^[的呢吗呀啊吧请]+|[的呢吗呀啊吧]+$", "")
                .trim();
        if (normalized.length() < 2 || sameNormalized(normalized, city) || sameNormalized(normalized, category)) return "";
        return normalized;
    }

    private boolean isGeneralQuestion(String text) {
        boolean explanatory = containsAny(text, "有什么区别", "有啥区别", "区别是什么", "什么叫", "什么意思", "怎么理解",
                "为什么", "原因", "介绍", "说明", "分别说", "分别讲");
        boolean advice = containsAny(text, "第一次", "注意什么", "怎么准备", "需要准备", "坐哪里", "坐哪", "适合吗", "礼仪", "体验怎么样", "准备什么")
                || text.matches(".*(?:需要|怎么|如何).{0,4}准备.*");
        if (explanatory || advice) return true;
        return text.matches("\\s*\\d+(?:\\.\\d+)?\\s*[+加减乘除×÷-]\\s*\\d+(?:\\.\\d+)?\\s*(?:是多少|等于多少|等于几)?[?？]?\\s*");
    }

    public boolean isTimeKnowledgeQuestion(String text) {
        String value = normalize(text);
        boolean timeExpression = containsAny(value, "最近", "近期", "未来", "今天", "明天", "后天", "本周", "周末", "本月")
                || value.matches(".*(?:近|最近|未来|接下来)\\s*[一二三四五六七八九十两\\d]+\\s*(?:天|日|周|星期|个月|月).*+");
        boolean asksMeaning = containsAny(value, "是什么意思", "指什么", "怎么理解", "一般是", "是多少天", "有几天", "多久");
        boolean ticketRequest = containsAny(value, "演出", "活动", "门票", "场次", "开演", "票价", "库存", "退款");
        return timeExpression && asksMeaning && !ticketRequest;
    }

    private boolean isEntityKnowledgeQuestion(String text) {
        boolean knowledgeForm = containsAny(text, "是什么", "是不是", "是否是", "在哪里", "在哪儿", "在哪个城市", "什么地方",
                "属于哪里", "是什么关系", "一样吗", "相同吗", "同一个吗");
        boolean ticketFact = containsAny(text, "演出", "活动", "门票", "场次", "票档", "票价", "库存", "还有票", "退款", "退票");
        return knowledgeForm && !ticketFact;
    }

    private boolean isDiscoveryRequest(String text) {
        boolean interrogative = containsAny(text, "有哪些", "有什么", "有没有", "能看什么", "有什么能看", "哪里有", "哪儿有");
        boolean imperative = containsAny(text, "搜索", "查找", "帮我找", "找一下", "找找", "推荐", "看看", "想看", "来点");
        return interrogative || imperative;
    }

    private boolean isSearchAdjustment(String text) {
        return Pattern.compile("(?:时间|范围).{0,4}(?:放宽|扩大)|(?:放宽|扩大).{0,4}(?:时间|范围)").matcher(text).find()
                || Pattern.compile("(?:便宜|低价|贵|高价|热门|热度高).{0,4}(?:前面|优先)").matcher(text).find();
    }

    private boolean isCompleteSearchRequest(String text) {
        boolean subject = containsCategory(text) || containsAny(text, "演出", "活动", "门票");
        boolean request = containsAny(text, "有哪些", "有什么", "推荐", "搜索", "查找", "帮我找", "想看", "来点", "看看");
        return subject && request;
    }

    private boolean isAllPerformancesRequest(String text) {
        return containsAny(text, "全部演出", "所有演出", "全部活动", "所有活动")
                && containsAny(text, "查看", "看看", "找", "搜索", "有哪些", "有什么");
    }

    private boolean hasResultState(AiSearchResultState state) {
        return state != null && state.resultCount() > 0
                && (state.currentResultIds() != null && !state.currentResultIds().isEmpty()
                || state.shownProjectIds() != null && !state.shownProjectIds().isEmpty());
    }

    private String removeVenueExpression(String text, AiResolvedVenue venue) {
        if (venue == null || !venue.found()) return text;
        String result = text;
        if (venue.matchedText() != null && !venue.matchedText().isBlank()) {
            result = result.replace(venue.matchedText(), "");
        }
        if (venue.canonicalName() != null && !venue.canonicalName().isBlank()) {
            result = result.replace(venue.canonicalName(), "");
        }
        return result;
    }

    private boolean hasSearchContext(AiSearchContext context) {
        return context != null && (!value(context.city()).isBlank() || !value(context.category()).isBlank()
                || !sanitizeKeyword(context.keyword(), "", "").isBlank() || !value(context.venue()).isBlank()
                || context.minPrice() != null || context.maxPrice() != null
                || !value(context.timeIntent()).isBlank() || !value(context.sort()).isBlank());
    }

    private boolean containsCategory(String text) {
        for (String category : CATEGORIES) if (isCategoryMention(text, category)) return true;
        return false;
    }

    private boolean containsCity(String text) {
        for (String city : COMMON_CITIES) if (text.contains(city)) return true;
        return false;
    }

    private boolean containsTimeExpression(String text) {
        return containsAny(text, "今天", "明天", "本周", "这周", "周末", "最近", "近期", "本月", "过去", "之前")
                || ISO_DATE_PATTERN.matcher(text).find() || MONTH_DAY_PATTERN.matcher(text).find()
                || SLASH_MONTH_DAY_PATTERN.matcher(text).find();
    }

    private boolean containsPriceExpression(String text) {
        return containsAny(text, "价格", "预算", "元", "便宜", "最贵");
    }

    private boolean containsSortExpression(String text) {
        return containsAny(text, "排序", "从低到高", "从高到低", "最近的", "热门", "最新");
    }

    private boolean containsVenueExpression(String text) {
        return looksLikeVenue(text) || containsAny(text, "换个场馆", "换场地", "不限场馆");
    }

    private boolean isContinueAction(String text) {
        return containsAny(text, "继续看", "继续找", "更多", "下一批", "再来一批", "还有别的", "还有其他",
                "其他演出", "其它演出", "换一批");
    }

    private boolean isContinueSemantic(String text, AiSemanticParseResult semantic) {
        return semantic != null && "CONTINUE".equals(
                semantic.recognizedSlots().get(AiActiveSlot.RESULT_REFERENCE.name())) || isContinueAction(text);
    }

    private boolean isComparison(String text) {
        return containsAny(text, "比较", "哪个更", "哪个便宜", "哪个早", "这两个", "第一个和第二个")
                || isPriceSelection(text) || isSessionSelection(text);
    }

    public boolean isPriceSelection(String text) {
        String value = normalize(text);
        boolean superlative = containsAny(value, "最便宜", "价格最低", "票价最低", "最低价", "最贵", "价格最高", "票价最高", "最高价");
        boolean selection = containsAny(value, "哪个", "哪一个", "哪场", "是谁", "是哪个", "是哪一个", "这几个");
        return superlative && selection;
    }

    public boolean isSessionSelection(String text) {
        String value = normalize(text);
        boolean superlative = containsAny(value, "最早", "最晚", "时间最早", "时间最晚");
        boolean comparative = containsAny(value, "更早", "更晚");
        boolean selection = containsAny(value, "哪个", "哪一个", "哪场", "是谁", "是哪个", "是哪一个", "这几个");
        return (superlative || comparative) && selection;
    }

    public boolean isSessionAttributeQuery(String text) {
        String value = normalize(text);
        if (containsAny(value, "场次", "开演时间", "演出日期")) return true;
        boolean asksTime = containsAny(value, "什么时候", "何时", "几点", "哪天", "哪一天", "日期", "时间");
        boolean eventPredicate = containsAny(value, "演", "举行", "开始", "开演", "开场");
        return asksTime && eventPredicate;
    }

    private String updateTextSlot(String slot, String previous, String explicit, boolean clear,
                                  Map<String, AiSlotOperation> operations) {
        if (clear) {
            operations.put(slot, AiSlotOperation.CLEAR);
            return "";
        }
        if (explicit != null && !explicit.isBlank()) {
            operations.put(slot, AiSlotOperation.REPLACE);
            return explicit.trim();
        }
        operations.put(slot, AiSlotOperation.KEEP);
        return value(previous);
    }

    private void putGroupOperation(Map<String, AiSlotOperation> operations, AiSlotOperation operation,
                                   String... slots) {
        for (String slot : slots) operations.put(slot, operation);
    }

    private void markRebuiltClears(Map<String, AiSlotOperation> operations) {
        for (String slot : List.of("category", "keyword", "venue", "minPrice", "maxPrice")) {
            if (operations.get(slot) == AiSlotOperation.KEEP) operations.put(slot, AiSlotOperation.CLEAR);
        }
    }

    private String normalizeEntity(String value) {
        return value(value).toLowerCase(Locale.ROOT).replaceAll("[\\s··()（）_-]+", "");
    }

    private String explicitCandidateValue(String text, ObjectNode candidate, String name) {
        String value = string(candidate, name);
        return !value.isBlank() && text.contains(value) ? value : "";
    }

    private String explicitCandidateCity(String text, ObjectNode candidate) {
        return explicitCandidateCity(text, string(candidate, "city"));
    }

    private String explicitCandidateCity(String text, String candidateCity) {
        String city = cleanCity(candidateCity);
        if (city.isBlank() || containsAny(city, "最近", "近期", "一周", "周末", "今天", "明天", "本周", "本月", "演出", "活动")) {
            return "";
        }
        String normalizedText = text.replace("市", "");
        String normalizedCity = city.replace("市", "");
        if (!normalizedText.contains(normalizedCity)) return "";
        boolean explicitForm = isKnownCity(city) || text.contains(city + "市")
                || containsAny(text, "换" + city, "换到" + city, "改成" + city, "在" + city, city + "的演出")
                || normalizedText.startsWith(normalizedCity + "有") || normalizedText.startsWith(normalizedCity + "最近")
                || normalizedText.startsWith(normalizedCity + "近期");
        return explicitForm ? city : "";
    }

    private boolean isKnownCity(String city) {
        for (String known : COMMON_CITIES) if (known.equals(cleanCity(city))) return true;
        return false;
    }

    private LocalDate extractExplicitDate(String text, LocalDate baseDate) {
        Matcher iso = ISO_DATE_PATTERN.matcher(text);
        if (iso.find()) return safeDate(integer(iso.group(1)), integer(iso.group(2)), integer(iso.group(3)));
        Matcher md = MONTH_DAY_PATTERN.matcher(text);
        if (md.find()) return safeDate(baseDate.getYear(), integer(md.group(1)), integer(md.group(2)));
        Matcher slash = SLASH_MONTH_DAY_PATTERN.matcher(text);
        if (slash.find()) return safeDate(baseDate.getYear(), integer(slash.group(1)), integer(slash.group(2)));
        return null;
    }

    private AiSearchResultReference ordinalResult(String userText, List<AiSearchResultReference> results) {
        if (results == null || results.isEmpty()) return null;
        String text = normalize(userText);
        if (containsAny(text, "最后一个", "最后场", "最后的", "排尾", "最末")) return results.get(results.size() - 1);
        int index = -1;
        if (containsAny(text, "第一个", "第1个", "第一场", "第一项", "排头", "最前面", "排在最前")) index = 0;
        else if (containsAny(text, "第二个", "第2个", "第二场", "第二项")) index = 1;
        else if (containsAny(text, "第三个", "第3个", "第三场", "第三项")) index = 2;
        else if (containsAny(text, "第四个", "第4个", "第四场", "第四项")) index = 3;
        else if (containsAny(text, "第五个", "第5个", "第五场", "第五项")) index = 4;
        else if (containsAny(text, "第六个", "第6个", "第六场", "第六项")) index = 5;
        else if (containsAny(text, "第七个", "第7个", "第七场", "第七项")) index = 6;
        else if (containsAny(text, "第八个", "第8个", "第八场", "第八项")) index = 7;
        return index >= 0 && index < results.size() ? results.get(index) : null;
    }

    private boolean containsOrdinalReference(String text) {
        return containsAny(text, "第一个", "第1个", "第一场", "第一项", "第二个", "第2个", "第二场", "第二项",
                "第三个", "第3个", "第三场", "第三项", "第四个", "第4个", "第四场", "第四项",
                "第五个", "第5个", "第五场", "第五项", "第六个", "第6个", "第六场", "第六项",
                "第七个", "第7个", "第七场", "第七项", "第八个", "第8个", "第八场", "第八项",
                "最后一个", "最后场", "最后的", "排头", "最前面", "排在最前", "排尾", "最末");
    }

    private boolean validKnownProject(Long projectId, String userText, List<AiSearchResultReference> results,
                                      Long selectedProjectId, Long legacyProjectId) {
        if (projectId == null) return false;
        if (projectId.equals(selectedProjectId) || projectId.equals(legacyProjectId)) return true;
        if (results != null && results.stream().anyMatch(item -> item != null && projectId.equals(item.projectId()))) return true;
        return normalize(userText).contains(String.valueOf(projectId));
    }

    private boolean validKnownSession(Long sessionId, List<AiSearchResultReference> results,
                                      Long selectedSessionId, Long legacySessionId) {
        if (sessionId == null) return false;
        if (sessionId.equals(selectedSessionId) || sessionId.equals(legacySessionId)) return true;
        return results != null && results.stream().anyMatch(item -> item != null && sessionId.equals(item.sessionId()));
    }

    private Long onlyProject(List<AiSearchResultReference> results) {
        return results != null && results.size() == 1 && results.get(0) != null ? results.get(0).projectId() : null;
    }

    private Long sessionForProject(List<AiSearchResultReference> results, Long projectId) {
        if (results == null || projectId == null) return null;
        for (AiSearchResultReference item : results) {
            if (item != null && projectId.equals(item.projectId())) return item.sessionId();
        }
        return null;
    }

    private String cleanCity(String city) {
        String value = value(city);
        return value.endsWith("市") && value.length() > 2 ? value.substring(0, value.length() - 1) : value;
    }

    private boolean sameNormalized(String left, String right) {
        return !value(left).isBlank() && cleanCity(left).equalsIgnoreCase(cleanCity(right));
    }

    private boolean isSupportedSort(String value) {
        return "NEAREST".equals(value) || "PRICE_ASC".equals(value) || "PRICE_DESC".equals(value)
                || "HOT".equals(value) || "NEW".equals(value);
    }

    private String string(JsonNode node, String name) {
        if (node == null) return "";
        JsonNode value = node.path(name);
        return value.isTextual() ? value(value.asText()) : "";
    }

    private BigDecimal decimal(JsonNode node, String name) {
        if (node == null) return null;
        JsonNode value = node.path(name);
        if (!value.isNumber() && !value.isTextual()) return null;
        return decimal(value.asText());
    }

    private BigDecimal decimal(String value) {
        try {
            return value == null || value.isBlank() ? null : new BigDecimal(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private int integer(JsonNode node, String name, int fallback) {
        if (node == null) return fallback;
        JsonNode value = node.path(name);
        return value.isNumber() ? value.asInt() : fallback;
    }

    private int integer(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return -1;
        }
    }

    private Long longValue(JsonNode node, String name) {
        if (node == null) return null;
        JsonNode value = node.path(name);
        if (value.isNumber()) return value.asLong();
        if (value.isTextual() && !value.asText().isBlank()) {
            try {
                return Long.parseLong(value.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private LocalDate safeDate(int year, int month, int day) {
        try {
            return LocalDate.of(year, month, day);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value.trim();
        return "";
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) if (value != null) return value;
        return null;
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }

    private void putText(ObjectNode node, String name, String value) {
        if (value != null && !value.isBlank()) node.put(name, value);
    }

    private String iso(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

    private record PriceResolution(BigDecimal minPrice, BigDecimal maxPrice) {}
    private record TimeResolution(String startTime, String endTime, String timeIntent) {}
    public record ResolvedSearch(ObjectNode arguments, AiSearchContext context,
                                 AiSearchSlotUpdate slotUpdate, AiActiveSlot activeSlot,
                                 AiSemanticParseResult semantic) {}
    public record ResolvedReference(ObjectNode arguments, Long projectId, Long sessionId) {}
}
