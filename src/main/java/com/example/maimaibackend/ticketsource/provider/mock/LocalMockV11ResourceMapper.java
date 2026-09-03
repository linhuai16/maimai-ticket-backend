package com.example.maimaibackend.ticketsource.provider.mock;

import com.example.maimaibackend.ticketsource.provider.adapter.V11AdapterException;
import com.example.maimaibackend.ticketsource.provider.adapter.V11ErrorCode;
import com.example.maimaibackend.ticketsource.provider.enums.*;
import com.example.maimaibackend.ticketsource.provider.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Component
class LocalMockV11ResourceMapper {
    private static final ZoneOffset CN_OFFSET = ZoneOffset.ofHours(8);
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    LocalMockV11ResourceMapper(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    ProviderProjectSummary toProjectSummary(Map<String, Object> row) {
        ProjectStatus status = parseProjectStatus(string(row, "sale_status"));
        ProviderVenue venue = null;
        String venueId = string(row, "venue_id");
        if (venueId != null) {
            Map<String, Object> venueRow = one("SELECT * FROM mock_ticket_source_venue WHERE venue_id=? AND enabled=1", venueId);
            if (venueRow != null) venue = toVenue(venueRow);
        }
        return new ProviderProjectSummary(
                string(row, "source_project_id"), string(row, "source_project_name"),
                new ProviderStatusValue<>(status, string(row, "source_status_code"), string(row, "source_status_text")),
                string(row, "project_type"), string(row, "category_code"), string(row, "category_name"),
                string(row, "city_code"), string(row, "city_name"), venue, string(row, "poster_url"),
                string(row, "show_time_text"), offsetNullable(row.get("sale_start_time")),
                offsetNullable(row.get("sale_end_time")),
                ProviderMoney.fromMajor(decimalNullable(row.get("min_price")), "CNY"),
                ProviderMoney.fromMajor(decimalNullable(row.get("max_price")), "CNY"),
                bool(row.get("has_reserved_seat")), true,
                defaultVersion(string(row, "data_version"), "project"), offset(row.get("update_time"))
        );
    }

    ProviderSession toSession(Map<String, Object> row) {
        ProjectStatus status = parseProjectStatus(string(row, "sale_status"));
        return new ProviderSession(
                string(row, "source_session_id"), string(row, "source_project_id"),
                string(row, "source_session_name"),
                new ProviderStatusValue<>(status, string(row, "source_status_code"), string(row, "source_status_text")),
                offset(row.get("start_time")), offsetNullable(row.get("end_time")),
                offsetNullable(row.get("sale_start_time")), offsetNullable(row.get("sale_end_time")),
                enumValue(SessionType.class, string(row, "session_type"), SessionType.SINGLE),
                enumValue(SeatMode.class, string(row, "seat_mode"), SeatMode.GENERAL_ADMISSION),
                bool(row.get("time_changed")), string(row, "change_reason"), string(row, "remark"), List.of(),
                nullableInt(row.get("limit_per_order")), string(row, "real_name_mode"),
                string(row, "issue_method"), string(row, "pickup_method"),
                defaultVersion(string(row, "data_version"), "session"), offset(row.get("update_time"))
        );
    }

    ProviderTicketProduct toTicketProduct(Map<String, Object> row) {
        TicketProductSaleStatus status = parseTicketSaleStatus(string(row, "sale_status"));
        return new ProviderTicketProduct(
                string(row, "source_sku_id"), string(row, "source_project_id"), string(row, "source_session_id"),
                string(row, "source_sku_name"),
                enumValue(TicketProductType.class, string(row, "product_type"), TicketProductType.SINGLE),
                ProviderMoney.fromMajor(decimalNullable(row.get("face_price")), "CNY"),
                ProviderMoney.fromMajor(decimalNullable(row.get("sale_price")), "CNY"),
                ProviderMoney.fromMajor(decimalNullable(row.get("settlement_price")), "CNY"),
                new ProviderStatusValue<>(status, string(row, "source_status_code"), string(row, "source_status_text")),
                string(row, "sub_status"),
                enumValue(InventoryMode.class, string(row, "inventory_mode"), InventoryMode.STATUS_ONLY),
                nullableInt(row.get("available_stock")), nullableInt(row.get("max_quantity_per_order")),
                defaultVersion(string(row, "data_version"), "sku"), offset(row.get("update_time"))
        );
    }

    ProviderVenue toVenue(Map<String, Object> row) {
        return new ProviderVenue(
                string(row, "venue_id"), string(row, "venue_name"), string(row, "country_code"),
                string(row, "province_code"), string(row, "city_code"), string(row, "district_code"),
                string(row, "address"), string(row, "longitude"), string(row, "latitude"),
                enumValue(CoordinateSystem.class, string(row, "coordinate_system"), CoordinateSystem.UNKNOWN),
                string(row, "navigation_name"), List.of(), null, null,
                defaultVersion(string(row, "data_version"), "venue"), offset(row.get("update_time"))
        );
    }

    ProviderNotice toNotice(Map<String, Object> row, String projectId) {
        String code = normalizeNoticeCode(string(row, "notice_code"));
        return new ProviderNotice(code, firstNonBlank(string(row, "title"), defaultNoticeTitle(code)),
                string(row, "content"), "PROJECT", projectId, intValue(row.get("priority")));
    }

    ProviderRefundPolicy loadRefundPolicy(String projectId) {
        Map<String, Object> policy = one(
                "SELECT * FROM mock_ticket_source_refund_policy WHERE source_project_id=?", projectId);
        if (policy == null) return null;
        List<ProviderRefundTier> tiers = jdbc.queryForList("""
                SELECT start_offset_minutes,end_offset_minutes,tier_result,fee_percent,fee_fixed
                FROM mock_ticket_source_refund_tier WHERE source_project_id=? ORDER BY sort_no,tier_id
                """, projectId).stream().map(row -> new ProviderRefundTier(
                nullableLong(row.get("start_offset_minutes")), nullableLong(row.get("end_offset_minutes")),
                string(row, "tier_result"), string(row, "fee_percent"),
                ProviderMoney.fromMajor(decimalNullable(row.get("fee_fixed")), "CNY")
        )).toList();
        return new ProviderRefundPolicy(
                string(policy, "refund_type"), bool(policy.get("consumer_entry_enabled")),
                string(policy, "fee_rule_mode"), bool(policy.get("delivery_fee_refundable")),
                string(policy, "paper_ticket_return_rule"), tiers, string(policy, "source_rule_text"));
    }

    ProviderRefundPolicy completeRefundPolicy(List<ProviderNotice> notices, ProviderRefundPolicy current) {
        String policyText = notices.stream().filter(notice -> "REFUND_NOTICE".equals(notice.noticeCode()))
                .map(ProviderNotice::content).filter(Objects::nonNull).filter(value -> !value.isBlank())
                .findFirst().orElse(null);
        if (current == null) {
            if (policyText == null) return null;
            boolean noRefund = isNoRefundText(policyText);
            return new ProviderRefundPolicy(noRefund ? "NO_REFUND" : "CONDITIONAL_REFUND", !noRefund,
                    null, false, null, List.of(), policyText);
        }
        String sourceRuleText = firstNonBlank(current.sourceRuleText(), policyText);
        if (Objects.equals(sourceRuleText, current.sourceRuleText())) return current;
        return new ProviderRefundPolicy(current.refundType(), current.consumerEntryEnabled(), current.feeRuleMode(),
                current.deliveryFeeRefundable(), current.paperTicketReturnRule(), current.tiers(), sourceRuleText);
    }

    ProviderPromotionRule toPromotionRule(Map<String, Object> row) {
        return new ProviderPromotionRule(
                string(row, "promotion_id"),
                enumValue(PromotionType.class, string(row, "promotion_type"), PromotionType.OTHER),
                string(row, "title"), string(row, "description"),
                singletonNullable(string(row, "source_project_id")),
                singletonNullable(string(row, "source_session_id")),
                singletonNullable(string(row, "source_sku_id")), bool(row.get("stackable")),
                readMap(string(row, "rule_data")), offset(row.get("valid_from")), offset(row.get("valid_to")),
                defaultVersion(string(row, "data_version"), "promotion"), offset(row.get("update_time")));
    }

    ProviderCampaignAsset toCampaignAsset(Map<String, Object> row) {
        return new ProviderCampaignAsset(
                string(row, "asset_id"),
                enumValue(CampaignAssetType.class, string(row, "asset_type"), CampaignAssetType.OTHER),
                string(row, "position_code"), string(row, "title"), string(row, "description"),
                string(row, "image_url"), string(row, "mobile_image_url"),
                enumValue(CampaignTargetType.class, string(row, "target_type"), CampaignTargetType.NONE),
                string(row, "target_value"), csv(string(row, "city_codes")),
                offsetNullable(row.get("start_time")), offsetNullable(row.get("end_time")),
                string(row, "promotion_id"), true,
                defaultVersion(string(row, "data_version"), "asset"), offset(row.get("update_time")));
    }

    private String normalizeNoticeCode(String rawCode) {
        String code = rawCode == null ? "OTHER" : rawCode.trim().toUpperCase(Locale.ROOT);
        return switch (code) {
            case "CHILDREN_NOTICE", "CHILDREN_POLICY" -> "CHILDREN_POLICY";
            case "REAL_NAME_NOTICE", "REAL_NAME_POLICY" -> "REAL_NAME_POLICY";
            case "LIMIT_NOTICE", "PURCHASE_LIMIT" -> "PURCHASE_LIMIT";
            case "ENTRANCE_NOTICE", "ENTRY_NOTICE" -> "ENTRY_NOTICE";
            case "PROHIBITED_ITEMS" -> "PROHIBITED_ITEMS";
            case "DEPOSIT_INFO", "STORAGE_NOTICE" -> "STORAGE_NOTICE";
            case "SELF_GET_TICKET_NOTICE", "PICKUP_NOTICE" -> "PICKUP_NOTICE";
            case "ETICKET_NOTICE", "E_TICKET_NOTICE" -> "E_TICKET_NOTICE";
            case "CHOICE_SEAT_NOTICE", "SEAT_SELECTION_NOTICE" -> "SEAT_SELECTION_NOTICE";
            case "POLICY_OF_RETURN", "REFUND_NOTICE" -> "REFUND_NOTICE";
            default -> code;
        };
    }

    private String defaultNoticeTitle(String code) {
        return switch (code) {
            case "CHILDREN_POLICY" -> "儿童购票说明";
            case "REAL_NAME_POLICY" -> "实名制说明";
            case "PURCHASE_LIMIT" -> "限购说明";
            case "ENTRY_NOTICE" -> "入场说明";
            case "PROHIBITED_ITEMS" -> "禁止携带物品说明";
            case "STORAGE_NOTICE" -> "寄存说明";
            case "PICKUP_NOTICE" -> "取票说明";
            case "E_TICKET_NOTICE" -> "电子票说明";
            case "SEAT_SELECTION_NOTICE" -> "选座说明";
            case "REFUND_NOTICE" -> "退票/换票规则";
            default -> code;
        };
    }

    private boolean isNoRefundText(String text) {
        return text != null && (text.contains("不支持退") || text.contains("不可退")
                || text.contains("不退不换") || text.contains("不支持退换"));
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            Object value = objectMapper.readValue(json, Map.class);
            return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        } catch (Exception e) {
            throw new V11AdapterException(V11ErrorCode.INVALID_PROVIDER_RESPONSE,
                    "MOCK_RULE_DATA_INVALID", "模拟优惠规则数据不是合法JSON", false);
        }
    }

