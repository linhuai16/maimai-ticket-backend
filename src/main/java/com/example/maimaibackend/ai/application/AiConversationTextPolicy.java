package com.example.maimaibackend.ai.application;

import com.example.maimaibackend.ai.api.dto.AiChatRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
class AiConversationTextPolicy {
    boolean isStructuredHistory(String content) {
        String value = safeText(content);
        if (value.startsWith("{") || value.startsWith("[")) return true;
        String lower = value.toLowerCase();
        return lower.contains("tool_calls") || lower.contains("searchcontext")
                || lower.contains("searchresultstate") || lower.contains("performance_card")
                || lower.contains("reasoning_content") || lower.contains("\"projectid\"")
                || lower.contains("\"sessionid\"") || lower.contains("<think>");
    }

    String cleanNaturalHistory(String content) {
        return safeText(content).replaceAll("(?is)<think>.*?</think>", "")
                .replaceAll("(?im)^\\s*(?:system|assistant|tool)\\s*[:：]\\s*", "")
                .trim();
    }

    boolean needsNaturalHistory(String content) {
        String value = content == null ? "" : content.trim();
        if (isIndependentDefinitionQuestion(value)) return false;
        return List.of("它", "这个", "那个", "这些", "那些", "上述", "前面", "刚才",
                        "继续", "接着", "再说", "再讲", "还是", "那呢", "那么", "然后", "为什么呢")
                .stream().anyMatch(value::contains)
                || value.matches("(?s).{0,24}(?:呢|吗)$") && value.length() <= 12;
    }

    boolean isIndependentDefinitionQuestion(String content) {
        String value = content == null ? "" : content.trim();
        if (value.contains("它") || value.contains("这个") || value.contains("那个") || value.contains("刚才")) {
            return false;
        }
        return value.matches("(?s)^[^，。！？?]{1,40}(?:是什么|是谁|指什么|是什么意思)[？?]?$");
    }

    boolean isPollutedAssistantHistory(String content) {
        String value = content == null ? "" : content.trim();
        if (value.isEmpty()) return true;
        String lower = value.toLowerCase();
        if (lower.contains("problem statement") || lower.contains("answer is")
                || lower.contains("\\boxed") || lower.contains("reasoning_content")
                || lower.contains("<think>")) return true;
        if (value.contains("语言输出异常") || value.contains("请求失败")
                || value.contains("生成已停止") || value.contains("已停止生成")
                || value.contains("请重新生成")) return true;
        return isEnglishDominant(value);
    }

    String latestUserText(AiChatRequest request) {
        if (request == null || request.messages() == null) return "";
        for (int index = request.messages().size() - 1; index >= 0; index--) {
            AiChatRequest.AiChatMessage message = request.messages().get(index);
            if (message != null && !"assistant".equals(message.role())
                    && message.content() != null && !message.content().isBlank()) {
                return message.content().trim();
            }
        }
        return "";
    }

    String previousUserText(AiChatRequest request) {
        if (request == null || request.messages() == null) return "";
        boolean skippedLatest = false;
        for (int index = request.messages().size() - 1; index >= 0; index--) {
            AiChatRequest.AiChatMessage message = request.messages().get(index);
            if (message == null || "assistant".equals(message.role())
                    || message.content() == null || message.content().isBlank()) continue;
            if (!skippedLatest) {
                skippedLatest = true;
                continue;
            }
            return message.content().trim();
        }
        return "";
    }

    String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    boolean referencesCurrentEntity(String value) {
        String text = safeText(value);
        return text.contains("它") || text.contains("这个场馆") || text.contains("那里") || text.contains("该场馆");
    }

    boolean expectsChineseOutput(String value) {
        if (!containsCjk(value)) return false;
        TranslationRequest translation = parseTranslationRequest(value);
        if (translation.found()) return !"英文".equals(translation.targetLanguage());
        String text = safeText(value);
        return !text.matches("(?is).*(?:用|以|翻译成|翻译为|回答成|输出为).{0,4}(?:英文|英语|english).*");
    }

