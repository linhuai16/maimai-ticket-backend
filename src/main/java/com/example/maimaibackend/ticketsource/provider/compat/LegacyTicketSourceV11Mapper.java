package com.example.maimaibackend.ticketsource.provider.compat;

import com.example.maimaibackend.ticketsource.gateway.model.*;
import com.example.maimaibackend.ticketsource.provider.enums.*;
import com.example.maimaibackend.ticketsource.provider.model.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 旧 gateway.model 到 V1.1 的单向兼容映射。
 *
 * <p>它只用于迁移期读取和回归，不应把 V1.1 新能力静默压缩回旧模型。</p>
 */
public final class LegacyTicketSourceV11Mapper {
    private LegacyTicketSourceV11Mapper() {}

    public static ProviderHealth health(TicketSourceHealth old) {
        if (old == null) return null;
        return new ProviderHealth(
                old.isAvailable() ? HealthStatus.UP : HealthStatus.DOWN,
                old.isAvailable() ? "available" : "unavailable",
                parseOffset(old.getProviderTime()),
                toOffset(old.getCheckedAt())
        );
    }

    public static ProviderProjectSummary project(TicketSourceProject old) {
        if (old == null) return null;
        ProjectStatus status = projectStatus(old.getSaleStatus());
        ProviderVenue venue = new ProviderVenue(
                "LEGACY:" + safe(old.getVenueName(), "UNKNOWN"),
                safe(old.getVenueName(), "未知场馆"),
                null, null, null, null, null, null, null,
                CoordinateSystem.UNKNOWN, old.getVenueName(), List.of(), null, null,
                safe(old.getDataVersion(), "legacy"), toOffset(old.getUpdateTime())
        );
        return new ProviderProjectSummary(
                old.getProviderProjectId(), old.getProjectName(),
                new ProviderStatusValue<>(status, old.getSaleStatus(), old.getSaleStatus()),
                null, null, old.getCategoryName(), null, old.getCityName(), venue,
                old.getPosterUrl(), null, null, null,
                ProviderMoney.fromMajor(old.getMinPrice(), "CNY"),
                ProviderMoney.fromMajor(old.getMaxPrice(), "CNY"),
                false, true, safe(old.getDataVersion(), "legacy"), requiredTime(old.getUpdateTime())
        );
    }

    public static ProviderSession session(TicketSourceSession old) {
        if (old == null) return null;
        ProjectStatus status = projectStatus(old.getSaleStatus());
        return new ProviderSession(
                old.getProviderSessionId(), old.getProviderProjectId(), old.getSessionName(),
                new ProviderStatusValue<>(status, old.getSaleStatus(), old.getSaleStatus()),
                toOffset(old.getStartTime()), toOffset(old.getEndTime()),
                toOffset(old.getSaleStartTime()), toOffset(old.getSaleEndTime()),
                SessionType.SINGLE, SeatMode.GENERAL_ADMISSION, false, null, null,
                List.of(), old.getLimitPerOrder(), null, null, null,
                safe(old.getDataVersion(), "legacy"), requiredTime(old.getUpdateTime())
        );
    }

    public static ProviderTicketProduct ticketProduct(TicketSourceSku old, String projectId) {
        if (old == null) return null;
        TicketProductSaleStatus sale = ticketProductStatus(old.getSaleStatus());
        return new ProviderTicketProduct(
                old.getProviderSkuId(), safe(projectId, "LEGACY_UNKNOWN_PROJECT"), old.getProviderSessionId(), old.getSkuName(),
                TicketProductType.SINGLE,
                ProviderMoney.fromMajor(old.getFacePrice(), old.getCurrencyCode()),
                ProviderMoney.fromMajor(old.getSalePrice(), old.getCurrencyCode()),
                ProviderMoney.fromMajor(old.getSettlementPrice(), old.getCurrencyCode()),
                new ProviderStatusValue<>(sale, old.getSaleStatus(), old.getSaleStatus()),
                null, inventoryMode(old.getInventoryMode()), old.getAvailableStock(), null,
                safe(old.getDataVersion(), "legacy"), requiredTime(old.getUpdateTime())
        );
    }

    public static ProviderInventory inventory(TicketSourceInventory old) {
        if (old == null) return null;
        TicketProductSaleStatus sale = ticketProductStatus(old.getSaleStatus());
        Integer stock = old.getAvailableStock();
        StockState state = stock == null
                ? (sale == TicketProductSaleStatus.ON_SALE ? StockState.AVAILABLE : StockState.UNKNOWN)
                : stock == 0 ? StockState.SOLD_OUT : StockState.AVAILABLE;
        boolean exact = stock != null && inventoryMode(old.getInventoryMode()) != InventoryMode.STATUS_ONLY;
        return new ProviderInventory(
                old.getProviderSkuId(), new ProviderStatusValue<>(sale, old.getSaleStatus(), old.getSaleStatus()),
                state, stock, exact, requiredTime(old.getProviderUpdateTime()), safe(old.getDataVersion(), "legacy")
        );
    }