    private ProjectStatus parseProjectStatus(String value) {
        return switch (upper(value, "UNKNOWN")) {
            case "PRESALE" -> ProjectStatus.PRESALE;
            case "ON_SALE" -> ProjectStatus.ON_SALE;
            case "SOLD_OUT" -> ProjectStatus.SOLD_OUT;
            case "OFF_SHELF", "SUSPENDED" -> ProjectStatus.SUSPENDED;
            case "CANCELLED", "CANCELED" -> ProjectStatus.CANCELLED;
            case "ENDED" -> ProjectStatus.ENDED;
            case "PENDING_SALE" -> ProjectStatus.PENDING_SALE;
            default -> ProjectStatus.UNKNOWN;
        };
    }

    private TicketProductSaleStatus parseTicketSaleStatus(String value) {
        return switch (upper(value, "UNKNOWN")) {
            case "ON_SALE", "PRESALE" -> TicketProductSaleStatus.ON_SALE;
            case "SOLD_OUT" -> TicketProductSaleStatus.SOLD_OUT;
            case "SALE_REMINDER" -> TicketProductSaleStatus.SALE_REMINDER;
            case "STOCK_REGISTRATION" -> TicketProductSaleStatus.STOCK_REGISTRATION;
            case "OFF_SHELF", "SUSPENDED" -> TicketProductSaleStatus.SUSPENDED;
            case "NOT_ON_SALE" -> TicketProductSaleStatus.NOT_ON_SALE;
            default -> TicketProductSaleStatus.UNKNOWN;
        };
    }

