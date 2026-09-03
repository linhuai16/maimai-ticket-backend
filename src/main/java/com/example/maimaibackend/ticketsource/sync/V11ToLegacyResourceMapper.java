package com.example.maimaibackend.ticketsource.sync;

import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSku;
import com.example.maimaibackend.ticketsource.sync.model.TicketSourceProjectBundle;
import com.example.maimaibackend.ticketsource.provider.enums.InventoryMode;
import com.example.maimaibackend.ticketsource.provider.enums.ProjectStatus;
import com.example.maimaibackend.ticketsource.provider.enums.TicketProductSaleStatus;
import com.example.maimaibackend.ticketsource.provider.model.*;
import com.example.maimaibackend.ticketsource.resource.provider.model.V11ResourceBundle;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public final class V11ToLegacyResourceMapper {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private V11ToLegacyResourceMapper() {}

    public static TicketSourceProjectBundle convert(V11ResourceBundle source) {
        return convert(source, buildDetail(source.getProject()));
    }

    public static TicketSourceProjectBundle convert(V11ResourceBundle source, String sanitizedDetailContent) {
        ProviderProjectDetail detail = source.getProject();
        ProviderProjectSummary summary = detail.summary();
        ProviderVenue venue = source.getVenue() == null ? summary.venue() : source.getVenue();
        TicketSourceProject legacyProject = new TicketSourceProject();
        legacyProject.setProviderProjectId(summary.projectId());
        legacyProject.setProjectName(summary.projectName());
        legacyProject.setCategoryName(first(summary.categoryName(), summary.categoryCode(), "其他"));
        legacyProject.setCityName(summary.cityName());
        legacyProject.setVenueName(venue == null ? "待确认场馆" : venue.venueName());
        legacyProject.setPosterUrl(first(summary.posterUrl(), "default_project_poster"));
        legacyProject.setDetailContent(sanitizedDetailContent);
        legacyProject.setSaleStatus(projectStatus(summary.projectStatus().status()));
        legacyProject.setMinPrice(major(summary.minPrice()));
        legacyProject.setMaxPrice(major(summary.maxPrice()));
        legacyProject.setDataVersion(summary.version());
        legacyProject.setUpdateTime(local(summary.updatedAt()));

        TicketSourceProjectBundle bundle = new TicketSourceProjectBundle();
        bundle.setProject(legacyProject);
        List<TicketSourceProjectBundle.SessionBundle> sessions = new ArrayList<>();
        for (V11ResourceBundle.SessionBundle sourceSession : source.getSessions()) {
            ProviderSession session = sourceSession.getSession();
            TicketSourceSession legacySession = new TicketSourceSession();
            legacySession.setProviderSessionId(session.sessionId());
            legacySession.setProviderProjectId(session.projectId());
            legacySession.setSessionName(session.sessionName());
            legacySession.setCityName(summary.cityName());
            legacySession.setVenueName(venue == null ? "待确认场馆" : venue.venueName());
            legacySession.setVenueAddress(venue == null ? "第三方票源导入，详细地址待完善" : first(venue.address(), "第三方票源导入，详细地址待完善"));
            legacySession.setStartTime(local(session.startAt()));
            legacySession.setEndTime(local(session.endAt()));
            legacySession.setSaleStartTime(local(session.saleStartAt()));
            legacySession.setSaleEndTime(local(session.saleEndAt()));
            legacySession.setSaleStatus(projectStatus(session.sessionStatus().status()));
            legacySession.setLimitPerOrder(minPositive(detail.purchaseLimitPerOrder(), session.maxQuantityPerOrder()));
            legacySession.setDeliveryType(resolveSessionDeliveryType(sourceSession.getTicketProducts()));
            legacySession.setDataVersion(session.version());
            legacySession.setUpdateTime(local(session.updatedAt()));

            TicketSourceProjectBundle.SessionBundle targetSession = new TicketSourceProjectBundle.SessionBundle();
            targetSession.setSession(legacySession);
            List<TicketSourceSku> skus = new ArrayList<>();
            for (ProviderTicketProduct product : sourceSession.getTicketProducts()) {
                ProviderInventory inventory = sourceSession.inventory(product.ticketProductId());
                TicketSourceSku sku = new TicketSourceSku();
                sku.setProviderSkuId(product.ticketProductId());
                sku.setProviderSessionId(product.sessionId());
                sku.setSkuName(product.productName());
                sku.setFacePrice(major(product.facePrice()));
                sku.setSalePrice(major(product.salePrice()));
                sku.setSettlementPrice(major(product.settlementPrice()));
                sku.setCurrencyCode("CNY");
                sku.setInventoryMode(inventoryMode(product.inventoryMode()));
                sku.setAvailableStock(inventory == null ? product.availableStock() : inventory.availableStock());
                sku.setSaleStatus(ticketStatus(inventory == null ? product.saleStatus().status() : inventory.saleStatus().status()));
                sku.setDataVersion(inventory == null ? product.version() : inventory.version());
                sku.setUpdateTime(local(inventory == null ? product.updatedAt() : inventory.snapshotAt()));
                skus.add(sku);
            }
            targetSession.setSkus(skus);
            sessions.add(targetSession);
        }
        bundle.setSessions(sessions);
        return bundle;
    }

    public static String projectStatus(ProjectStatus status) {
        if (status == null) return "OFF_SHELF";
        return switch (status) {
            case PENDING_SALE, PRESALE -> "PRESALE";
            case ON_SALE -> "ON_SALE";
            case SOLD_OUT -> "SOLD_OUT";
            case CANCELLED, ENDED -> "ENDED";
            case DRAFT, SUSPENDED, UNKNOWN -> "OFF_SHELF";
        };
    }

    public static String ticketStatus(TicketProductSaleStatus status) {
        if (status == null) return "OFF_SHELF";
        return switch (status) {
            case ON_SALE -> "ON_SALE";
            case SOLD_OUT -> "SOLD_OUT";
            case SALE_REMINDER, STOCK_REGISTRATION -> "PRESALE";
            case NOT_ON_SALE, SUSPENDED, UNKNOWN -> "OFF_SHELF";
        };
    }

    public static String inventoryMode(InventoryMode mode) {
        if (mode == null) return "UNKNOWN";
        return switch (mode) {
            case REALTIME_QUERY -> "REALTIME_QUERY";
            case SNAPSHOT -> "SNAPSHOT";
            case STATUS_ONLY -> "UNKNOWN";
        };
    }

    private static String resolveSessionDeliveryType(List<ProviderTicketProduct> products) {
        boolean hasPaper = false;
        boolean hasElectronic = false;
        for (ProviderTicketProduct product : products == null ? List.<ProviderTicketProduct>of() : products) {
            if (isPaperTicket(product)) hasPaper = true;
            else hasElectronic = true;
        }
        if (hasPaper && hasElectronic) return "MIXED";
        if (hasPaper) return "PAPER_TICKET";
        return "ETICKET";
    }

    private static boolean isPaperTicket(ProviderTicketProduct product) {
        if (product == null) return false;
        String subStatus = product.subStatus() == null ? "" : product.subStatus().trim().toUpperCase();
        for (String token : subStatus.split("[|,;\\s]+")) {
            if ("PAPER_TICKET".equals(token)
                    || "EXPRESS_SUPPORTED".equals(token)
                    || "SELF_PICKUP_SUPPORTED".equals(token)) {
                return true;
            }
        }
        String name = product.productName() == null ? "" : product.productName();
        return name.contains("纸质票") || name.toUpperCase().contains("PAPER");
    }

    static String buildDetail(ProviderProjectDetail detail) {
        StringBuilder builder = new StringBuilder();
        if (notBlank(detail.subtitle())) builder.append("<h2>").append(escape(detail.subtitle())).append("</h2>");
        if (notBlank(detail.introduction())) builder.append("<p>").append(escape(detail.introduction())).append("</p>");
        if (notBlank(detail.showDetail())) builder.append(detail.showDetail());
        return builder.length() == 0 ? null : builder.toString();
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    private static java.math.BigDecimal major(ProviderMoney money) { return money == null ? null : money.toMajor(); }
    private static LocalDateTime local(OffsetDateTime time) { return time == null ? null : time.atZoneSameInstant(ZONE).toLocalDateTime(); }
    private static boolean notBlank(String value) { return value != null && !value.isBlank(); }
    private static Integer minPositive(Integer left, Integer right) {
        Integer leftValue = left != null && left > 0 ? left : null;
        Integer rightValue = right != null && right > 0 ? right : null;
        if (leftValue == null) return rightValue == null ? 1 : rightValue;
        if (rightValue == null) return leftValue;
        return Math.min(leftValue, rightValue);
    }

    private static String first(String... values) {
        for (String value : values) if (notBlank(value)) return value;
        return null;
    }
}
