package com.example.maimaibackend.ai.tool;

import com.example.maimaibackend.ai.domain.entity.AiResolvedVenue;
import com.example.maimaibackend.service.PerformanceService;
import com.example.maimaibackend.vo.performance.VenueVO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class AiVenueResolver {
    private static final long CACHE_MILLIS = 300_000L;
    private static final Map<String, String> ALIASES = aliases();
    private static final Set<String> CANONICAL_CITIES = Set.of(
            "北京", "上海", "天津", "重庆", "石家庄", "太原", "呼和浩特", "沈阳", "大连", "长春", "哈尔滨",
            "南京", "无锡", "苏州", "杭州", "宁波", "合肥", "福州", "厦门", "南昌", "济南", "青岛",
            "郑州", "武汉", "长沙", "广州", "深圳", "珠海", "佛山", "东莞", "南宁", "海口", "三亚",
            "成都", "贵阳", "昆明", "拉萨", "西安", "兰州", "西宁", "银川", "乌鲁木齐");
    private final PerformanceService performanceService;
    private volatile List<VenueVO> cachedVenues = List.of();
    private volatile long cacheExpiresAt;

    public AiVenueResolver(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    public AiResolvedVenue resolve(String userText, String candidateVenue, String candidateKeyword) {
        String text = value(userText);
        List<VenueVO> venues = venues();
        AiResolvedVenue direct = venues.stream()
                .filter(this::valid)
                .filter(venue -> normalize(text).contains(normalize(venue.getVenueName())))
                .max(Comparator.comparingInt(venue -> normalize(venue.getVenueName()).length()))
                .map(venue -> resolved(venue, venue.getVenueName(), "USER_TEXT"))
                .orElse(AiResolvedVenue.empty());
        if (direct.found()) return logged(direct);

        for (String candidate : List.of(value(candidateVenue), value(candidateKeyword))) {
            if (candidate.isBlank()) continue;
            VenueVO matched = exactVenue(venues, candidate);
            if (matched != null) {
                String matchedText = text.contains(candidate) ? candidate : matchedAlias(text, matched.getVenueName());
                return logged(resolved(matched, matchedText, "LLM_CANDIDATE"));
            }
        }

        String normalizedText = normalize(text);
        for (Map.Entry<String, String> alias : ALIASES.entrySet()) {
            if (!normalizedText.contains(normalize(alias.getKey()))) continue;
            VenueVO matched = exactVenue(venues, alias.getValue());
            if (matched != null) return logged(resolved(matched, alias.getKey(), "ALIAS"));
        }
        return AiResolvedVenue.empty();
    }

    public String resolveCity(String userText, String candidateCity) {
        String text = value(userText);
        List<VenueVO> venues = venues();
        List<String> cities = cityDirectory(venues);
        String direct = cities.stream()
                .filter(city -> explicitlyMentionsCity(text, city))
                .max(Comparator.comparingInt(city -> normalize(city).length()))
                .orElse("");
        if (!direct.isBlank()) {
            return direct;
        }
        String candidate = cleanCity(candidateCity);
        String verified = cities.stream()
                .filter(city -> normalize(city).equals(normalize(candidate)))
                .findFirst().orElse("");
        if (!verified.isBlank() && explicitlyMentionsCity(text, verified)) {
            return verified;
        }
        return verified;
    }

    public AiResolvedVenue resolveCanonical(String canonicalName) {
        VenueVO matched = exactVenue(venues(), canonicalName);
        return matched == null ? AiResolvedVenue.empty() : logged(resolved(matched, value(canonicalName), "CONTEXT"));
    }

    public List<VenueFact> resolveKnowledgeEntities(String userText) {
        String text = value(userText);
        String normalizedText = normalize(text);
        List<VenueFact> facts = new ArrayList<>();
        List<VenueVO> venues = venues();
        for (VenueVO venue : venues) {
            if (!valid(venue) || !normalizedText.contains(normalize(venue.getVenueName()))) continue;
            facts.add(fact(venue, venue.getVenueName()));
        }
        for (Map.Entry<String, String> alias : ALIASES.entrySet()) {
            if (!normalizedText.contains(normalize(alias.getKey()))) continue;
            VenueVO venue = exactVenue(venues, alias.getValue());
            if (venue != null) facts.add(fact(venue, alias.getKey()));
        }
        return List.copyOf(facts);
    }

    private List<VenueVO> venues() {
        long now = System.currentTimeMillis();
        List<VenueVO> current = cachedVenues;
        if (now < cacheExpiresAt && !current.isEmpty()) return current;
        synchronized (this) {
            if (now < cacheExpiresAt && !cachedVenues.isEmpty()) return cachedVenues;
            List<VenueVO> loaded = performanceService.getVenueLookupList();
            cachedVenues = loaded == null ? List.of() : List.copyOf(loaded);
            cacheExpiresAt = now + CACHE_MILLIS;
            return cachedVenues;
        }
    }

    private VenueVO exactVenue(List<VenueVO> venues, String candidate) {
        String normalized = normalize(candidate);
        if (normalized.isBlank()) return null;
        for (VenueVO venue : venues) {
            if (valid(venue) && normalize(venue.getVenueName()).equals(normalized)) return venue;
        }
        return null;
    }

    private AiResolvedVenue resolved(VenueVO venue, String matchedText, String source) {
        return new AiResolvedVenue(venue.getVenueId(), value(venue.getVenueName()), value(venue.getCityName()), value(matchedText), source);
    }

    private VenueFact fact(VenueVO venue, String matchedText) {
        return new VenueFact(venue.getVenueId(), value(venue.getVenueName()), value(venue.getCityName()),
                value(venue.getAddress()), value(matchedText));
    }

    private String matchedAlias(String text, String canonicalName) {
        String normalizedText = normalize(text);
        for (Map.Entry<String, String> alias : ALIASES.entrySet()) {
            if (normalize(alias.getValue()).equals(normalize(canonicalName))
                    && normalizedText.contains(normalize(alias.getKey()))) return alias.getKey();
        }
        return "";
    }

    private AiResolvedVenue logged(AiResolvedVenue venue) {
        return venue;
    }

    private boolean valid(VenueVO venue) {
        return venue != null && venue.getVenueName() != null && !venue.getVenueName().isBlank();
    }

    private String normalize(String value) {
        return value(value).toLowerCase(Locale.ROOT).replaceAll("[\\s·・,，.。()（）\u2013\u2014_-]+", "");
    }

    private List<String> cityDirectory(List<VenueVO> venues) {
        LinkedHashSet<String> cities = new LinkedHashSet<>(CANONICAL_CITIES);
        for (VenueVO venue : venues) {
            if (!valid(venue)) continue;
            String city = cleanCity(venue.getCityName());
            if (!city.isBlank()) cities.add(city);
        }
        return new ArrayList<>(cities);
    }

    private boolean explicitlyMentionsCity(String text, String city) {
        String normalizedText = normalize(text).replace("市", "");
        String normalizedCity = normalize(city).replace("市", "");
        return !normalizedCity.isBlank() && normalizedText.contains(normalizedCity);
    }

    private String cleanCity(String value) {
        String cleaned = value(value);
        return cleaned.endsWith("市") && cleaned.length() > 2 ? cleaned.substring(0, cleaned.length() - 1) : cleaned;
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }

    private static Map<String, String> aliases() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("鸟巢", "国家体育场");
        values.put("五棵松", "凯迪拉克中心");
        values.put("梅奔", "梅赛德斯-奔驰文化中心");
        values.put("梅赛德斯中心", "梅赛德斯-奔驰文化中心");
        values.put("上文广", "上海文化广场");
        return Map.copyOf(values);
    }

    public record VenueFact(Long entityId, String canonicalName, String city, String address, String matchedText) {
    }
}