    public static ProviderOrder order(TicketSourceProviderOrder old) {
        if (old == null) return null;
        ProviderOrderStatus status = orderStatus(old.getOrderStatus());
        String clientTicketNo = old.getClientOrderNo() + ":T1";
        ProviderTicketUnit unit = new ProviderTicketUnit(clientTicketNo, null, old.getProviderSkuId(), null, null);
        ProviderMoney total = ProviderMoney.fromMajor(old.getTotalAmount(), old.getCurrencyCode());
        return new ProviderOrder(
                old.getProviderOrderId(), old.getProviderOrderNo(), old.getClientOrderNo(),
                old.getProviderProjectId(), old.getProviderSessionId(),
                new ProviderStatusValue<>(status, old.getOrderStatus(), old.getOrderStatus()),
                new ProviderOrderPriceBreakdown(null, total, null, null, ProviderMoney.cny(0), ProviderMoney.cny(0), total, total),
                List.of(unit), toOffset(old.getReservationExpireTime()), toOffset(old.getCreateTime()),
                toOffset(old.getPayTime()), toOffset(old.getCancelTime()), safe(old.getDataVersion(), "legacy")
        );
    }

    public static ProviderTicketDelivery delivery(TicketSourceDelivery old) {
        if (old == null) return null;
        List<ProviderTicketCredential> tickets = new ArrayList<>();
        if (old.getTickets() != null) {
            for (TicketSourceCredential credential : old.getTickets()) tickets.add(credential(credential));
        }
        TicketDeliveryStatus status = deliveryStatus(old.getDeliveryStatus());
        return new ProviderTicketDelivery(
                old.getProviderOrderId(), new ProviderStatusValue<>(status, old.getDeliveryStatus(), old.getDeliveryStatus()),
                value(old.getExpectedTicketCount()), value(old.getIssuedCount()), value(old.getFailedCount()),
                toOffset(old.getNextPollTime()), tickets, safe(old.getDataVersion(), "legacy")
        );
    }

    public static ProviderTicketCredential credential(TicketSourceCredential old) {
        if (old == null) return null;
        CredentialType type = credentialType(old.getCredentialType());
        DynamicQrMode dynamicMode = type == CredentialType.DYNAMIC_QR ? DynamicQrMode.REMOTE_REFRESH : null;
        return new ProviderTicketCredential(
                old.getProviderTicketId(), null, null, null,
                new ProviderStatusValue<>(ticketStatus(old.getTicketStatus()), old.getTicketStatus(), old.getTicketStatus()),
                type, old.getCredentialPayload(), old.getCredentialVersion(), dynamicMode,
                new ProviderSeatAssignment(null, null, old.getSeatZone(), old.getSeatRow(), old.getSeatNumber(), null, old.getEntranceInfo()),
                ValidateStatus.UNKNOWN, toOffset(old.getIssueTime()), toOffset(old.getExpireTime()),
                old.getErrorCode(), old.getErrorMessage(), safe(old.getDataVersion(), "legacy")
        );
    }

    public static ProviderRefund refund(TicketSourceRefund old) {
        if (old == null) return null;
        return new ProviderRefund(
                old.getProviderRefundId(), old.getProviderRefundNo(), old.getProviderOrderId(), old.getClientRefundNo(),
                new ProviderStatusValue<>(refundStatus(old.getRefundStatus()), old.getRefundStatus(), old.getRefundStatus()),
                ProviderMoney.fromMajor(old.getRefundAmount(), old.getCurrencyCode()),
                ProviderMoney.fromMajor(old.getFeeAmount(), old.getCurrencyCode()), null,
                toOffset(old.getNextPollTime()), toOffset(old.getRefundTime()), old.getErrorCode(), old.getErrorMessage(),
                safe(old.getDataVersion(), "legacy")
        );
    }

    public static ProjectStatus projectStatus(String raw) {
        String s = norm(raw);
        return switch (s) {
            case "PRESALE", "PENDING_SALE" -> ProjectStatus.PRESALE;
            case "ON_SALE" -> ProjectStatus.ON_SALE;
            case "SOLD_OUT" -> ProjectStatus.SOLD_OUT;
            case "OFF_SHELF", "SUSPENDED" -> ProjectStatus.SUSPENDED;
            case "CANCELLED", "CANCELED" -> ProjectStatus.CANCELLED;
            case "ENDED" -> ProjectStatus.ENDED;
            case "DRAFT" -> ProjectStatus.DRAFT;
            default -> ProjectStatus.UNKNOWN;
        };
    }

    public static TicketProductSaleStatus ticketProductStatus(String raw) {
        String s = norm(raw);
        return switch (s) {
            case "ON_SALE" -> TicketProductSaleStatus.ON_SALE;
            case "SOLD_OUT" -> TicketProductSaleStatus.SOLD_OUT;
            case "PRESALE", "SALE_REMINDER" -> TicketProductSaleStatus.SALE_REMINDER;
            case "STOCK_REGISTRATION" -> TicketProductSaleStatus.STOCK_REGISTRATION;
            case "OFF_SHELF", "NOT_ON_SALE" -> TicketProductSaleStatus.NOT_ON_SALE;
            case "SUSPENDED" -> TicketProductSaleStatus.SUSPENDED;
            default -> TicketProductSaleStatus.UNKNOWN;
        };
    }

