package com.example.maimaibackend.ai.application.orchestration;

import com.example.maimaibackend.ai.domain.action.AiConversationAction;
import com.example.maimaibackend.ai.domain.intent.AiIntent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiTaskPlanner {
    public TaskPlan plan(AiIntent intent, AiConversationAction action, String userText, boolean hasCandidates) {
        return plan(intent, action, userText, hasCandidates, false);
    }

    public TaskPlan plan(AiIntent intent, AiConversationAction action, String userText,
                         boolean hasCandidates, boolean hasSelectedEntity) {
        String text = userText == null ? "" : userText.trim();
        if (action == AiConversationAction.COMPARE_RESULTS) {
            if (containsAny(text, "早", "场次", "周末", "时间")) {
                return new TaskPlan(TaskType.COMPARE_SESSION, CandidateSource.CURRENT_RESULTS,
                        List.of("getSessions"), "session", CompareMode.EARLIEST);
            }
            if (isSuperlativeSelection(text)) {
                CompareMode mode = containsAny(text, "最贵", "最高") ? CompareMode.MAX_PRICE : CompareMode.MIN_PRICE;
                return new TaskPlan(TaskType.SELECT_PRICE, CandidateSource.CURRENT_RESULTS,
                        List.of("getTicketSkus"), "price", mode);
            }
            CompareMode mode = containsAny(text, "更贵", "价格更高") ? CompareMode.MAX_PRICE : CompareMode.MIN_PRICE;
            return new TaskPlan(TaskType.COMPARE_PRICE, CandidateSource.CURRENT_RESULTS,
                    List.of("getTicketSkus"), "price", mode);
        }
        boolean collectionReference = isCollectionQuestion(text);
        boolean specificReference = hasSpecificReference(text) || hasSelectedEntity && !collectionReference;
        boolean refundFilter = intent == AiIntent.REFUND_QUERY && hasCandidates && !specificReference && collectionReference;
        boolean availabilityFilter = intent == AiIntent.TICKET_QUERY && hasCandidates && !specificReference && collectionReference;
        if (intent == AiIntent.REFUND_QUERY && hasSearchConstraint(text)) {
            return new TaskPlan(TaskType.SEARCH_FILTER_REFUND, CandidateSource.SEARCH_RESULTS,
                    List.of("searchPerformances", "getRefundRule"), "refundable", CompareMode.NONE);
        }
        if (refundFilter) {
            return new TaskPlan(TaskType.FILTER_REFUND, CandidateSource.CURRENT_RESULTS,
                    List.of("getRefundRule"), "refundable", CompareMode.NONE);
        }
        if (availabilityFilter) {
            return new TaskPlan(TaskType.FILTER_AVAILABILITY, CandidateSource.CURRENT_RESULTS,
                    List.of("getTicketSkus"), "availableStock", CompareMode.NONE);
        }
        return new TaskPlan(TaskType.DIRECT_INTENT, CandidateSource.SELECTED_ENTITY, List.of(), "", CompareMode.NONE);
    }

    private boolean isCollectionQuestion(String text) {
        return containsAny(text, "这些", "当前这些", "这批", "哪些", "有没有", "其他", "其它", "都", "演出");
    }

    private boolean hasSpecificReference(String text) {
        String compact = text == null ? "" : text.replaceAll("\\s+", "");
        return compact.matches(".*第(?:[一二三四五六七八]|[1-8])(?:个|场|项).*+")
                || containsAny(compact, "最后一个", "这个演出", "该演出", "它", "刚才那个");
    }

    private boolean hasSearchConstraint(String text) {
        return containsAny(text, "今天", "明天", "周", "月", "最近", "近期", "以内", "以下", "以上", "元",
                "演唱会", "音乐会", "音乐节", "话剧", "展览", "体育", "在", "换到");
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }

    private boolean isSuperlativeSelection(String text) {
        return containsAny(text, "最便宜", "价格最低", "票价最低", "最低价", "最贵", "价格最高", "票价最高", "最高价")
                && containsAny(text, "哪个", "哪一个", "哪场", "是谁", "是哪个", "是哪一个", "这几个");
    }

    public record TaskPlan(
            TaskType taskType,
            CandidateSource candidateSource,
            List<String> requiredTools,
            String filter,
            CompareMode compareMode
    ) {
    }

    public enum TaskType {
        DIRECT_INTENT,
        FILTER_REFUND,
        FILTER_AVAILABILITY,
        COMPARE_PRICE,
        SELECT_PRICE,
        COMPARE_SESSION,
        SEARCH_FILTER_REFUND
    }

    public enum CandidateSource {
        SELECTED_ENTITY,
        CURRENT_RESULTS,
        SEARCH_RESULTS
    }

    public enum CompareMode {
        NONE,
        PRICE,
        MIN_PRICE,
        MAX_PRICE,
        EARLIEST
    }
}
