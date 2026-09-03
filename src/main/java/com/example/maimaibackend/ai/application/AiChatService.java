package com.example.maimaibackend.ai.application;

import com.example.maimaibackend.ai.api.dto.AiChatRequest;
import com.example.maimaibackend.ai.application.orchestration.AiResponseComposer;
import com.example.maimaibackend.ai.application.orchestration.AiTaskPlanner;
import com.example.maimaibackend.ai.application.recommendation.AiCapabilityRegistry;
import com.example.maimaibackend.ai.domain.action.AiConversationAction;
import com.example.maimaibackend.ai.domain.context.AiActiveSlot;
import com.example.maimaibackend.ai.domain.context.AiEntityContext;
import com.example.maimaibackend.ai.domain.context.AiSearchContext;
import com.example.maimaibackend.ai.domain.context.AiSearchResultReference;
import com.example.maimaibackend.ai.domain.context.AiSearchResultState;
import com.example.maimaibackend.ai.domain.entity.AiResolvedVenue;
import com.example.maimaibackend.ai.domain.intent.AiIntent;
import com.example.maimaibackend.ai.domain.search.AiSearchSemanticResolver;
import com.example.maimaibackend.ai.domain.search.AiSemanticParseResult;
import com.example.maimaibackend.ai.domain.search.SearchExecutionResult;
import com.example.maimaibackend.ai.infrastructure.llm.AiChatProvider;
import com.example.maimaibackend.ai.infrastructure.llm.AiProperties;
import com.example.maimaibackend.ai.tool.AiTicketToolService;
import com.example.maimaibackend.ai.tool.AiVenueResolver;
import com.example.maimaibackend.ai.tool.AiVenueResolver.VenueFact;
import com.example.maimaibackend.vo.performance.PerformanceCardVO;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AiChatService {
    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private static final String GENERAL_SYSTEM_RULES = "你是麦麦AI。"
            + "用户使用简体中文提问时，必须以自然、完整的简体中文回答。"
            + "除非用户明确要求翻译、英文回答，或专有名词必须保留英文，否则不要输出英文完整句。"
            + "不要进行中英双语解释，不要自动给出英文例句。"
            + "不要将普通问题改写成考试题、数学题或选择题。"
            + "不知道的事实不要编造。回答简洁、直接。";
    private final AiProperties properties;
    private final AiChatProvider provider;
    private final AiTicketToolService toolService;
    private final AiSearchSemanticResolver semanticResolver;
    private final AiVenueResolver venueResolver;
    private final AiTaskPlanner taskPlanner;
    private final AiResponseComposer responseComposer;
    private final AiCapabilityRegistry capabilityRegistry;
    private final ObjectMapper objectMapper;
    private final AiToolSchemaFactory toolSchemaFactory;
    private final AiConversationTextPolicy textPolicy;

    public AiChatService(AiProperties properties, AiChatProvider provider,
                         AiTicketToolService toolService, AiSearchSemanticResolver semanticResolver,
                         AiVenueResolver venueResolver,
                         AiTaskPlanner taskPlanner, AiResponseComposer responseComposer,
                         AiCapabilityRegistry capabilityRegistry, ObjectMapper objectMapper,
                         AiToolSchemaFactory toolSchemaFactory, AiConversationTextPolicy textPolicy) {
        this.properties = properties;
        this.provider = provider;
        this.toolService = toolService;
        this.semanticResolver = semanticResolver;
        this.venueResolver = venueResolver;
        this.taskPlanner = taskPlanner;
        this.responseComposer = responseComposer;
        this.capabilityRegistry = capabilityRegistry;
        this.objectMapper = objectMapper;
        this.toolSchemaFactory = toolSchemaFactory;
        this.textPolicy = textPolicy;
    }

    public SseEmitter chat(AiChatRequest request) {
        SseEmitter emitter = new SseEmitter(180000L);
        AtomicBoolean closed = new AtomicBoolean(false);
        emitter.onCompletion(() -> closed.set(true));
        emitter.onTimeout(() -> closed.set(true));
        emitter.onError(error -> closed.set(true));
        CompletableFuture.runAsync(() -> run(request, emitter, closed));
        return emitter;
    }

    private void run(AiChatRequest request, SseEmitter emitter, AtomicBoolean closed) {
        TimingState timing = new TimingState();
        try {
            properties.requireConfigured();
            RuntimeState state = new RuntimeState(request);
            String latestUserText = textPolicy.latestUserText(request);
            AiConversationTextPolicy.TranslationRequest translation = textPolicy.translationRequest(request);
            String currentCity = request == null ? "" : textPolicy.safeText(request.currentCity());
            AiResolvedVenue explicitVenue = translation.found()
                    ? AiResolvedVenue.empty() : venueResolver.resolve(latestUserText, "", "");
            String explicitCity = translation.found() ? "" : explicitVenue.found() ? explicitVenue.city()
                    : venueResolver.resolveCity(latestUserText, "");
            AiSemanticParseResult initialSemantic = translation.found() ? AiSemanticParseResult.empty()
                    : semanticResolver.parseSemantic(
                    latestUserText, objectMapper.createObjectNode(), state.searchContext, state.activeSlot,
                    AiConversationAction.REFINE_SEARCH, explicitVenue, explicitCity);
            AiIntent intent = translation.found() ? AiIntent.GENERAL_CHAT : semanticResolver.detectIntent(
                    latestUserText, state.searchContext, state.lastSearchResults,
                    state.selectedProjectId, state.selectedSessionId, initialSemantic);
            String previousUserText = textPolicy.previousUserText(request);
            if (!translation.found() && !textPolicy.isIndependentDefinitionQuestion(latestUserText)
                    && !previousUserText.isBlank()) {
                AiSemanticParseResult previousSemantic = semanticResolver.parseSemantic(
                        previousUserText, objectMapper.createObjectNode(), state.searchContext, state.activeSlot,
                        AiConversationAction.REFINE_SEARCH, AiResolvedVenue.empty(), "");
                AiIntent previousIntent = semanticResolver.detectIntent(
                        previousUserText, state.searchContext, state.lastSearchResults,
                        state.selectedProjectId, state.selectedSessionId, previousSemantic);
                intent = semanticResolver.resolveEllipticalReferenceIntent(intent, previousIntent, initialSemantic);
            }
            state.intent = intent;
            if (explicitVenue.found()) state.rememberVenue(explicitVenue);
            state.action = translation.found() ? AiConversationAction.GENERAL_CHAT
                    : semanticResolver.resolveAction(intent, latestUserText, state.searchContext,
                    state.searchResultState, initialSemantic);
            ArrayNode messages = buildMessages(request, state, intent);
            ArrayNode tools = toolSchemaFactory.build();
            if (state.action == AiConversationAction.BROADEN_SEARCH
                    && semanticResolver.cannotBroadenFutureTime(latestUserText, state.searchContext)) {
                timing.semanticMs = timing.elapsedMs();
                sendTextChunks(emitter, closed, "当前已经是全部未来演出范围，可以尝试不限价格、取消分类或更换城市。", timing);
                finishResponse(emitter, closed, state, timing);
                return;
            }
            AiTaskPlanner.TaskPlan taskPlan = taskPlanner.plan(
                    intent, state.action, latestUserText, !state.lastSearchResults.isEmpty(),
                    state.selectedProjectId != null || state.selectedSessionId != null);
            timing.semanticMs = timing.elapsedMs();

            if (semanticResolver.hasMissingOrdinalReference(latestUserText, state.lastSearchResults)) {
                state.clearSearchReferences();
                sendTextChunks(emitter, closed, responseComposer.missingReference(), timing);
                finishResponse(emitter, closed, state, timing);
                return;
            }

            if (taskPlan.taskType() != AiTaskPlanner.TaskType.DIRECT_INTENT) {
                timing.startTool();
                PlanResult planResult = executePlan(taskPlan, latestUserText, currentCity, messages, tools,
                        emitter, closed, state);
                timing.finishTool();
                sendTextChunks(emitter, closed, planResult.summary(), timing);
                finishResponse(emitter, closed, state, timing);
                return;
            }

            if (!isConversationIntent(intent)) {
                ensureConnected(closed);
                timing.startTool();
                String requiredTool = semanticResolver.toolForIntent(intent);
                JsonNode candidateArguments = objectMapper.createObjectNode();
                ObjectNode finalArguments = resolveArguments(
                        requiredTool, candidateArguments, latestUserText, currentCity, state);
                if (state.action == AiConversationAction.REFINE_SEARCH
                        && state.semanticConfidence == AiSemanticParseResult.Confidence.LOW) {
                    timing.finishTool();
                    sendTextChunks(emitter, closed, "我还不能确定你想修改哪个搜索条件，请补充城市、时间、价格、类型或场馆。", timing);
                    finishResponse(emitter, closed, state, timing);
                    return;
                }
                messages.add(fallbackAssistant(requiredTool, finalArguments));
                AiTicketToolService.ToolResult directResult = executeTool(
                        messages, emitter, closed, requiredTool, "fallback_1", finalArguments, state);
                timing.finishTool();
                if (intent != AiIntent.SEARCH_PERFORMANCE) {
                    sendTextChunks(emitter, closed, responseComposer.business(intent, directResult, latestUserText), timing);
                    finishResponse(emitter, closed, state, timing);
                    return;
                }
            }

            if (intent == AiIntent.SEARCH_PERFORMANCE) {
                String summary = state.lastToolSummary == null || state.lastToolSummary.isBlank()
                        ? "暂未查询到符合当前条件的演出。" : state.lastToolSummary;
                sendTextChunks(emitter, closed, summary, timing);
                finishResponse(emitter, closed, state, timing);
                return;
            }

            if (isTicketFactIntent(intent)) {
                log.warn("[AiFactGuard] intent={} toolResponseMissing=true providerBlocked=true", intent);
                sendTextChunks(emitter, closed, responseComposer.ticketFactUnavailable(intent), timing);
                finishResponse(emitter, closed, state, timing);
                return;
            }

            String deterministicGeneral = translation.found() ? ""
                    : deterministicGeneralAnswer(latestUserText, initialSemantic);
            if (!deterministicGeneral.isBlank()) {
                sendTextChunks(emitter, closed, deterministicGeneral, timing);
                finishResponse(emitter, closed, state, timing);
                return;
            }

            ensureConnected(closed);
            boolean[] hasText = {false};
            GeneralStreamGuard guard = new GeneralStreamGuard(textPolicy.expectsChineseOutput(latestUserText));
            try {
                provider.stream(messages, text -> {
                    if (text == null || text.isEmpty()) return;
                    String accepted = guard.accept(text);
                    if (!accepted.isEmpty()) {
                        hasText[0] = true;
                        timing.markFirstText();
                        send(emitter, closed, "TEXT", Map.of("text", accepted));
                    }
                    if (guard.stopped()) throw new GeneralGenerationStoppedException();
                });
            } catch (GeneralGenerationStoppedException ignored) {
            }
            if (guard.languageViolation()) {
                log.warn("[AiLanguageGuard] requestId={} expectedLanguage=zh-CN englishRatio={} triggered=true action=LOG_ONLY",
                        state.requestId, guard.englishRatio());
            }
            if (!hasText[0] && state.emittedCards.isEmpty()) throw new IllegalStateException("麦麦AI未返回可展示内容，请重试");
            finishResponse(emitter, closed, state, timing);
        } catch (SseDisconnectedException ignored) {
            closed.set(true);
        } catch (Exception ex) {
            if (closed.get() || isDisconnect(ex)) {
                closed.set(true);
                return;
            }
            try {
                send(emitter, closed, "ERROR", Map.of("message", safe(ex.getMessage())));
            } catch (SseDisconnectedException ignored) {
                closed.set(true);
                return;
            }
            completeSafely(emitter, closed);
        }
    }

    private ObjectNode resolveArguments(String name, JsonNode candidateArguments, String latestUserText,
                                        String currentCity, RuntimeState state) {
        if ("searchPerformances".equals(name)) {
            AiResolvedVenue resolvedVenue = state.action == AiConversationAction.CONTINUE_RESULTS
                    ? AiResolvedVenue.empty()
                    : venueResolver.resolve(latestUserText,
                    text(candidateArguments, "venue"), text(candidateArguments, "keyword"));
            if (!resolvedVenue.found() && textPolicy.referencesCurrentEntity(latestUserText)
                    && state.entityContext != null && !textPolicy.safeText(state.entityContext.venue()).isBlank()) {
                resolvedVenue = venueResolver.resolveCanonical(state.entityContext.venue());
            }
            String resolvedCity = resolvedVenue.found() ? resolvedVenue.city()
                    : venueResolver.resolveCity(latestUserText, text(candidateArguments, "city"));
            AiSearchSemanticResolver.ResolvedSearch resolved = semanticResolver.resolveSearch(
                    latestUserText, candidateArguments, state.searchContext, currentCity,
                    state.action, state.searchResultState, resolvedVenue, resolvedCity, state.activeSlot);
            state.searchContext = resolved.context();
            state.activeSlot = resolved.activeSlot();
            state.semanticConfidence = resolved.semantic().confidence();
            if (resolvedVenue.found()) state.rememberVenue(resolvedVenue);
            return resolved.arguments();
        }
        AiSearchSemanticResolver.ResolvedReference resolved = semanticResolver.resolveReference(
                name, candidateArguments, latestUserText, state.lastSearchResults,
                state.selectedProjectId, state.selectedSessionId, state.contextProjectId, state.contextSessionId,
                state.entityContext == null ? null : state.entityContext.mentionedProjectId());
        return resolved.arguments();
    }

    private PlanResult executePlan(AiTaskPlanner.TaskPlan plan, String userText, String currentCity,
                                   ArrayNode messages, ArrayNode tools, SseEmitter emitter,
                                   AtomicBoolean closed, RuntimeState state) {
        return switch (plan.taskType()) {
            case SEARCH_FILTER_REFUND -> executeRefundPlan(userText, currentCity, messages, tools,
                    emitter, closed, state, true);
            case FILTER_REFUND -> executeRefundPlan(userText, currentCity, messages, tools,
                    emitter, closed, state, false);
            case FILTER_AVAILABILITY -> executeAvailabilityPlan(emitter, closed, state);
            case COMPARE_PRICE -> executePriceComparison(plan.compareMode(), state);
            case SELECT_PRICE -> executePriceSelection(plan.compareMode(), state);
            case COMPARE_SESSION -> executeSessionComparison(userText, state);
            case DIRECT_INTENT -> new PlanResult("", 0, 0);
        };
    }

    private PlanResult executeRefundPlan(String userText, String currentCity, ArrayNode messages,
                                         ArrayNode tools, SseEmitter emitter, AtomicBoolean closed,
                                         RuntimeState state, boolean searchFirst) {
        List<PerformanceCardVO> searchCards = List.of();
        int stages = searchFirst ? 2 : 1;
        if (searchFirst) {
            JsonNode candidateArguments = objectMapper.createObjectNode();
            ObjectNode searchArguments = resolveArguments(
                    "searchPerformances", candidateArguments, userText, currentCity, state);
            AiTicketToolService.ToolResult searchResult = callTool("searchPerformances", searchArguments, null, null);
            searchCards = searchResult.cards() == null ? List.of() : searchResult.cards();
            applySearchResult(searchResult, state);
            if (state.lastSearchResults.isEmpty()) {
                return new PlanResult(responseComposer.search(searchResult), 0, stages);
            }
        }

        List<AiSearchResultReference> candidates = limitedCandidates(state.lastSearchResults);
        if (candidates.isEmpty()) {
            state.clearSearchReferences();
            return new PlanResult(responseComposer.missingReference(), 0, stages);
        }
        boolean other = userText.contains("其他");
        boolean firstOnly = userText.contains("有没有") || other;
        List<AiSearchResultReference> matched = new ArrayList<>();
        for (AiSearchResultReference candidate : candidates) {
            if (candidate == null || candidate.projectId() == null) continue;
            if (other && candidate.projectId().equals(state.selectedProjectId)) continue;
            AiTicketToolService.ToolResult result = callTool(
                    "getRefundRule", projectArguments(candidate.projectId(), candidate.sessionId()), null, null);
            if (toolFound(result)) {
                matched.add(candidate);
                if (firstOnly) break;
            }
        }
        if (matched.isEmpty()) {
            if (searchFirst) state.clearSearchReferences();
            return new PlanResult("当前这批搜索结果中暂未找到支持退款的演出。", candidates.size(), stages);
        }

        List<PerformanceCardVO> cards = cardsForMatches(matched, searchCards);
        if (!searchFirst && !matched.isEmpty()) stages++;
        queueCards(cards, state);
        if (searchFirst || !firstOnly) {
            state.setCurrentResults(matched);
        } else {
            state.moveResultToFront(matched.get(0));
            state.select(matched.get(0));
        }
        String summary = responseComposer.refundFilter(matched, candidates.size());
        return new PlanResult(summary, candidates.size(), stages);
    }

    private PlanResult executeAvailabilityPlan(SseEmitter emitter, AtomicBoolean closed, RuntimeState state) {
        List<AiSearchResultReference> candidates = limitedCandidates(state.lastSearchResults);
        if (candidates.isEmpty()) {
            state.clearSearchReferences();
            return new PlanResult(responseComposer.missingReference(), 0, 1);
        }
        List<AiSearchResultReference> matched = new ArrayList<>();
        for (AiSearchResultReference candidate : candidates) {
            AiTicketToolService.ToolResult result = callTool(
                    "getTicketSkus", projectArguments(candidate.projectId(), candidate.sessionId()), null, null);
            if (minimumAvailablePrice(result) != null) matched.add(candidate);
        }
        if (matched.isEmpty()) {
            return new PlanResult("当前这批搜索结果中暂未找到有可售库存的演出。", candidates.size(), 1);
        }
        List<PerformanceCardVO> cards = cardsForMatches(matched, List.of());
        queueCards(cards, state);
        state.setCurrentResults(matched);
        return new PlanResult(responseComposer.availabilityFilter(matched), candidates.size(), 2);
    }

    private PlanResult executePriceComparison(AiTaskPlanner.CompareMode mode, RuntimeState state) {
        List<AiSearchResultReference> pair = comparisonPair(state);
        if (pair.size() < 2) {
            state.clearSearchReferences();
            return new PlanResult("当前没有两个可比较的演出，请先搜索演出。", pair.size(), 1);
        }
        state.setCompared(pair);
        boolean higherWins = mode == AiTaskPlanner.CompareMode.MAX_PRICE;
        BigDecimal firstPrice = availablePrice(callTool(
                "getTicketSkus", projectArguments(pair.get(0).projectId(), pair.get(0).sessionId()), null, null), mode);
        BigDecimal secondPrice = availablePrice(callTool(
                "getTicketSkus", projectArguments(pair.get(1).projectId(), pair.get(1).sessionId()), null, null), mode);
        String priceLabel = higherWins ? "最高价" : "最低价";
        String saleLabel = higherWins ? "最高可售票档价格" : "最低可售价";
        String summary;
        if (firstPrice == null && secondPrice == null) {
            summary = "这两个演出当前都没有可售票档，暂时无法比较价格。";
        } else if (firstPrice == null) {
            summary = pair.get(0).title() + "当前暂无可售票档；" + pair.get(1).title() + "当前" + priceLabel + "为"
                    + formatPrice(secondPrice) + "元。";
        } else if (secondPrice == null) {
            summary = pair.get(1).title() + "当前暂无可售票档；" + pair.get(0).title() + "当前" + priceLabel + "为"
                    + formatPrice(firstPrice) + "元。";
        } else {
            int compared = firstPrice.compareTo(secondPrice);
            if (compared == 0) {
                summary = "按当前可售票档，" + pair.get(0).title() + "和" + pair.get(1).title()
                        + "的" + priceLabel + "相同，都是" + formatPrice(firstPrice) + "元。";
            } else {
                boolean firstWins = higherWins ? compared > 0 : compared < 0;
                AiSearchResultReference winner = firstWins ? pair.get(0) : pair.get(1);
                BigDecimal winnerPrice = firstWins ? firstPrice : secondPrice;
                AiSearchResultReference other = firstWins ? pair.get(1) : pair.get(0);
                BigDecimal otherPrice = firstWins ? secondPrice : firstPrice;
                state.select(winner);
                summary = winner.title() + (higherWins ? "更贵" : "更便宜") + "，当前" + saleLabel
                        + formatPrice(winnerPrice) + "元；" + other.title() + "当前" + saleLabel
                        + formatPrice(otherPrice) + "元。";
            }
        }
        return new PlanResult(summary, pair.size(), 1);
    }

    private PlanResult executePriceSelection(AiTaskPlanner.CompareMode mode, RuntimeState state) {
        List<AiSearchResultReference> candidates = limitedCandidates(state.lastSearchResults);
        if (candidates.isEmpty()) {
            state.clearSearchReferences();
            return new PlanResult(responseComposer.missingComparison(), 0, 0);
        }
        boolean selectMaximum = mode == AiTaskPlanner.CompareMode.MAX_PRICE;
        BigDecimal bestPrice = null;
        List<AiSearchResultReference> winners = new ArrayList<>();
        for (AiSearchResultReference candidate : candidates) {
            BigDecimal price = availablePrice(callTool(
                    "getTicketSkus", projectArguments(candidate.projectId(), candidate.sessionId()), null, null), mode);
            if (price == null) continue;
            int comparison = bestPrice == null ? 1 : price.compareTo(bestPrice);
            boolean better = bestPrice == null || (selectMaximum ? comparison > 0 : comparison < 0);
            if (bestPrice == null || better) {
                bestPrice = price;
                winners.clear();
                winners.add(candidate);
            } else if (comparison == 0) {
                winners.add(candidate);
            }
        }
        if (winners.isEmpty()) return new PlanResult("当前这些演出都没有可售票档，暂时无法比较价格。", candidates.size(), 1);
        state.setCompared(winners);
        if (winners.size() == 1) state.select(winners.get(0));
        String names = winners.stream().map(AiSearchResultReference::title).filter(value -> value != null && !value.isBlank())
                .reduce((left, right) -> left + "、" + right).orElse("符合条件的演出");
        String label = selectMaximum ? "最高" : "最低";
        String aggregationLabel = selectMaximum ? "最高可售票档价格" : "最低可售价";
        return new PlanResult(winners.size() == 1
                ? names + "当前" + aggregationLabel + "为" + formatPrice(bestPrice) + "元，是这批结果中价格" + label + "的演出。"
                : names + "当前" + aggregationLabel + "相同，均为" + formatPrice(bestPrice) + "元，并列价格" + label + "。",
                candidates.size(), 1);
    }

    private PlanResult executeSessionComparison(String userText, RuntimeState state) {
        boolean weekend = userText.contains("周末");
        List<AiSearchResultReference> candidates = weekend
                ? limitedCandidates(state.lastSearchResults) : comparisonPair(state);
        if (candidates.isEmpty() || !weekend && candidates.size() < 2) {
            state.clearSearchReferences();
            return new PlanResult(weekend ? "当前没有可引用的演出，请先搜索演出。"
                    : "当前没有两个可比较的演出，请先搜索演出。", candidates.size(), 1);
        }
        if (weekend) {
            List<String> weekendTitles = new ArrayList<>();
            for (AiSearchResultReference candidate : candidates) {
                AiTicketToolService.ToolResult result = callTool(
                        "getSessions", projectArguments(candidate.projectId(), null), null, null);
                if (hasWeekendSession(result)) weekendTitles.add(candidate.title());
            }
            String summary = weekendTitles.isEmpty()
                    ? "当前这批搜索结果中没有查询到周末场次。"
                    : "当前有周末场次的演出：" + String.join("、", weekendTitles) + "。";
            return new PlanResult(summary, candidates.size(), 1);
        }
        List<AiSearchResultReference> pair = candidates;
        state.setCompared(pair);
        AiTicketToolService.ToolResult firstResult = callTool(
                "getSessions", projectArguments(pair.get(0).projectId(), null), null, null);
        AiTicketToolService.ToolResult secondResult = callTool(
                "getSessions", projectArguments(pair.get(1).projectId(), null), null, null);
        LocalDateTime firstTime = earliestSession(firstResult);
        LocalDateTime secondTime = earliestSession(secondResult);
        String summary;
        if (firstTime == null && secondTime == null) {
            summary = "这两个演出当前都没有可比较的场次。";
        } else if (firstTime == null) {
            state.select(pair.get(1));
            summary = pair.get(0).title() + "当前没有可用场次；" + pair.get(1).title() + "更早。";
        } else if (secondTime == null) {
            state.select(pair.get(0));
            summary = pair.get(1).title() + "当前没有可用场次；" + pair.get(0).title() + "更早。";
        } else if (firstTime.equals(secondTime)) {
            summary = "这两个演出当前最早场次时间相同，都是" + formatTime(firstTime) + "。";
        } else {
            boolean firstEarlier = firstTime.isBefore(secondTime);
            state.select(firstEarlier ? pair.get(0) : pair.get(1));
            summary = (firstEarlier ? pair.get(0).title() : pair.get(1).title()) + "更早，最早场次为"
                    + formatTime(firstEarlier ? firstTime : secondTime) + "。";
        }
        return new PlanResult(summary, pair.size(), 1);
    }

    private AiTicketToolService.ToolResult executeTool(ArrayNode messages, SseEmitter emitter, AtomicBoolean closed,
                                                       String name, String callId, ObjectNode arguments, RuntimeState state) {
        ensureConnected(closed);
        Long fallbackProjectId = state.selectedProjectId != null ? state.selectedProjectId : state.contextProjectId;
        Long fallbackSessionId = state.selectedSessionId != null ? state.selectedSessionId : state.contextSessionId;
        AiTicketToolService.ToolResult result = callTool(name, arguments, fallbackProjectId, fallbackSessionId);

        state.lastToolSummary = "searchPerformances".equals(name)
                ? responseComposer.search(result) : toolSummary(result.content());

        if ("searchPerformances".equals(name)) {
            applySearchResult(result, state);
        } else {
            if (result.projectId() != null) {
                state.contextProjectId = result.projectId();
                state.selectedProjectId = result.projectId();
            }
            if (result.sessionId() != null) {
                state.contextSessionId = result.sessionId();
                state.selectedSessionId = result.sessionId();
            }
        }

        queueCards(result.cards(), state);
        ObjectNode toolMessage = objectMapper.createObjectNode();
        toolMessage.put("role", "tool");
        toolMessage.put("tool_call_id", callId);
        toolMessage.put("content", result.content());
        messages.add(toolMessage);
        return result;
    }

    private AiTicketToolService.ToolResult callTool(String name, ObjectNode arguments,
                                                     Long fallbackProjectId, Long fallbackSessionId) {
        try {
            return toolService.execute(name, arguments, fallbackProjectId, fallbackSessionId);
        } catch (RuntimeException ex) {
            return new AiTicketToolService.ToolResult(
                    write(Map.of("found", false, "message", safe(ex.getMessage()))), List.of(), null, null);
        }
    }

    private void applySearchResult(AiTicketToolService.ToolResult result, RuntimeState state) {
        SearchExecutionResult execution = result == null ? null : result.searchExecutionResult();
        List<AiSearchResultReference> current = toLastSearchResults(
                execution == null ? result.cards() : execution.items());
        JsonNode content = toolContent(result);
        int resultCount = execution == null ? content.path("resultCount").asInt(current.size()) : execution.matchedTotal();
        boolean hasMore = execution == null ? content.path("hasMore").asBoolean(false) : execution.hasMore();
        boolean continuation = state.action == AiConversationAction.CONTINUE_RESULTS;
        LinkedHashSet<Long> shown = new LinkedHashSet<>();
        if (continuation && state.searchResultState != null && state.searchResultState.shownProjectIds() != null) {
            shown.addAll(state.searchResultState.shownProjectIds());
        }
        for (AiSearchResultReference item : current) {
            if (item != null && item.projectId() != null) shown.add(item.projectId());
        }
        List<Long> currentIds = current.stream().filter(item -> item != null && item.projectId() != null)
                .map(AiSearchResultReference::projectId).toList();
        int nextCursor = execution == null ? shown.size() : execution.nextCursor();
        state.searchResultState = new AiSearchResultState(currentIds, new ArrayList<>(shown), nextCursor, resultCount, hasMore);
        state.lastSearchResults = current;
        state.lastDisplayedCount = current.size();
        if (execution != null && execution.finalContext() != null) state.searchContext = execution.finalContext();
        state.selectedProjectId = null;
        state.selectedSessionId = null;
        String city = state.searchContext == null ? "" : textPolicy.safeText(state.searchContext.city());
        String venue = state.searchContext == null ? "" : textPolicy.safeText(state.searchContext.venue());
        String canonicalVenue = content.path("canonicalVenue").asText("");
        if (!canonicalVenue.isBlank()) {
            venue = canonicalVenue;
            state.searchContext = withVenue(state.searchContext, canonicalVenue);
        }
        Long venueId = state.entityContext == null ? null : state.entityContext.venueId();
        state.entityContext = new AiEntityContext(null, List.of(), city, venue, venueId);
        if (current.size() == 1) {
            state.contextProjectId = current.get(0).projectId();
            state.contextSessionId = current.get(0).sessionId();
        } else {
            state.contextProjectId = null;
            state.contextSessionId = null;
        }
    }

    private AiSearchContext withVenue(AiSearchContext context, String venue) {
        if (context == null) return null;
        return new AiSearchContext(context.city(), context.category(), context.keyword(), venue,
                context.minPrice(), context.maxPrice(), context.startTime(), context.endTime(),
                context.timeIntent(), context.sort());
    }

    private String defaultSummary(AiTicketToolService.ToolResult result, String fallback) {
        String summary = toolSummary(result.content());
        return summary.isBlank() ? fallback : summary;
    }

    private ObjectNode projectArguments(Long projectId, Long sessionId) {
        ObjectNode arguments = objectMapper.createObjectNode();
        if (projectId != null) arguments.put("projectId", projectId);
        if (sessionId != null) arguments.put("sessionId", sessionId);
        return arguments;
    }

    private boolean toolFound(AiTicketToolService.ToolResult result) {
        return toolContent(result).path("found").asBoolean(false);
    }

    private JsonNode toolContent(AiTicketToolService.ToolResult result) {
        try {
            String content = result == null ? "" : result.content();
            return objectMapper.readTree(content == null || content.isBlank() ? "{}" : content);
        } catch (Exception ignored) {
            return objectMapper.createObjectNode();
        }
    }

    private List<AiSearchResultReference> limitedCandidates(
            List<AiSearchResultReference> candidates) {
        if (candidates == null || candidates.isEmpty()) return List.of();
        int size = Math.min(6, candidates.size());
        return new ArrayList<>(candidates.subList(0, size));
    }

    private List<AiSearchResultReference> comparisonPair(RuntimeState state) {
        List<AiSearchResultReference> candidates = state.lastSearchResults;
        if (candidates == null || candidates.size() < 2) return candidates == null ? List.of() : candidates;
        return List.of(candidates.get(0), candidates.get(1));
    }

    private List<PerformanceCardVO> cardsForMatches(List<AiSearchResultReference> matches,
                                                    List<PerformanceCardVO> availableCards) {
        Map<Long, PerformanceCardVO> cardsByProject = new LinkedHashMap<>();
        if (availableCards != null) {
            for (PerformanceCardVO card : availableCards) {
                if (card != null && card.getProjectId() != null) cardsByProject.put(card.getProjectId(), card);
            }
        }
        List<PerformanceCardVO> cards = new ArrayList<>();
        for (AiSearchResultReference match : matches) {
            if (match == null || match.projectId() == null) continue;
            PerformanceCardVO card = cardsByProject.get(match.projectId());
            if (card == null) {
                AiTicketToolService.ToolResult detail = callTool(
                        "getPerformanceDetail", projectArguments(match.projectId(), match.sessionId()), null, null);
                if (detail.cards() != null && !detail.cards().isEmpty()) card = detail.cards().get(0);
            }
            if (card != null) cards.add(card);
        }
        return cards;
    }

    private BigDecimal minimumAvailablePrice(AiTicketToolService.ToolResult result) {
        return availablePrice(result, AiTaskPlanner.CompareMode.MIN_PRICE);
    }

    private BigDecimal availablePrice(AiTicketToolService.ToolResult result, AiTaskPlanner.CompareMode mode) {
        JsonNode skus = toolContent(result).path("ticketSkus");
        if (!skus.isArray()) return null;
        boolean maximum = mode == AiTaskPlanner.CompareMode.MAX_PRICE;
        BigDecimal selected = null;
        for (JsonNode sku : skus) {
            String status = sku.path("skuStatus").asText("").toUpperCase();
            if (sku.path("stockAvailable").asInt(0) <= 0 || "OFFLINE".equals(status) || "SOLD_OUT".equals(status)) continue;
            JsonNode price = sku.path("price");
            if (!price.isNumber() && !price.isTextual()) continue;
            try {
                BigDecimal value = new BigDecimal(price.asText());
                if (selected == null || maximum && value.compareTo(selected) > 0
                        || !maximum && value.compareTo(selected) < 0) selected = value;
            } catch (NumberFormatException ignored) {
            }
        }
        return selected;
    }

    private LocalDateTime earliestSession(AiTicketToolService.ToolResult result) {
        JsonNode sessions = toolContent(result).path("sessions");
        if (!sessions.isArray()) return null;
        LocalDateTime now = LocalDateTime.now(AiSearchSemanticResolver.BUSINESS_ZONE);
        LocalDateTime earliest = null;
        for (JsonNode session : sessions) {
            String status = session.path("sessionStatus").asText("").toUpperCase();
            if ("OFFLINE".equals(status) || "ENDED".equals(status) || "CANCELLED".equals(status)) continue;
            LocalDateTime startTime = parseTime(session.path("startTime").asText(""));
            if (startTime == null || startTime.isBefore(now)) continue;
            if (earliest == null || startTime.isBefore(earliest)) earliest = startTime;
        }
        return earliest;
    }

    private boolean hasWeekendSession(AiTicketToolService.ToolResult result) {
        JsonNode sessions = toolContent(result).path("sessions");
        if (!sessions.isArray()) return false;
        LocalDateTime now = LocalDateTime.now(AiSearchSemanticResolver.BUSINESS_ZONE);
        for (JsonNode session : sessions) {
            String status = session.path("sessionStatus").asText("").toUpperCase();
            if ("OFFLINE".equals(status) || "ENDED".equals(status) || "CANCELLED".equals(status)) continue;
            LocalDateTime startTime = parseTime(session.path("startTime").asText(""));
            if (startTime == null || startTime.isBefore(now)) continue;
            if (startTime.getDayOfWeek() == DayOfWeek.SATURDAY || startTime.getDayOfWeek() == DayOfWeek.SUNDAY) return true;
        }
        return false;
    }

    private LocalDateTime parseTime(String value) {
        try {
            return LocalDateTime.parse(textPolicy.safeText(value).replace(' ', 'T'));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String joinTitles(List<AiSearchResultReference> results) {
        List<String> titles = new ArrayList<>();
        for (AiSearchResultReference result : results) {
            if (result != null && result.title() != null && !result.title().isBlank()) titles.add(result.title());
        }
        return titles.isEmpty() ? "符合条件的演出" : String.join("、", titles);
    }

    private String formatPrice(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "" : value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }

    private ObjectNode fallbackAssistant(String toolName, ObjectNode arguments) {
        ObjectNode assistant = objectMapper.createObjectNode();
        assistant.put("role", "assistant");
        assistant.put("content", "");
        ArrayNode calls = objectMapper.createArrayNode();
        ObjectNode call = objectMapper.createObjectNode();
        call.put("id", "fallback_1");
        call.put("type", "function");
        ObjectNode function = objectMapper.createObjectNode();
        function.put("name", toolName);
        function.put("arguments", write(arguments));
        call.set("function", function);
        calls.add(call);
        assistant.set("tool_calls", calls);
        return assistant;
    }

    private JsonNode candidateArgumentsForTool(JsonNode assistantMessage, String requiredTool) {
        JsonNode toolCalls = assistantMessage == null ? null : assistantMessage.path("tool_calls");
        if (toolCalls == null || !toolCalls.isArray()) return objectMapper.createObjectNode();
        for (JsonNode toolCall : toolCalls) {
            if (!requiredTool.equals(toolCall.path("function").path("name").asText(""))) continue;
            return parseArguments(toolCall.path("function").path("arguments").asText("{}"));
        }
        return objectMapper.createObjectNode();
    }

    private ArrayNode buildMessages(AiChatRequest request, RuntimeState state, AiIntent intent) {
        if (request == null || request.messages() == null || request.messages().isEmpty()) {
            throw new IllegalArgumentException("请输入找票问题");
        }
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode system = objectMapper.createObjectNode();
        system.put("role", "system");
        String systemContent = systemPrompt(request, state, intent);
        system.put("content", systemContent);
        messages.add(system);
        boolean conversational = isConversationIntent(intent);
        String latestText = textPolicy.latestUserText(request);
        AiConversationTextPolicy.TranslationRequest translation = textPolicy.translationRequest(request);
        boolean keepNaturalHistory = conversational && !translation.found() && textPolicy.needsNaturalHistory(latestText);
        int historyLimit = conversational ? (keepNaturalHistory ? 5 : 1) : 20;
        int contentLimit = conversational ? 600 : 2000;
        int start = Math.max(0, request.messages().size() - historyLimit);
        int latestUserIndex = -1;
        for (int index = request.messages().size() - 1; index >= 0; index--) {
            AiChatRequest.AiChatMessage item = request.messages().get(index);
            if (item != null && !"assistant".equalsIgnoreCase(textPolicy.safeText(item.role()))
                    && item.content() != null && !item.content().isBlank()) {
                latestUserIndex = index;
                break;
            }
        }
        if (conversational && !keepNaturalHistory && latestUserIndex >= 0) start = latestUserIndex;
        List<String> roles = new ArrayList<>();
        roles.add("system");
        for (int index = start; index < request.messages().size(); index++) {
            AiChatRequest.AiChatMessage item = request.messages().get(index);
            if (item == null || item.content() == null || item.content().isBlank()) continue;
            String role = textPolicy.safeText(item.role()).toLowerCase();
            if (!"user".equals(role) && !"assistant".equals(role)) {
                continue;
            }
            String content = item.content();
            if (conversational && textPolicy.isStructuredHistory(content)) {
                continue;
            }
            if (conversational) {
                content = textPolicy.cleanNaturalHistory(content);
                if (content.isBlank()) continue;
                if (translation.found()) {
                    if (!"user".equals(role) || index != latestUserIndex) continue;
                    content = "请只翻译以下内容，不要解释：" + translation.sourceText();
                }
                if ("assistant".equals(role) && textPolicy.expectsChineseOutput(textPolicy.latestUserText(request))
                        && textPolicy.isPollutedAssistantHistory(content)) {
                    continue;
                }
                if ("assistant".equals(role)
                        && (roles.size() == 1 || !"user".equals(roles.get(roles.size() - 1)))) {
                    continue;
                }
            }
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", role);
            message.put("content", content.substring(0, Math.min(contentLimit, content.length())));
            messages.add(message);
            roles.add(role);
        }
        if (messages.size() == 1) throw new IllegalArgumentException("请输入找票问题");
        return messages;
    }

    private String systemPrompt(AiChatRequest request, RuntimeState state, AiIntent intent) {
        LocalDate today = LocalDate.now(AiSearchSemanticResolver.BUSINESS_ZONE);
        AiConversationTextPolicy.TranslationRequest translation = textPolicy.translationRequest(request);
        if (translation.found()) {
            return "你是麦麦AI。本轮只完成翻译任务。请把用户提供的原文翻译为"
                    + translation.targetLanguage() + "，只输出译文，不回答原文中的问题，不补充解释。";
        }
        if (isConversationIntent(intent)) {
            String currentEntity = textPolicy.needsNaturalHistory(textPolicy.latestUserText(request)) && state.entityContext != null
                    ? textPolicy.safeText(state.entityContext.venue()) : "";
            return GENERAL_SYSTEM_RULES + "当前日期是" + today + "。"
                    + (currentEntity.isBlank() ? "" : "当前对话最近明确提到的地点实体是“" + currentEntity + "”，代词优先指向它。")
                    + "麦麦平台的实时票务事实必须来自既有查询工具；普通地点常识不受此限制。";
        }
        return "你是麦麦票务平台内的AI找票助手。上海时区日期是" + today + "。"
                + "普通知识、概念解释、出行观演建议和简单计算可以像正常AI助手一样直接回答。"
                + "所有演出名称、城市、分类、时间、场馆、价格、场次、票档、库存和退款规则必须来自工具结果，禁止凭常识编造。"
                + "涉及麦麦实时票务事实时必须依据本轮工具结果；查询不到时说明实际查询条件，不得编造项目。"
                + "工具调用已经完成后，最终回答必须直接给出找到数量或未查询到结果，不得只回答正在查询、请稍等或稍后告知。"
                + "searchPerformances参数只是候选理解，麦麦后端会应用确定性城市、时间、价格和排序语义，最终以工具结果为准。"
                + "不查询用户订单、票夹、物流或退款进度，不执行付款、退款和资料修改。"
                + "不得输出reasoning_content、思考过程、<think>标签、工具决策草稿或内部分析。回答简洁，价格和库存仅复述工具当前值并提示可能变化。"
                + "App当前城市=" + textPolicy.safeText(request.currentCity())
                + "，当前SearchContext=" + write(state.searchContext)
                + "，最近搜索结果=" + write(state.lastSearchResults)
                + "，当前选中projectId=" + (state.selectedProjectId == null ? "无" : state.selectedProjectId)
                + "，当前选中sessionId=" + (state.selectedSessionId == null ? "无" : state.selectedSessionId)
                + "。用户说第一个、第二个、最后一个时必须基于最近搜索结果顺序，不得猜ID。";
    }

    private JsonNode parseArguments(String value) {
        try { return objectMapper.readTree(value == null || value.isBlank() ? "{}" : value); }
        catch (Exception ex) { return objectMapper.createObjectNode(); }
    }

    private String text(JsonNode node, String name) {
        if (node == null) return "";
        JsonNode value = node.path(name);
        return value.isTextual() ? value.asText("").trim() : "";
    }

    private String toolSummary(String content) {
        try {
            JsonNode value = objectMapper.readTree(content == null || content.isBlank() ? "{}" : content);
            return textPolicy.safeText(value.path("message").asText(""));
        } catch (Exception ignored) {
            return "";
        }
    }

    private boolean isProgressPrefixCandidate(String value) {
        String normalized = value == null ? "" : value.stripLeading();
        if (normalized.isEmpty()) return true;
        for (String prefix : List.of("正在", "我正在", "正在为您查询", "稍等", "请稍等", "马上为您查询", "我来为您查询")) {
            if (prefix.startsWith(normalized) || normalized.startsWith(prefix)) return true;
        }
        return false;
    }

    private boolean isProgressResponse(String value) {
        String normalized = value == null ? "" : value.stripLeading();
        for (String prefix : List.of("正在", "我正在", "稍等", "请稍等", "马上为您查询", "我来为您查询")) {
            if (normalized.startsWith(prefix)) return true;
        }
        return false;
    }

    private void emitCards(SseEmitter emitter, AtomicBoolean closed, List<PerformanceCardVO> cards, Set<Long> emittedCards) {
        if (cards == null) return;
        for (PerformanceCardVO card : cards) {
            if (card == null || card.getProjectId() == null || !emittedCards.add(card.getProjectId())) continue;
            send(emitter, closed, "PERFORMANCE_CARD", card);
        }
    }

    private void queueCards(List<PerformanceCardVO> cards, RuntimeState state) {
        if (cards == null) return;
        for (PerformanceCardVO card : cards) {
            if (card == null || card.getProjectId() == null || state.emittedCards.contains(card.getProjectId())) continue;
            state.pendingCards.putIfAbsent(card.getProjectId(), card);
        }
    }

    private void flushCards(SseEmitter emitter, AtomicBoolean closed, RuntimeState state, TimingState timing) {
        if (state.pendingCards.isEmpty()) return;
        timing.markFirstCard();
        emitCards(emitter, closed, new ArrayList<>(state.pendingCards.values()), state.emittedCards);
        state.pendingCards.clear();
    }

    private void sendTextChunks(SseEmitter emitter, AtomicBoolean closed, String text, TimingState timing) {
        String value = textPolicy.safeText(text);
        if (value.isEmpty()) return;
        int offset = 0;
        while (offset < value.length()) {
            int end = Math.min(value.length(), offset + 18);
            if (end < value.length()) {
                int punctuation = -1;
                for (int index = end - 1; index > offset + 5; index--) {
                    if ("，。；！？、".indexOf(value.charAt(index)) >= 0) {
                        punctuation = index + 1;
                        break;
                    }
                }
                if (punctuation > offset) end = punctuation;
            }
            timing.markFirstText();
            send(emitter, closed, "TEXT", Map.of("text", value.substring(offset, end)));
            offset = end;
        }
    }

    private void finishResponse(SseEmitter emitter, AtomicBoolean closed, RuntimeState state, TimingState timing) {
        flushCards(emitter, closed, state, timing);
        send(emitter, closed, "DONE", donePayload(state));
        completeSafely(emitter, closed);
    }

    private void send(SseEmitter emitter, AtomicBoolean closed, String event, Object data) {
        ensureConnected(closed);
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException | IllegalStateException ex) {
            closed.set(true);
            throw new SseDisconnectedException();
        }
    }

    private void completeSafely(SseEmitter emitter, AtomicBoolean closed) {
        if (closed.get()) return;
        try {
            emitter.complete();
        } catch (RuntimeException ignored) {
        } finally {
            closed.set(true);
        }
    }

    private void ensureConnected(AtomicBoolean closed) {
        if (closed.get()) throw new SseDisconnectedException();
    }

    private boolean isDisconnect(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SseDisconnectedException || current instanceof AsyncRequestNotUsableException) return true;
            String message = current.getMessage();
            if (message != null && (message.contains("Broken pipe") || message.contains("Connection reset")
                    || message.contains("已建立的连接") || message.contains("connection was aborted"))) return true;
            current = current.getCause();
        }
        return false;
    }

    private Map<String, Object> donePayload(RuntimeState state) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("contextProjectId", state.contextProjectId == null ? "" : String.valueOf(state.contextProjectId));
        data.put("contextSessionId", state.contextSessionId == null ? "" : String.valueOf(state.contextSessionId));
        data.put("searchContext", state.searchContext);
        data.put("lastSearchResults", state.lastSearchResults);
        data.put("selectedProjectId", state.selectedProjectId == null ? "" : String.valueOf(state.selectedProjectId));
        data.put("selectedSessionId", state.selectedSessionId == null ? "" : String.valueOf(state.selectedSessionId));
        data.put("searchResultState", state.searchResultState);
        data.put("entityContext", state.entityContext);
        data.put("activeSlot", state.activeSlot);
        data.put("capabilities", isConversationIntent(state.intent) ? List.of()
                : capabilityRegistry.supported(state.searchContext, state.searchResultState,
                state.selectedProjectId, state.selectedSessionId));
        return data;
    }

    private boolean isConversationIntent(AiIntent intent) {
        return intent == AiIntent.GENERAL_CHAT || intent == AiIntent.ENTITY_QA;
    }

    private boolean isTicketFactIntent(AiIntent intent) {
        return intent != null && !isConversationIntent(intent);
    }

    private List<AiSearchResultReference> toLastSearchResults(List<PerformanceCardVO> cards) {
        if (cards == null || cards.isEmpty()) return List.of();
        List<AiSearchResultReference> results = new ArrayList<>();
        for (PerformanceCardVO card : cards) {
            if (card == null || card.getProjectId() == null) continue;
            results.add(new AiSearchResultReference(card.getProjectId(), card.getSessionId(), card.getTitle()));
        }
        return results;
    }

    private String write(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception ex) { return "null"; }
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) return "麦麦AI请求失败，请稍后重试";
        return value.substring(0, Math.min(500, value.length()));
    }

    private String deterministicGeneralAnswer(String userText, AiSemanticParseResult semantic) {
        List<VenueFact> venues = venueResolver.resolveKnowledgeEntities(userText);
        if (!venues.isEmpty() && isVenueKnowledgeQuestion(userText)) return venueKnowledgeAnswer(venues);
        if (semanticResolver.isTimeKnowledgeQuestion(userText) && semantic != null) {
            String timeIntent = semantic.recognizedSlots().getOrDefault(AiActiveSlot.TIME.name(), "");
            return timeKnowledgeAnswer(timeIntent);
        }
        return "";
    }

    private boolean isVenueKnowledgeQuestion(String text) {
        String value = textPolicy.safeText(text);
        return containsAny(value, "是什么", "是不是", "是否是", "在哪里", "在哪儿", "什么地方", "同一个",
                "什么关系", "一样吗", "相同吗", "有何区别", "有什么区别", "区别");
    }

    private String venueKnowledgeAnswer(List<VenueFact> facts) {
        VenueFact first = facts.get(0);
        if (facts.size() > 1) {
            VenueFact second = facts.get(1);
            if (first.entityId() != null && first.entityId().equals(second.entityId())) {
                return "“" + first.matchedText() + "”与“" + second.matchedText() + "”指的是同一场馆："
                        + first.canonicalName() + venueLocation(first) + "。";
            }
            return first.canonicalName() + "与" + second.canonicalName() + "是两个不同的场馆。"
                    + first.canonicalName() + venueLocation(first) + "；" + second.canonicalName() + venueLocation(second) + "。";
        }
        String alias = textPolicy.safeText(first.matchedText()).equals(textPolicy.safeText(first.canonicalName()))
                ? first.canonicalName() : "“" + first.matchedText() + "”是" + first.canonicalName() + "的常用名称";
        return alias + venueLocation(first) + "。";
    }

    private String venueLocation(VenueFact fact) {
        String city = textPolicy.safeText(fact.city());
        String address = textPolicy.safeText(fact.address());
        if (!address.isBlank()) return "，位于" + (city.isBlank() ? "" : city) + address;
        return city.isBlank() ? "" : "，位于" + city;
    }

    private String timeKnowledgeAnswer(String intent) {
        String value = textPolicy.safeText(intent).toUpperCase();
        if ("NEXT_7_DAYS".equals(value)) return "在麦麦票务搜索中，“近一周”指从现在起到未来7天。";
        if ("NEXT_30_DAYS".equals(value) || "RECENT".equals(value)) {
            return "在麦麦票务搜索中，“最近”或“近期”指从现在起到未来30天。";
        }
        if (value.matches("NEXT_\\d+_DAYS")) {
            String days = value.substring("NEXT_".length(), value.length() - "_DAYS".length());
            return "在麦麦票务搜索中，该时间范围指从现在起到未来" + days + "天。";
        }
        return "";
    }

    private record PlanResult(String summary, int candidateCount, int toolSteps) {
    }

    private static final class RuntimeState {
        private Long contextProjectId;
        private Long contextSessionId;
        private Long selectedProjectId;
        private Long selectedSessionId;
        private AiSearchContext searchContext;
        private List<AiSearchResultReference> lastSearchResults;
        private AiConversationAction action = AiConversationAction.GENERAL_CHAT;
        private AiIntent intent = AiIntent.GENERAL_CHAT;
        private AiSearchResultState searchResultState;
        private AiEntityContext entityContext;
        private AiActiveSlot activeSlot;
        private AiSemanticParseResult.Confidence semanticConfidence = AiSemanticParseResult.Confidence.LOW;
        private final Set<Long> emittedCards = new LinkedHashSet<>();
        private final Map<Long, PerformanceCardVO> pendingCards = new LinkedHashMap<>();
        private String lastToolSummary = "";
        private int lastDisplayedCount;
        private final String requestId = UUID.randomUUID().toString();
        private final String conversationId;

        private RuntimeState(AiChatRequest request) {
            conversationId = request == null ? "" : safeValue(request.conversationId());
            contextProjectId = request == null ? null : request.contextProjectId();
            contextSessionId = request == null ? null : request.contextSessionId();
            selectedProjectId = request == null ? null : request.selectedProjectId();
            selectedSessionId = request == null ? null : request.selectedSessionId();
            searchContext = request == null ? null : request.searchContext();
            lastSearchResults = request == null || request.lastSearchResults() == null
                    ? List.of() : request.lastSearchResults();
            searchResultState = request == null || request.searchResultState() == null
                    ? AiSearchResultState.empty() : request.searchResultState();
            entityContext = request == null || request.entityContext() == null
                    ? AiEntityContext.empty() : request.entityContext();
            activeSlot = request == null || request.activeSlot() == null ? AiActiveSlot.NONE : request.activeSlot();
        }

        private void clearSearchReferences() {
            contextProjectId = null;
            contextSessionId = null;
            selectedProjectId = null;
            selectedSessionId = null;
            lastSearchResults = List.of();
            searchResultState = AiSearchResultState.empty();
            entityContext = new AiEntityContext(null, List.of(),
                    entityContext == null ? "" : entityContext.city(),
                    entityContext == null ? "" : entityContext.venue(),
                    entityContext == null ? null : entityContext.venueId());
            activeSlot = AiActiveSlot.NONE;
        }

        private void setCurrentResults(List<AiSearchResultReference> results) {
            lastSearchResults = results == null ? List.of() : new ArrayList<>(results);
            List<Long> ids = lastSearchResults.stream().filter(item -> item != null && item.projectId() != null)
                    .map(AiSearchResultReference::projectId).toList();
            List<Long> shown = searchResultState == null || searchResultState.shownProjectIds() == null
                    ? ids : searchResultState.shownProjectIds();
            searchResultState = new AiSearchResultState(ids, shown, shown.size(),
                    searchResultState == null ? ids.size() : searchResultState.resultCount(),
                    searchResultState != null && searchResultState.hasMore());
            selectedProjectId = null;
            selectedSessionId = null;
            if (lastSearchResults.size() == 1) {
                select(lastSearchResults.get(0));
            } else {
                contextProjectId = null;
                contextSessionId = null;
            }
        }

        private void moveResultToFront(AiSearchResultReference selected) {
            if (selected == null || selected.projectId() == null) return;
            List<AiSearchResultReference> reordered = new ArrayList<>();
            reordered.add(selected);
            for (AiSearchResultReference result : lastSearchResults) {
                if (result != null && !selected.projectId().equals(result.projectId())) reordered.add(result);
            }
            lastSearchResults = reordered;
        }

        private void select(AiSearchResultReference result) {
            if (result == null) return;
            selectedProjectId = result.projectId();
            selectedSessionId = result.sessionId();
            contextProjectId = result.projectId();
            contextSessionId = result.sessionId();
            entityContext = new AiEntityContext(result.projectId(),
                    entityContext == null ? List.of() : entityContext.comparedProjectIds(),
                    entityContext == null ? "" : entityContext.city(),
                    entityContext == null ? "" : entityContext.venue(),
                    entityContext == null ? null : entityContext.venueId());
        }

        private void setCompared(List<AiSearchResultReference> results) {
            List<Long> ids = results == null ? List.of() : results.stream()
                    .filter(item -> item != null && item.projectId() != null)
                    .map(AiSearchResultReference::projectId).limit(2).toList();
            entityContext = new AiEntityContext(entityContext == null ? null : entityContext.mentionedProjectId(), ids,
                    entityContext == null ? "" : entityContext.city(),
                    entityContext == null ? "" : entityContext.venue(),
                    entityContext == null ? null : entityContext.venueId());
        }

        private void rememberVenue(AiResolvedVenue venue) {
            if (venue == null || !venue.found()) return;
            entityContext = new AiEntityContext(entityContext == null ? null : entityContext.mentionedProjectId(),
                    entityContext == null ? List.of() : entityContext.comparedProjectIds(),
                    safeValue(venue.city()), safeValue(venue.canonicalName()), venue.entityId());
        }

        private static String safeValue(String value) {
            return value == null ? "" : value.trim();
        }
    }

    private static final class TimingState {
        private final long startedAt = System.nanoTime();
        private long toolStartedAt;
        private long semanticMs;
        private long toolMs;
        private long firstTextMs = -1;
        private long firstCardMs = -1;

        private long elapsedMs() {
            return (System.nanoTime() - startedAt) / 1_000_000L;
        }

        private void startTool() {
            toolStartedAt = System.nanoTime();
        }

        private void finishTool() {
            if (toolStartedAt > 0) toolMs += (System.nanoTime() - toolStartedAt) / 1_000_000L;
            toolStartedAt = 0;
        }

        private void markFirstText() {
            if (firstTextMs < 0) firstTextMs = elapsedMs();
        }

        private void markFirstCard() {
            if (firstCardMs < 0) firstCardMs = elapsedMs();
        }
    }

    private static final class GeneralStreamGuard {
        private static final int MAX_CHARS = 1200;
        private final boolean expectChinese;
        private final StringBuilder emitted = new StringBuilder();
        private boolean stopped;
        private boolean languageViolation;
        private String stopReason = "PROVIDER_DONE";

        private GeneralStreamGuard(boolean expectChinese) {
            this.expectChinese = expectChinese;
        }

        private String accept(String chunk) {
            if (stopped || chunk == null || chunk.isEmpty()) return "";
            String value = stripRoleLeakage(chunk);
            if (value.isEmpty()) return "";
            StringBuilder accepted = new StringBuilder();
            for (int index = 0; index < value.length(); index++) {
                if (emitted.length() >= MAX_CHARS) {
                    stopped = true;
                    stopReason = "LENGTH_GUARD";
                    break;
                }
                char next = value.charAt(index);
                emitted.append(next);
                accepted.append(next);
                if (isSentenceBoundary(next) && hasRepeatedTail(emitted)) {
                    stopped = true;
                    stopReason = "REPETITION_GUARD";
                    break;
                }
            }
            if (expectChinese && emitted.length() >= 80 && isEnglishDominant(emitted)) languageViolation = true;
            return accepted.toString();
        }

        private boolean stopped() {
            return stopped;
        }

        private boolean languageViolation() {
            return languageViolation;
        }

        private static boolean isEnglishDominant(CharSequence value) {
            int han = 0;
            int latin = 0;
            for (int index = 0; index < value.length(); index++) {
                char current = value.charAt(index);
                if (Character.UnicodeScript.of(current) == Character.UnicodeScript.HAN) han++;
                else if ((current >= 'a' && current <= 'z') || (current >= 'A' && current <= 'Z')) latin++;
            }
            return latin >= 24 && latin > Math.max(12, han);
        }

        private double englishRatio() {
            int han = 0;
            int latin = 0;
            for (int index = 0; index < emitted.length(); index++) {
                char current = emitted.charAt(index);
                if (Character.UnicodeScript.of(current) == Character.UnicodeScript.HAN) han++;
                else if ((current >= 'a' && current <= 'z') || (current >= 'A' && current <= 'Z')) latin++;
            }
            int total = han + latin;
            return total == 0 ? 0D : Math.round((latin * 10000D / total)) / 10000D;
        }

        private String stopReason() {
            return stopReason;
        }

        private static boolean isSentenceBoundary(char value) {
            return value == '。' || value == '！' || value == '？' || value == '!' || value == '?' || value == '\n';
        }

        private static String stripRoleLeakage(String value) {
            return value.replaceAll("(?im)(^|\\R)\\s*(?:system|assistant|tool|developer)\\s*[:：]\\s*", "$1");
        }

        private static boolean hasRepeatedTail(StringBuilder value) {
            int length = value.length();
            for (int unit = 12; unit <= Math.min(160, length / 3); unit++) {
                int start = length - unit * 3;
                if (same(value, start, start + unit, unit) && same(value, start + unit, start + unit * 2, unit)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean same(StringBuilder value, int left, int right, int length) {
            for (int index = 0; index < length; index++) {
                if (value.charAt(left + index) != value.charAt(right + index)) return false;
            }
            return true;
        }
    }

    private static final class SseDisconnectedException extends IllegalStateException {
        private SseDisconnectedException() {
            super("SSE disconnected");
        }
    }

    private static final class GeneralGenerationStoppedException extends IllegalStateException {
        private GeneralGenerationStoppedException() {
            super("GENERAL_GENERATION_STOPPED");
        }
    }
}