    public static ProviderOrderStatus orderStatus(String raw) {
        String s = norm(raw);
        return switch (s) {
            case "WAIT_PAY", "RESERVED" -> ProviderOrderStatus.RESERVED;
            case "PAID", "WAIT_USE" -> ProviderOrderStatus.PAID;
            case "ISSUING" -> ProviderOrderStatus.ISSUING;
            case "ISSUED" -> ProviderOrderStatus.ISSUED;
            case "PARTIALLY_ISSUED", "PARTIAL" -> ProviderOrderStatus.PARTIALLY_ISSUED;
            case "REFUNDING" -> ProviderOrderStatus.REFUNDING;
            case "REFUNDED", "REFUND_SUCCESS" -> ProviderOrderStatus.REFUNDED;
            case "CANCELLED", "CANCELED" -> ProviderOrderStatus.CANCELLED;
            case "EXPIRED" -> ProviderOrderStatus.EXPIRED;
            case "FAILED" -> ProviderOrderStatus.FAILED;
            default -> ProviderOrderStatus.UNKNOWN;
        };
    }

    public static ProviderTicketStatus ticketStatus(String raw) {
        String s = norm(raw);
        return switch (s) {
            case "GENERATING" -> ProviderTicketStatus.GENERATING;
            case "UNUSED", "ISSUED" -> ProviderTicketStatus.UNUSED;
            case "CHECKED", "USED" -> ProviderTicketStatus.USED;
            case "VOIDED" -> ProviderTicketStatus.VOIDED;
            case "EXPIRED", "INVALID" -> ProviderTicketStatus.EXPIRED;
            default -> ProviderTicketStatus.ERROR;
        };
    }

    public static ProviderRefundStatus refundStatus(String raw) {
        String s = norm(raw);
        return switch (s) {
            case "SUBMITTED", "PENDING_REVIEW" -> ProviderRefundStatus.SUBMITTED;
            case "PROCESSING", "REQUESTING", "RETRY_WAIT" -> ProviderRefundStatus.PROCESSING;
            case "SUCCESS", "REFUND_SUCCESS" -> ProviderRefundStatus.SUCCESS;
            case "REJECTED", "REFUND_FAILED" -> ProviderRefundStatus.REJECTED;
            case "CANCELLED", "CANCELED" -> ProviderRefundStatus.CANCELLED;
            default -> ProviderRefundStatus.FAILED;
        };
    }

    private static TicketDeliveryStatus deliveryStatus(String raw) {
        return switch (norm(raw)) {
            case "PENDING", "WAIT_PROVIDER" -> TicketDeliveryStatus.PENDING;
            case "PROCESSING" -> TicketDeliveryStatus.PROCESSING;
            case "PARTIAL" -> TicketDeliveryStatus.PARTIAL;
            case "SUCCESS", "ISSUED" -> TicketDeliveryStatus.SUCCESS;
            default -> TicketDeliveryStatus.FAILED;
        };
    }

    private static CredentialType credentialType(String raw) {
        return switch (norm(raw)) {
            case "QR_CODE", "STATIC_QR" -> CredentialType.STATIC_QR;
            case "DYNAMIC_QR" -> CredentialType.DYNAMIC_QR;
            case "ID_CARD" -> CredentialType.ID_CARD;
            case "EXCHANGE_CODE" -> CredentialType.EXCHANGE_CODE;
            case "SMS_CODE" -> CredentialType.SMS_CODE;
            case "URL" -> CredentialType.URL;
            case "TEXT" -> CredentialType.TEXT;
            case "PAPER_TICKET" -> CredentialType.PAPER_TICKET;
            default -> CredentialType.TEXT;
        };
    }

    private static InventoryMode inventoryMode(String raw) {
        return switch (norm(raw)) {
            case "REALTIME_QUERY" -> InventoryMode.REALTIME_QUERY;
            case "SNAPSHOT" -> InventoryMode.SNAPSHOT;
            default -> InventoryMode.STATUS_ONLY;
        };
    }

    private static OffsetDateTime requiredTime(java.time.LocalDateTime time) {
        return time == null ? OffsetDateTime.now(ZoneOffset.ofHours(8)) : toOffset(time);
    }

    private static OffsetDateTime toOffset(java.time.LocalDateTime time) {
        return time == null ? null : time.atOffset(ZoneOffset.ofHours(8));
    }

    private static OffsetDateTime parseOffset(String value) {
        if (value == null || value.isBlank()) return null;
        try { return OffsetDateTime.parse(value); } catch (Exception ignored) { return null; }
    }

    private static String safe(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private static int value(Integer value) { return value == null ? 0 : value; }
    private static String norm(String value) { return value == null ? "" : value.trim().toUpperCase(Locale.ROOT); }
}