    private String string(Map<String, Object> values, String key) {
        if (values == null) return null;
        Object value = values.get(key);
        if (value == null) value = values.get(key.toUpperCase(Locale.ROOT));
        return value == null ? null : value.toString();
    }

    private boolean bool(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean booleanValue) return booleanValue;
        if (value instanceof Number number) return number.intValue() != 0;
        return "1".equals(value.toString()) || Boolean.parseBoolean(value.toString());
    }

    private int intValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(value.toString());
    }

    private Integer nullableInt(Object value) {
        return value == null ? null : intValue(value);
    }

    private Long nullableLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString());
    }

    private BigDecimal decimalNullable(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return new BigDecimal(value.toString());
    }

    private OffsetDateTime offset(Object value) {
        LocalDateTime time = localDateTime(value);
        return (time == null ? LocalDateTime.now() : time).atOffset(CN_OFFSET);
    }

    private OffsetDateTime offsetNullable(Object value) {
        LocalDateTime time = localDateTime(value);
        return time == null ? null : time.atOffset(CN_OFFSET);
    }

    private LocalDateTime localDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime time) return time;
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        if (value instanceof Date date) return new Timestamp(date.getTime()).toLocalDateTime();
        return LocalDateTime.parse(value.toString().replace(' ', 'T'));
    }

    private String defaultVersion(String value, String prefix) {
        return value == null || value.isBlank() ? "mock-v11-" + prefix + "-" + System.currentTimeMillis() : value;
    }

    private String upper(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim().toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return null;
    }

    private List<String> csv(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(",")).map(String::trim).filter(item -> !item.isEmpty()).distinct().toList();
    }

    private List<String> singletonNullable(String value) {
        return value == null ? List.of() : List.of(value);
    }

    private <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