    TranslationRequest translationRequest(AiChatRequest request) {
        TranslationRequest parsed = parseTranslationRequest(latestUserText(request));
        if (!parsed.found() || !isContextualTranslationSource(parsed.sourceText())) return parsed;
        String previous = previousUserText(request);
        return previous.isBlank() ? TranslationRequest.empty()
                : new TranslationRequest(true, previous, parsed.targetLanguage());
    }

    private boolean isEnglishDominant(CharSequence value) {
        int han = 0;
        int latin = 0;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (Character.UnicodeScript.of(current) == Character.UnicodeScript.HAN) han++;
            else if ((current >= 'a' && current <= 'z') || (current >= 'A' && current <= 'Z')) latin++;
        }
        return latin >= 24 && latin > Math.max(12, han);
    }

    private boolean containsCjk(String value) {
        if (value == null) return false;
        for (int index = 0; index < value.length(); index++) {
            if (Character.UnicodeScript.of(value.charAt(index)) == Character.UnicodeScript.HAN) return true;
        }
        return false;
    }

    private TranslationRequest parseTranslationRequest(String input) {
        String value = input == null ? "" : input.trim();
        if (value.isEmpty()) return TranslationRequest.empty();
        Matcher chinesePrefix = Pattern.compile(
                "(?is)^\\s*(?:请)?(?:把|将)\\s*[“\\\"']?(.+?)[”\\\"']?\\s*翻译(?:成|为)\\s*(英文|英语|中文|汉语|简体中文)\\s*[。.!！]*$")
                .matcher(value);
        if (chinesePrefix.matches()) return translation(chinesePrefix.group(1), chinesePrefix.group(2));
        Matcher chineseSuffix = Pattern.compile(
                "(?is)^\\s*[“\\\"']?(.+?)[”\\\"']?\\s*(?:这句话|这段话|这段文字)?\\s*(?:请)?翻译(?:成|为)\\s*(英文|英语|中文|汉语|简体中文)\\s*[。.!！]*$")
                .matcher(value);
        if (chineseSuffix.matches()) return translation(chineseSuffix.group(1), chineseSuffix.group(2));
        Matcher englishPrefix = Pattern.compile(
                "(?is)^\\s*translate\\s+[\\\"']?(.+?)[\\\"']?\\s+into\\s+(english|chinese)\\s*[.!]*$")
                .matcher(value);
        if (englishPrefix.matches()) return translation(englishPrefix.group(1), englishPrefix.group(2));
        Matcher englishSuffix = Pattern.compile(
                "(?is)^\\s*[\\\"']?(.+?)[\\\"']?\\s+(?:please\\s+)?translate\\s+into\\s+(english|chinese)\\s*[.!]*$")
                .matcher(value);
        if (englishSuffix.matches()) return translation(englishSuffix.group(1), englishSuffix.group(2));
        return TranslationRequest.empty();
    }

    private boolean isContextualTranslationSource(String source) {
        String value = source == null ? "" : source.trim();
        return List.of("这句话", "这段话", "这段文字", "上述内容", "上面这句话", "上面这段话").contains(value);
    }

    private TranslationRequest translation(String source, String target) {
        String cleanSource = source == null ? "" : source.trim()
                .replaceAll("^[“\\\"']+|[”\\\"']+$", "").trim();
        String normalizedTarget = target == null ? "" : target.trim().toLowerCase();
        String language = normalizedTarget.contains("英") || "english".equals(normalizedTarget) ? "英文" : "中文";
        return cleanSource.isBlank() ? TranslationRequest.empty()
                : new TranslationRequest(true, cleanSource, language);
    }

    record TranslationRequest(boolean found, String sourceText, String targetLanguage) {
        static TranslationRequest empty() {
            return new TranslationRequest(false, "", "");
        }
    }
}
