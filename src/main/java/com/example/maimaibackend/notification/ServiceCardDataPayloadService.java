package com.example.maimaibackend.notification;

import com.example.maimaibackend.service.HomeService;
import com.example.maimaibackend.service.OrderQueryService;
import com.example.maimaibackend.service.TicketService;
import com.example.maimaibackend.vo.home.HomeRecommendItemVO;
import com.example.maimaibackend.vo.order.OrderListItemVO;
import com.example.maimaibackend.vo.ticket.TicketFolderItemVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceCardDataPayloadService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter UPDATE_TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final OrderQueryService orderQueryService;
    private final TicketService ticketService;
    private final HomeService homeService;
    private final PushProperties properties;

    public ServiceCardDataPayloadService(OrderQueryService orderQueryService, TicketService ticketService,
                                         HomeService homeService, PushProperties properties) {
        this.orderQueryService = orderQueryService;
        this.ticketService = ticketService;
        this.homeService = homeService;
        this.properties = properties;
    }

    public Map<String, Object> load(Long userId, String cityName) {
        List<OrderListItemVO> waitPay = orderQueryService.getOrderList(userId, "WAIT_PAY", 1, 20).getOrders();
        if (waitPay != null && !waitPay.isEmpty()) return waitPay(userId, waitPay.get(0));

        List<OrderListItemVO> waitUse = orderQueryService.getOrderList(userId, "WAIT_USE", 1, 50).getOrders();
        if (waitUse != null) {
            OrderListItemVO ticketing = waitUse.stream()
                    .filter(item -> "TICKETING".equals(item.getActionType()))
                    .findFirst().orElse(null);
            if (ticketing != null) return ticketing(userId, ticketing);
        }

        List<TicketFolderItemVO> tickets = ticketService.getTicketFolder(userId).getItems();
        if (tickets != null) {
            TicketFolderItemVO upcoming = tickets.stream()
                    .filter(item -> "UNUSED".equals(item.getDisplayStatus()))
                    .filter(item -> item.getStartTime() == null || !item.getStartTime().isBefore(LocalDateTime.now()))
                    .min(Comparator.comparing(TicketFolderItemVO::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElse(null);
            if (upcoming != null) return upcoming(userId, upcoming);
        }

        List<HomeRecommendItemVO> recommends = homeService.getHomeIndex(cityName, 1, 0).getRecommends();
        if (recommends != null && !recommends.isEmpty()) return hot(userId, cityName, recommends.get(0));
        return empty(userId);
    }

    private Map<String, Object> waitPay(Long userId, OrderListItemVO order) {
        Map<String, Object> data = base("WAIT_PAY", "待支付", text(order.getTitle()));
        data.put("primaryText", amount(order.getPayAmount()) + " · " + count(order.getTotalQuantity()) + "张");
        long expireTimestamp = timestamp(order.getPayExpireTime());
        long remaining = expireTimestamp <= 0 ? 0 : Math.max(0, Math.min(86_399_999, expireTimestamp - System.currentTimeMillis()));
        data.put("secondaryText", expireTimestamp <= 0 ? "支付有效期以订单详情为准" :
                remaining > 0 ? "" : "支付已超时 · 请查看订单");
        data.put("actionText", "去支付");
        data.put("target", "ORDER_DETAIL");
        data.put("userId", String.valueOf(userId));
        data.put("orderId", id(order.getOrderId()));
        data.put("posterUrl", poster(order.getPosterUrl()));
        data.put("payRemainingMillis", remaining);
        data.put("payExpireTimestamp", expireTimestamp);
        return data;
    }

    private Map<String, Object> ticketing(Long userId, OrderListItemVO order) {
        Map<String, Object> data = base("TICKETING", "出票中", text(order.getTitle()));
        data.put("primaryText", count(order.getTotalQuantity()) + "张");
        data.put("secondaryText", dateTime(order.getStartTime()));
        data.put("actionText", "查看进度");
        data.put("target", "ORDER_DETAIL");
        data.put("userId", String.valueOf(userId));
        data.put("orderId", id(order.getOrderId()));
        data.put("posterUrl", poster(order.getPosterUrl()));
        return data;
    }

    private Map<String, Object> upcoming(Long userId, TicketFolderItemVO ticket) {
        Map<String, Object> data = base("UPCOMING", "下一场观演", text(ticket.getTitle()));
        data.put("primaryText", dateTime(ticket.getStartTime()));
        String venue = text(ticket.getVenueName());
        data.put("secondaryText", venue.isEmpty() ? count(ticket.getTicketCount()) + "张" :
                venue + " · " + count(ticket.getTicketCount()) + "张");
        boolean hasTicket = ticket.getDefaultTicketId() != null;
        data.put("actionText", hasTicket ? "查看票夹" : "查看订单");
        data.put("target", hasTicket ? "TICKET_DETAIL" : "ORDER_DETAIL");
        data.put("userId", String.valueOf(userId));
        data.put("orderId", id(ticket.getOrderId()));
        data.put("ticketId", id(ticket.getDefaultTicketId()));
        data.put("projectId", id(ticket.getProjectId()));
        data.put("sessionId", id(ticket.getSessionId()));
        data.put("posterUrl", poster(ticket.getPosterUrl()));
        return data;
    }

    private Map<String, Object> hot(Long userId, String cityName, HomeRecommendItemVO item) {
        String city = text(cityName).isEmpty() ? "北京" : text(cityName);
        Map<String, Object> data = base("HOT", city + "热门", text(item.getTitle()));
        data.put("primaryText", item.getMinPrice() == null ? "热门演出" : amount(item.getMinPrice()) + "起");
        data.put("secondaryText", dateTime(item.getStartTime()));
        data.put("actionText", "查看演出");
        data.put("target", "PERFORMANCE_DETAIL");
        data.put("userId", String.valueOf(userId));
        data.put("projectId", id(item.getProjectId()));
        data.put("sessionId", id(item.getSessionId()));
        data.put("posterUrl", poster(item.getPosterUrl()));
        return data;
    }

    private Map<String, Object> empty(Long userId) {
        Map<String, Object> data = base("EMPTY", "暂无数据", "暂无可展示的票务信息");
        data.put("primaryText", "打开应用后可以刷新演出内容");
        data.put("actionText", "打开应用");
        data.put("target", "HOME");
        data.put("userId", String.valueOf(userId));
        return data;
    }

    private Map<String, Object> base(String statusCode, String statusText, String title) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("statusCode", statusCode);
        data.put("statusText", statusText);
        data.put("title", title);
        data.put("primaryText", "");
        data.put("secondaryText", "");
        data.put("actionText", "打开应用");
        data.put("target", "HOME");
        data.put("userId", "");
        data.put("orderId", "");
        data.put("ticketId", "");
        data.put("projectId", "");
        data.put("sessionId", "");
        data.put("posterUrl", "");
        data.put("updatedText", "更新于 " + LocalDateTime.now().format(UPDATE_TIME));
        data.put("payRemainingMillis", 0L);
        data.put("payExpireTimestamp", 0L);
        return data;
    }

    private long timestamp(LocalDateTime value) {
        return value == null ? 0 : value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private String dateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    private String amount(BigDecimal value) {
        return "¥" + (value == null ? BigDecimal.ZERO : value).setScale(2).toPlainString();
    }

    private String id(Long value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int count(Integer value) {
        return value == null ? 0 : value;
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String poster(String value) {
        String path = text(value);
        if (path.isEmpty() || path.startsWith("https://") || path.startsWith("http://") || !path.startsWith("/")) return path;
        String base = text(properties.getPublicBaseUrl());
        return base.endsWith("/") ? base.substring(0, base.length() - 1) + path : base + path;
    }
}
