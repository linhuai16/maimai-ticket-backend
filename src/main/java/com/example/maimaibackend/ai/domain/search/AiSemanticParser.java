package com.example.maimaibackend.ai.domain.search;

import com.example.maimaibackend.ai.domain.action.AiConversationAction;
import com.example.maimaibackend.ai.domain.context.AiActiveSlot;
import com.example.maimaibackend.ai.domain.context.AiSearchContext;
import com.example.maimaibackend.ai.domain.context.AiSlotDelta;
import com.example.maimaibackend.ai.domain.entity.AiResolvedVenue;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiSemanticParser {
    private static final Pattern DYNAMIC_TIME = Pattern.compile(
            "(?:最近|近期|近|未来|接下来)\\s*([零〇一二两三四五六七八九十百\\d]+)\\s*(天|日|周|星期|个?月)");
    private static final Pattern ACTIVE_TIME = Pattern.compile(
            "^\\s*([零〇一二两三四五六七八九十百\\d]+)\\s*(天|日|周|星期|个?月)(?:内|以内)?[呢吗呀啊吧？?]*\\s*$");
    private static final Pattern PRICE_RANGE = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*元?\\s*(?:~|～|—|-|到|至)\\s*(\\d+(?:\\.\\d+)?)\\s*元?");
    private static final Pattern PRICE_MAX_SUFFIX = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*元?\\s*(?:以内|以下|之内)");
    private static final Pattern PRICE_MAX_PREFIX = Pattern.compile(
            "(?:不超过|最多(?:是|为)?|预算(?:是|为|在)?|控制在)\\s*(\\d+(?:\\.\\d+)?)\\s*元?");
    private static final Pattern PRICE_MIN_SUFFIX = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*元?\\s*(?:以上|起)");
    private static final Pattern PRICE_MIN_PREFIX = Pattern.compile(
            "(?:不低于|至少)\\s*(\\d+(?:\\.\\d+)?)\\s*元?");
    private static final Pattern ACTIVE_PRICE = Pattern.compile("^\\s*(\\d+(?:\\.\\d+)?)\\s*元?[呢吗呀啊吧？?]*\\s*$");
    private static final Pattern ACTIVE_NATURAL_PRICE = Pattern.compile(
            "^\\s*([零〇一二两三四五六七八九十百]+)\\s*元?[呢吗呀啊吧？?]*\\s*$");
    private static final Pattern ISO_DATE = Pattern.compile("(20\\d{2})[-/.年](\\d{1,2})[-/.月](\\d{1,2})日?");
    private static final Pattern MONTH_DAY = Pattern.compile("(?<!\\d)(\\d{1,2})月(\\d{1,2})日?");
    private static final Pattern CONTINUE = Pattern.compile("(?:继续(?:看|找)?|更多|下一批|换一批|再来一批|还有(?:别的|其他)|(?:其他|其它)演出)");
    private static final Pattern ORDINAL = Pattern.compile("(?:第([一二两三四五六七八九十\\d]+)个|最后一个|排头|最前面|排尾|最末|这两个|这一个|这个|它|刚才那个)");
    private static final Pattern ALL_RESULTS = Pattern.compile("(?:全部|所有).{0,4}(?:演出|活动)");
    private static final Pattern BROADEN_TIME = Pattern.compile("(?:(?:放宽|扩大).{0,4}时间|时间.{0,4}(?:放宽|扩大))");
    private static final List<String> CATEGORIES = List.of(
            "演唱会", "音乐会", "音乐节", "话剧", "歌剧", "舞蹈", "芭蕾", "戏曲", "相声", "脱口秀", "儿童剧", "展览", "体育");

    public AiSemanticParseResult parse(String userText, JsonNode candidate, AiSearchContext previous,
                                       AiActiveSlot activeSlot, AiConversationAction action,
                                       AiResolvedVenue venue, String resolvedCity, LocalDateTime now) {
        String text = value(userText);
        AiResolvedVenue verifiedVenue = venue == null ? AiResolvedVenue.empty() : venue;
        String entityFreeText = removeVenue(text, verifiedVenue);
        Map<String, String> recognized = new LinkedHashMap<>();
        List<AiSlotDelta> deltas = new ArrayList<>();
        List<SlotHit> hits = new ArrayList<>();
        boolean mediumEvidence = false;

        if (ALL_RESULTS.matcher(entityFreeText).find()) {
            clear(deltas, "category", "keyword", "venue", "minPrice", "maxPrice", "startTime", "endTime", "timeIntent");
            replace(deltas, "sort", "NEAREST");
            recognized.put("FILTERS", "ALL_FUTURE");
        }

        if (verifiedVenue.found()) {
            replace(deltas, "venue", verifiedVenue.canonicalName());
            recognized.put(AiActiveSlot.VENUE.name(), verifiedVenue.canonicalName());
            hit(hits, AiActiveSlot.VENUE, text, verifiedVenue.matchedText(), verifiedVenue.canonicalName());
            if (!value(verifiedVenue.city()).isBlank()) {
                replace(deltas, "city", verifiedVenue.city());
                recognized.put(AiActiveSlot.CITY.name(), verifiedVenue.city());
            }
            mediumEvidence = "LLM_CANDIDATE".equals(verifiedVenue.source());
        } else if (matchesAny(entityFreeText, "不限场馆", "场馆不限", "不限场地")) {
            clear(deltas, "venue");
            recognized.put(AiActiveSlot.VENUE.name(), "CLEAR");
            hit(hits, AiActiveSlot.VENUE, entityFreeText, "场馆", "");
        }

        if (!value(resolvedCity).isBlank() && !verifiedVenue.found()) {
            replace(deltas, "city", resolvedCity);
            recognized.put(AiActiveSlot.CITY.name(), resolvedCity);
            hit(hits, AiActiveSlot.CITY, entityFreeText, resolvedCity, resolvedCity);
        } else if (matchesAny(entityFreeText, "不限城市", "城市不限", "全国")) {
            clear(deltas, "city");
            recognized.put(AiActiveSlot.CITY.name(), "CLEAR");
            hit(hits, AiActiveSlot.CITY, entityFreeText, "城市", "");
        }

        String category = category(entityFreeText, text(candidate, "category"));
        if (!category.isBlank()) {
            replace(deltas, "category", category);
            recognized.put(AiActiveSlot.CATEGORY.name(), category);
            hit(hits, AiActiveSlot.CATEGORY, entityFreeText, category, category);
        } else if (matchesAny(entityFreeText, "不限分类", "不限类型", "分类不限", "类型不限")) {
            clear(deltas, "category");
            recognized.put(AiActiveSlot.CATEGORY.name(), "CLEAR");
            hit(hits, AiActiveSlot.CATEGORY, entityFreeText, "分类", "");
        }

        PriceValue price = price(entityFreeText, candidate, activeSlot);
        if (price != null) {
            if (price.clear()) clear(deltas, "minPrice", "maxPrice");
            else {
                if (price.min() == null) clear(deltas, "minPrice");
                else replace(deltas, "minPrice", price.min().toPlainString());
                if (price.max() == null) clear(deltas, "maxPrice");
                else replace(deltas, "maxPrice", price.max().toPlainString());
            }
            recognized.put(AiActiveSlot.PRICE.name(), price.semantic());
            hits.add(new SlotHit(AiActiveSlot.PRICE, price.end()));
        }

        TimeValue time = time(entityFreeText, previous, activeSlot, action, now);
        if (time != null) {
            if (time.clear()) clear(deltas, "startTime", "endTime", "timeIntent");
            else {
                replace(deltas, "startTime", time.start());
                if (time.end().isBlank()) clear(deltas, "endTime");
                else replace(deltas, "endTime", time.end());
                replace(deltas, "timeIntent", time.intent());
            }
            recognized.put(AiActiveSlot.TIME.name(), time.intent());
            hits.add(new SlotHit(AiActiveSlot.TIME, time.endIndex()));
        }

        String sort = sort(entityFreeText, candidate);
        if (!sort.isBlank()) {
            replace(deltas, "sort", sort);
            recognized.put(AiActiveSlot.SORT.name(), sort);
            hit(hits, AiActiveSlot.SORT, entityFreeText, sortToken(entityFreeText), sort);
        } else if (matchesAny(entityFreeText, "不限排序", "默认排序")) {
            clear(deltas, "sort");
            recognized.put(AiActiveSlot.SORT.name(), "CLEAR");
            hit(hits, AiActiveSlot.SORT, entityFreeText, "排序", "");
        }

        Matcher continuation = CONTINUE.matcher(entityFreeText);
        Matcher ordinal = ORDINAL.matcher(entityFreeText);
        if (continuation.find()) {
            recognized.put(AiActiveSlot.RESULT_REFERENCE.name(), "CONTINUE");
            hits.add(new SlotHit(AiActiveSlot.RESULT_REFERENCE, continuation.end()));
        } else if (ordinal.find()) {
            recognized.put(AiActiveSlot.RESULT_REFERENCE.name(), ordinal.group());
            hits.add(new SlotHit(AiActiveSlot.RESULT_REFERENCE, ordinal.end()));
        }

        AiActiveSlot candidateSlot = hits.stream().max((left, right) -> Integer.compare(left.end(), right.end()))
                .map(SlotHit::slot).orElse(AiActiveSlot.NONE);
        AiSemanticParseResult.Confidence confidence = recognized.isEmpty()
                ? AiSemanticParseResult.Confidence.LOW
                : mediumEvidence ? AiSemanticParseResult.Confidence.MEDIUM : AiSemanticParseResult.Confidence.HIGH;
        return new AiSemanticParseResult(Map.copyOf(recognized), candidateSlot, confidence, List.copyOf(deltas));
    }

    private TimeValue time(String text, AiSearchContext previous, AiActiveSlot activeSlot,
                           AiConversationAction action, LocalDateTime now) {
        if (matchesAny(text, "不限时间", "时间不限", "清除时间")) return TimeValue.clear(text.length());
        Matcher broaden = BROADEN_TIME.matcher(text);
        if (action == AiConversationAction.BROADEN_SEARCH && broaden.find()) {
            return broaden(previous, now, broaden.end());
        }
        Matcher dynamic = DYNAMIC_TIME.matcher(text);
        if (dynamic.find()) {
            int amount = naturalNumber(dynamic.group(1));
            if (amount > 0) return duration(now, amount, dynamic.group(2), dynamic.end());
        }
        if (activeSlot == AiActiveSlot.TIME) {
            Matcher active = ACTIVE_TIME.matcher(text);
            if (active.find()) {
                int amount = naturalNumber(active.group(1));
                if (amount > 0) return duration(now, amount, active.group(2), active.end());
            }
        }
        if (matchesAny(text, "过去", "之前", "已结束", "以前")) return new TimeValue("", iso(now), "PAST", false, text.length());
        LocalDate explicit = explicitDate(text, now.toLocalDate());
        if (explicit != null) return new TimeValue(iso(explicit.atStartOfDay()), iso(explicit.atTime(LocalTime.MAX)), "EXPLICIT_DATE", false, text.length());
        if (matchesAny(text, "后天", "后日")) {
            LocalDate day = now.toLocalDate().plusDays(2);
            return new TimeValue(iso(day.atStartOfDay()), iso(day.atTime(LocalTime.MAX)), "DAY_AFTER_TOMORROW", false, text.length());
        }
        if (matchesAny(text, "明天", "明日")) {
            LocalDate day = now.toLocalDate().plusDays(1);
            return new TimeValue(iso(day.atStartOfDay()), iso(day.atTime(LocalTime.MAX)), "TOMORROW", false, text.length());
        }
        if (matchesAny(text, "今天", "今日")) return new TimeValue(iso(now), iso(now.toLocalDate().atTime(LocalTime.MAX)), "TODAY", false, text.length());
        if (text.contains("周末")) {
            LocalDate today = now.toLocalDate();
            LocalDate saturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
            LocalDate sunday = saturday.plusDays(1);
            LocalDateTime start = today.getDayOfWeek().getValue() >= DayOfWeek.SATURDAY.getValue() ? now : saturday.atStartOfDay();
            return new TimeValue(iso(start), iso(sunday.atTime(LocalTime.MAX)), "WEEKEND", false, text.length());
        }
        if (matchesAny(text, "本周", "这周")) {
            LocalDate sunday = now.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            return new TimeValue(iso(now), iso(sunday.atTime(LocalTime.MAX)), "THIS_WEEK", false, text.length());
        }
        if (matchesAny(text, "本月", "这个月")) {
            LocalDate last = now.toLocalDate().with(TemporalAdjusters.lastDayOfMonth());
            return new TimeValue(iso(now), iso(last.atTime(LocalTime.MAX)), "THIS_MONTH", false, text.length());
        }
        if (matchesAny(text, "最近", "近期")) return new TimeValue(iso(now), iso(now.plusDays(30)), "NEXT_30_DAYS", false, text.length());
        if (text.contains("未来")) return new TimeValue(iso(now), "", "FUTURE", false, text.length());
        return null;
    }

    private TimeValue duration(LocalDateTime now, int amount, String unit, int end) {
        LocalDateTime until;
        String intent;
        if ("天".equals(unit) || "日".equals(unit)) {
            until = now.plusDays(amount);
            intent = amount == 7 ? "NEXT_7_DAYS" : amount == 30 ? "NEXT_30_DAYS" : "NEXT_" + amount + "_DAYS";
        } else if ("周".equals(unit) || "星期".equals(unit)) {
            until = now.plusWeeks(amount);
            intent = amount == 1 ? "NEXT_7_DAYS" : "NEXT_" + amount + "_WEEKS";
        } else {
            until = now.plusMonths(amount);
            intent = amount == 1 ? "NEXT_30_DAYS" : "NEXT_" + amount + "_MONTHS";
        }
        return new TimeValue(iso(now), iso(until), intent, false, end);
    }

    private TimeValue broaden(AiSearchContext previous, LocalDateTime now, int end) {
        String intent = previous == null ? "" : value(previous.timeIntent()).toUpperCase(Locale.ROOT);
        return switch (intent) {
            case "TODAY", "TOMORROW", "DAY_AFTER_TOMORROW", "WEEKEND", "THIS_WEEK", "EXPLICIT_DATE" ->
                    new TimeValue(iso(now), iso(now.plusDays(7)), "NEXT_7_DAYS", false, end);
            case "NEXT_7_DAYS" -> new TimeValue(iso(now), iso(now.plusDays(30)), "NEXT_30_DAYS", false, end);
            case "NEXT_30_DAYS", "RECENT", "THIS_MONTH" -> new TimeValue(iso(now), "", "FUTURE", false, end);
            case "FUTURE" -> new TimeValue(previous.startTime(), previous.endTime(), "FUTURE", false, end);
            default -> new TimeValue(iso(now), iso(now.plusDays(7)), "NEXT_7_DAYS", false, end);
        };
    }

    private PriceValue price(String text, JsonNode candidate, AiActiveSlot activeSlot) {
        if (matchesAny(text, "不限价格", "价格不限", "不看价格", "不限制价格")) return PriceValue.clear(text.length());
        Matcher range = PRICE_RANGE.matcher(text);
        if (range.find()) {
            BigDecimal first = decimal(range.group(1));
            BigDecimal second = decimal(range.group(2));
            if (first != null && second != null) {
                BigDecimal min = first.min(second);
                BigDecimal max = first.max(second);
                return new PriceValue(min, max, false, "RANGE:" + min + "-" + max, range.end());
            }
        }
        Matcher max = PRICE_MAX_SUFFIX.matcher(text);
        if (!max.find()) max = PRICE_MAX_PREFIX.matcher(text);
        if (max.find(0)) {
            BigDecimal value = decimal(max.group(1));
            if (value != null) return new PriceValue(null, value, false, "MAX:" + value, max.end());
        }
        Matcher min = PRICE_MIN_SUFFIX.matcher(text);
        if (!min.find()) min = PRICE_MIN_PREFIX.matcher(text);
        if (min.find(0)) {
            BigDecimal value = decimal(min.group(1));
            if (value != null) return new PriceValue(value, null, false, "MIN:" + value, min.end());
        }
        if (activeSlot == AiActiveSlot.PRICE) {
            Matcher active = ACTIVE_PRICE.matcher(text);
            if (active.find()) {
                BigDecimal value = decimal(active.group(1));
                if (value != null) return new PriceValue(null, value, false, "MAX:" + value, active.end());
            }
            Matcher natural = ACTIVE_NATURAL_PRICE.matcher(text);
            if (natural.find()) {
                int amount = naturalNumber(natural.group(1));
                if (amount > 0) {
                    BigDecimal value = BigDecimal.valueOf(amount);
                    return new PriceValue(null, value, false, "MAX:" + value, natural.end());
                }
            }
        }
        if (activeSlot == AiActiveSlot.PRICE || matchesAny(text, "价格", "预算", "元")) {
            BigDecimal candidateMin = decimal(candidate, "minPrice");
            BigDecimal candidateMax = decimal(candidate, "maxPrice");
            if (candidateMin != null || candidateMax != null) {
                return new PriceValue(candidateMin, candidateMax, false,
                        candidateMin != null && candidateMax != null ? "RANGE:" + candidateMin + "-" + candidateMax
                                : candidateMax != null ? "MAX:" + candidateMax : "MIN:" + candidateMin, text.length());
            }
        }
        return null;
    }

    private String category(String text, String candidate) {
        for (String category : CATEGORIES) if (categoryMention(text, category)) return category;
        String value = value(candidate);
        return CATEGORIES.contains(value) && categoryMention(text, value) ? value : "";
    }

    private boolean categoryMention(String text, String category) {
        if (!text.contains(category)) return false;
        if (!"体育".equals(category)) return true;
        return text.replaceAll("体育(?:场|馆|中心)", "").contains("体育");
    }

    private String sort(String text, JsonNode candidate) {
        if (isSuperlativeSelection(text)) return "";
        if (Pattern.compile("(?:价格)?.{0,3}(?:从低到高|升序)|(?:便宜|低价).{0,4}(?:前面|优先)|最便宜|价格最低").matcher(text).find()) return "PRICE_ASC";
        if (Pattern.compile("(?:价格)?.{0,3}(?:从高到低|降序)|(?:贵|高价).{0,4}(?:前面|优先)|最贵|价格最高").matcher(text).find()) return "PRICE_DESC";
        if (Pattern.compile("(?:时间|开演)?.{0,3}(?:最近|由近到远)|最近的").matcher(text).find()) return "NEAREST";
        if (matchesAny(text, "热门", "最火", "按热度")
                || Pattern.compile("(?:热门|热度高).{0,4}(?:前面|优先)").matcher(text).find()) return "HOT";
        if (matchesAny(text, "最新", "新上架", "新发布")) return "NEW";
        String value = text(candidate, "sort").toUpperCase(Locale.ROOT);
        return isSort(value) && matchesAny(text, "排序", "价格", "热门", "最新", "最近") ? value : "";
    }

    private boolean isSuperlativeSelection(String text) {
        boolean superlative = matchesAny(text, "最便宜", "价格最低", "票价最低", "最低价", "最贵", "价格最高", "票价最高", "最高价", "最早", "最晚");
        boolean selection = matchesAny(text, "哪个", "哪一个", "哪场", "是谁", "是哪个", "是哪一个", "这几个");
        return superlative && selection;
    }

    private String sortToken(String text) {
        for (String token : List.of("从低到高", "从高到低", "最便宜", "最贵", "热门", "最新", "最近")) {
            if (text.contains(token)) return token;
        }
        return "排序";
    }

    private LocalDate explicitDate(String text, LocalDate today) {
        Matcher iso = ISO_DATE.matcher(text);
        if (iso.find()) return date(integer(iso.group(1)), integer(iso.group(2)), integer(iso.group(3)));
        Matcher monthDay = MONTH_DAY.matcher(text);
        if (!monthDay.find()) return null;
        int month = integer(monthDay.group(1));
        int day = integer(monthDay.group(2));
        LocalDate value = date(today.getYear(), month, day);
        if (value != null && value.isBefore(today)) value = date(today.getYear() + 1, month, day);
        return value;
    }

    private int naturalNumber(String token) {
        String value = value(token);
        if (value.matches("\\d+")) return integer(value);
        if ("零".equals(value) || "〇".equals(value)) return 0;
        Map<Character, Integer> digits = Map.of('一', 1, '二', 2, '两', 2, '三', 3, '四', 4,
                '五', 5, '六', 6, '七', 7, '八', 8, '九', 9);
        if (value.equals("十")) return 10;
        int hundred = value.indexOf('百');
        int total = 0;
        if (hundred >= 0) {
            total += hundred == 0 ? 100 : digits.getOrDefault(value.charAt(hundred - 1), 0) * 100;
            value = value.substring(hundred + 1);
        }
        int ten = value.indexOf('十');
        if (ten >= 0) {
            total += ten == 0 ? 10 : digits.getOrDefault(value.charAt(ten - 1), 0) * 10;
            value = value.substring(ten + 1);
        }
        if (!value.isEmpty()) total += digits.getOrDefault(value.charAt(value.length() - 1), 0);
        return total;
    }

    private void replace(List<AiSlotDelta> deltas, String slot, String value) {
        deltas.removeIf(item -> slot.equals(item.slot()));
        deltas.add(AiSlotDelta.replace(slot, value));
    }

    private void clear(List<AiSlotDelta> deltas, String... slots) {
        for (String slot : slots) {
            deltas.removeIf(item -> slot.equals(item.slot()));
            deltas.add(AiSlotDelta.clear(slot));
        }
    }

    private void hit(List<SlotHit> hits, AiActiveSlot slot, String text, String expression, String fallback) {
        int index = expression == null || expression.isBlank() ? -1 : text.lastIndexOf(expression);
        if (index < 0 && fallback != null && !fallback.isBlank()) index = text.lastIndexOf(fallback);
        hits.add(new SlotHit(slot, index < 0 ? text.length() : index + Math.max(1, value(expression).length())));
    }

    private String removeVenue(String text, AiResolvedVenue venue) {
        if (venue == null || !venue.found()) return text;
        String result = text;
        if (!value(venue.matchedText()).isBlank()) result = result.replace(venue.matchedText(), "");
        if (!value(venue.canonicalName()).isBlank()) result = result.replace(venue.canonicalName(), "");
        return result;
    }

    private String text(JsonNode node, String field) {
        if (node == null) return "";
        JsonNode value = node.path(field);
        return value.isTextual() ? value(value.asText()) : "";
    }

    private BigDecimal decimal(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode value = node.path(field);
        return value.isNumber() || value.isTextual() ? decimal(value.asText()) : null;
    }

    private BigDecimal decimal(String value) {
        try {
            return value == null || value.isBlank() ? null : new BigDecimal(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private LocalDate date(int year, int month, int day) {
        try {
            return LocalDate.of(year, month, day);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private boolean matchesAny(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }

    private boolean isSort(String value) {
        return List.of("NEAREST", "PRICE_ASC", "PRICE_DESC", "HOT", "NEW").contains(value);
    }

    private int integer(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }

    private String iso(LocalDateTime value) {
        return value == null ? "" : value.withSecond(0).withNano(0).toString();
    }

    private record SlotHit(AiActiveSlot slot, int end) {
    }

    private record PriceValue(BigDecimal min, BigDecimal max, boolean clear, String semantic, int end) {
        private static PriceValue clear(int end) {
            return new PriceValue(null, null, true, "CLEAR", end);
        }
    }

    private record TimeValue(String start, String end, String intent, boolean clear, int endIndex) {
        private static TimeValue clear(int end) {
            return new TimeValue("", "", "CLEAR", true, end);
        }
    }
}
