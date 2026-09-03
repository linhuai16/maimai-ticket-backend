package com.example.maimaibackend.ticketsource.purchase;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.order.OrderAudienceSnapshotDTO;
import com.example.maimaibackend.mapper.OrderMapper;
import com.example.maimaibackend.mapper.ticketsource.V11OrderMapper;
import com.example.maimaibackend.ticketsource.provider.model.ProviderMoney;
import com.example.maimaibackend.ticketsource.order.provider.V11CreateUnknownResultException;
import com.example.maimaibackend.ticketsource.order.provider.V11OrderService;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderActionRequest;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderActionResult;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderCreateRequest;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderCreateResult;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderQuoteRequest;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderQuoteResult;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderSkuContext;
import com.example.maimaibackend.ticketsource.order.provider.model.V11TicketSelection;
import com.example.maimaibackend.ticketsource.purchase.options.V12PurchaseOptionService;
import com.example.maimaibackend.ticketsource.purchase.options.model.V12PurchaseOptionsView;
import com.example.maimaibackend.ticketsource.purchase.model.V13AudienceEligibilityView;
import com.example.maimaibackend.ticketsource.purchase.model.V13EstimatedAmountView;
import com.example.maimaibackend.ticketsource.purchase.model.V13FulfillmentOptionView;
import com.example.maimaibackend.ticketsource.purchase.model.V13InventoryView;
import com.example.maimaibackend.ticketsource.purchase.model.V13MoneyView;
import com.example.maimaibackend.ticketsource.purchase.model.V13PayRequest;
import com.example.maimaibackend.ticketsource.purchase.model.V13PurchaseInitView;
import com.example.maimaibackend.ticketsource.purchase.model.V13SubmitOrderRequest;
import com.example.maimaibackend.ticketsource.purchase.model.V13SubmitOrderView;
import com.example.maimaibackend.ticketsource.purchase.model.V13TicketSubmitLine;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * V1.3-B10R-5 真实购票交互层。
 * 页面不再感知 quote；点击“提交订单”才执行最终实时校验、价格确认、锁库存和创建订单。
 */
@Service
public class V13PurchaseService {
    private static final long EXPRESS_DELIVERY_FEE_MINOR = 1200L;
    private static final String IDEMPOTENCY_STATUS_SUCCESS = "SUCCESS";
    private static final String IDEMPOTENCY_STATUS_FAILED = "FAILED";
    private static final String IDEMPOTENCY_STATUS_UNKNOWN_RESULT = "UNKNOWN_RESULT";
    private static final String IDEMPOTENCY_STATUS_MANUAL_REVIEW = "MANUAL_REVIEW";

    private final V11OrderMapper v11OrderMapper;
    private final OrderMapper orderMapper;
    private final V11OrderService orderService;
    private final V12PurchaseOptionService optionService;
    private final JdbcTemplate jdbcTemplate;

    public V13PurchaseService(V11OrderMapper v11OrderMapper,
                              OrderMapper orderMapper,
                              V11OrderService orderService,
                              V12PurchaseOptionService optionService,
                              JdbcTemplate jdbcTemplate) {
        this.v11OrderMapper = v11OrderMapper;
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.optionService = optionService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public V13PurchaseInitView init(Long userId, Long projectId, Long sessionId, Long skuId, Integer quantity) {
        int qty = normalizeQuantity(quantity);
        V11OrderSkuContext sku = requireSku(projectId, sessionId, skuId);
        if (sku.getLimitPerOrder() != null && sku.getLimitPerOrder() > 0 && qty > sku.getLimitPerOrder()) {
            throw new BusinessException("购买数量超过场次限购");
        }
        V12PurchaseOptionsView raw = optionService.get(projectId, sessionId, skuId);
        // R5/M21：库存已明确为 0/不足时优先返回用户可理解的库存文案。
        // 不能先被 saleable=false 的通用“当前票档不可购买”吞掉，否则提交页停留期间
        // Provider 库存瞬时变 0 时，用户无法知道应重新选择票档。
        if (raw.stockAvailable() != null && raw.stockAvailable() < qty) {
            throw new BusinessException("当前票档库存不足，请重新选择");
        }
        if (!raw.saleable()) {
            throw new BusinessException(raw.warnings() != null && !raw.warnings().isEmpty() ? raw.warnings().get(0) : "当前票档不可购买");
        }

        ProviderMoney ticketAmount = moneyFromMajor(sku.getLocalUnitPrice() == null
                ? BigDecimal.ZERO : sku.getLocalUnitPrice().multiply(BigDecimal.valueOf(qty)));
        List<V13FulfillmentOptionView> options = toV13Options(raw.options(), sku.getProviderCode());
        V13FulfillmentOptionView recommended = options.stream().filter(V13FulfillmentOptionView::recommended).findFirst()
                .orElse(options.isEmpty() ? null : options.get(0));
        V13MoneyView initDeliveryFee = recommended == null ? money(ProviderMoney.cny(0)) : recommended.deliveryFee();
        V13MoneyView initPayAmount = new V13MoneyView(ticketAmount.amountMinor() + initDeliveryFee.amountMinor(), "CNY");
        V13InventoryView inventory = new V13InventoryView(
                raw.stockAvailable() != null && raw.stockAvailable() <= 0 ? "SOLD_OUT" : "AVAILABLE",
                raw.stockAvailable(), raw.stockExact(), inventoryText(raw.stockAvailable()), raw.stockAvailable() == null || raw.stockAvailable() > 0);

        return new V13PurchaseInitView(
                userId, projectId, sessionId, skuId, sku.getSkuName(), qty, raw.limitPerOrder(), inventory,
                raw.purchaseMode(), options,
                new V13EstimatedAmountView("ESTIMATED", money(ticketAmount), initDeliveryFee, initPayAmount,
                        "页面金额为展示价预估，提交订单时以系统最终确认金额为准"),
                buildAudienceEligibility(userId, sessionId),
                List.of("一个订单只购买当前一个票档", "每张票必须绑定一位不同观演人", "不支持用户选座"));
    }

    public V13SubmitOrderView submit(V13SubmitOrderRequest request) {
        validateSubmitShape(request);
        String submitNo = request.clientSubmitNo().trim();
        String fingerprint = fingerprint(request);
        V13SubmitOrderView replay = resolveExistingIdempotency(findIdempotency(submitNo), fingerprint);
        if (replay != null) return replay;
        validateAudienceCanBuy(request);

        /*
         * B10R-5：先完成所有可纠正校验与第三方最终计价，再进入订单创建幂等区。
         * 地址不完整、履约方式失效、库存不足、金额变化都不应制造 FAILED 幂等记录。
         */
        V13FulfillmentOptionView option = requireOption(request.projectId(), request.sessionId(), request.skuId(), request.fulfillmentOptionCode());
        if (option.requiresAddress() && request.addressId() == null) {
            throw new BusinessException("快递票还未添加收货人信息，无法下单");
        }
        List<V11TicketSelection> tickets = request.tickets().stream()
                .map(t -> new V11TicketSelection(t.clientTicketNo().trim(), t.audienceId()))
                .toList();
        V12PurchaseOptionsView optionView = optionService.get(request.projectId(), request.sessionId(), request.skuId());
        V11OrderQuoteRequest quoteRequest = new V11OrderQuoteRequest(
                request.userId(), request.projectId(), request.sessionId(), request.skuId(), tickets,
                optionView.purchaseMode(), option.credentialType(), option.deliveryMethod(),
                option.requiresAddress() ? request.addressId() : null);
        optionService.validateUserSelection(quoteRequest);
        V11OrderQuoteResult quote = orderService.quote(quoteRequest);

        if (request.expectedPayAmountMinor() == null || request.expectedPayAmountMinor() < 0) {
            throw new BusinessException("提交金额不能为空，请刷新页面后重试");
        }
        V13MoneyView realtimeTicketAmount = userVisibleTicketAmount(quote);
        V13MoneyView realtimeDeliveryFee = money(moneyFromMajor(quote.deliveryFeeAmount()));
        V13MoneyView realtimePayAmount = money(moneyFromMajor(quote.payAmount()));
        if (realtimePayAmount.amountMinor() != request.expectedPayAmountMinor()) {
            // 金额变化不是订单创建失败：不创建订单、不写 FAILED 幂等，直接把新金额返回当前页面二次确认。
            return new V13SubmitOrderView(
                    null, null, "NOT_CREATED", "CHANGED",
                    realtimeTicketAmount, realtimeDeliveryFee, realtimePayAmount,
                    null, "重新确认", "价格已更新，请确认后重新提交");
        }

        // 只有最终金额已经被用户确认后，才进入真正的订单创建幂等区。
        try {
            insertIdempotency(request, fingerprint);
        } catch (DuplicateKeyException ex) {
            // 极端并发双击下，另一个请求可能刚刚插入了同一clientSubmitNo。
            V13SubmitOrderView concurrentReplay = resolveExistingIdempotency(findIdempotency(submitNo), fingerprint);
            if (concurrentReplay != null) return concurrentReplay;
            throw new BusinessException("订单正在提交，请稍后查看订单状态");
        }
        try {
            V11OrderCreateResult order = orderService.create(new V11OrderCreateRequest(request.userId(), quote.quoteId()));
            V13SubmitOrderView view = new V13SubmitOrderView(
                    order.orderId(), order.orderNo(), order.orderStatus(), "CONFIRMED",
                    realtimeTicketAmount, realtimeDeliveryFee, money(moneyFromMajor(order.payAmount())),
                    order.payExpireTime(), "立即支付", "订单已提交，请在15分钟内完成支付");
            markIdempotencySuccess(submitNo, order.orderId());
            return view;
        } catch (V11CreateUnknownResultException ex) {
            // G2 不是明确失败：保存本地 orderId，禁止 FAILED / 禁止再次 create。
            markIdempotencyUnknown(submitNo, ex.getOrderId(), ex.getRecoveryStatus(), sanitize(ex.getMessage()));
            throw new BusinessException(503, "订单提交结果正在确认，请稍后查看订单状态");
        } catch (RuntimeException ex) {
            markIdempotencyFailed(submitNo, "SUBMIT_FAILED", sanitize(ex.getMessage()));
            // R4/M22 已冻结：Provider 集成层 502/503 属于内部故障，用户端只返回统一业务文案。
            if (ex instanceof BusinessException businessException
                    && (businessException.getCode() == 502 || businessException.getCode() == 503)) {
                throw new BusinessException(503, "订单提交失败，请稍后重试");
            }
            throw ex;
        }
    }

    public V13SubmitOrderView pay(Long orderId, V13PayRequest request) {
        if (orderId == null || orderId <= 0 || request == null || request.userId() == null || request.userId() <= 0) {
            throw new BusinessException("orderId和userId不能为空");
        }
        V11OrderActionResult result = orderService.pay(orderId,
                new V11OrderActionRequest(request.userId(), request.payMethod() == null ? "WECHAT" : request.payMethod(), null));
        BigDecimal payAmount = selectOrderPayAmount(orderId);
        return new V13SubmitOrderView(result.orderId(), result.orderNo(), result.orderStatus(),
                "CONFIRMED", null, null, money(moneyFromMajor(payAmount)), null, "查看订单", result.message());
    }

    private V13FulfillmentOptionView requireOption(Long projectId, Long sessionId, Long skuId, String optionCode) {
        if (optionCode == null || optionCode.isBlank()) throw new BusinessException("请选择入场/取票方式");
        V13PurchaseInitView init = init(null, projectId, sessionId, skuId, 1);
        return init.fulfillmentOptions().stream()
                .filter(o -> o.optionCode().equals(optionCode.trim()))
                .findFirst().orElseThrow(() -> new BusinessException("当前票档不支持该入场/取票方式"));
    }

    private void validateSubmitShape(V13SubmitOrderRequest request) {
        if (request == null || request.userId() == null || request.userId() <= 0
                || request.projectId() == null || request.projectId() <= 0
                || request.sessionId() == null || request.sessionId() <= 0
                || request.skuId() == null || request.skuId() <= 0
                || request.fulfillmentOptionCode() == null || request.fulfillmentOptionCode().isBlank()
                || request.clientSubmitNo() == null || request.clientSubmitNo().isBlank()
                || request.tickets() == null || request.tickets().isEmpty()) {
            throw new BusinessException("用户、项目、场次、票档、提交流水和观演人不能为空");
        }
        int qty = normalizeQuantity(request.quantity());
        if (qty != request.tickets().size()) throw new BusinessException("购买数量必须等于tickets.length");
        Set<String> clientTicketNos = new HashSet<>();
        Set<Long> audienceIds = new HashSet<>();
        for (V13TicketSubmitLine ticket : request.tickets()) {
            if (ticket == null || ticket.clientTicketNo() == null || ticket.clientTicketNo().isBlank()
                    || ticket.audienceId() == null || ticket.audienceId() <= 0) {
                throw new BusinessException("每张票必须包含clientTicketNo和audienceId");
            }
            if (!clientTicketNos.add(ticket.clientTicketNo().trim())) throw new BusinessException("clientTicketNo不能重复");
            if (!audienceIds.add(ticket.audienceId())) throw new BusinessException("一个观演人在同一订单中只能购买一张票");
        }
    }

    private void validateAudienceCanBuy(V13SubmitOrderRequest request) {
        Set<Long> audienceIds = request.tickets().stream()
                .map(V13TicketSubmitLine::audienceId)
                .collect(Collectors.toCollection(HashSet::new));
        List<OrderAudienceSnapshotDTO> audiences = orderMapper.selectAudienceSnapshots(request.userId(), new ArrayList<>(audienceIds));
        if (audiences.size() != audienceIds.size()) throw new BusinessException("观演人不存在或不属于当前用户");
        List<String> hashes = audiences.stream().map(OrderAudienceSnapshotDTO::getCertificateNoHash).toList();
        if (!hashes.isEmpty() && orderMapper.countExistingAudienceBySessionAndCertHashes(request.sessionId(), hashes) > 0) {
            throw new BusinessException("当前观演人已经购买过本场次，不可重复购买");
        }
    }

    private List<V13FulfillmentOptionView> toV13Options(List<V12PurchaseOptionsView.Option> raw, String providerCode) {
        List<V13FulfillmentOptionView> result = new ArrayList<>();
        if (raw == null) return result;
        for (V12PurchaseOptionsView.Option option : raw) {
            String credential = option.ticketMode();
            String delivery = option.deliveryMode();
            long deliveryFee = "EXPRESS".equals(delivery) ? configuredDeliveryFee(providerCode, delivery) : 0L;
            result.add(new V13FulfillmentOptionView(
                    credential + "_" + delivery,
                    displayName(credential, delivery),
                    credential,
                    delivery,
                    option.requiresAddress(),
                    "DYNAMIC_QR".equals(credential),
                    option.recommended(),
                    new V13MoneyView(deliveryFee, "CNY"),
                    tips(credential, delivery)));
        }
        return result;
    }

    private List<V13AudienceEligibilityView> buildAudienceEligibility(Long userId, Long sessionId) {
        if (userId == null || userId <= 0 || sessionId == null || sessionId <= 0) return List.of();
        String sql = """
                SELECT a.audience_id,
                       CASE WHEN EXISTS (
                         SELECT 1 FROM ticket_order o
                         JOIN order_audience oa ON oa.order_id=o.order_id
                         WHERE o.session_id=?
                           AND o.order_status NOT IN ('CANCELED','REFUND_SUCCESS')
                           AND oa.certificate_no_hash=a.certificate_no_hash
                       ) THEN 0 ELSE 1 END AS can_select
                FROM audience a
                WHERE a.user_id=?
                ORDER BY a.is_default DESC, a.audience_id ASC
                """;
        return jdbcTemplate.query(sql, ps -> {
            ps.setLong(1, sessionId);
            ps.setLong(2, userId);
        }, (rs, rowNum) -> {
            boolean canSelect = rs.getInt("can_select") == 1;
            return new V13AudienceEligibilityView(rs.getLong("audience_id"), canSelect,
                    canSelect ? "" : "已购买过本场次");
        });
    }


    /**
     * V1.3 用户侧提交页本期只展示“票款 + 快递费 = 合计”。
     * V11 内部可能存在第三方优惠/服务费，为避免把 provider 促销字段直接暴露给用户，
     * 这里把除配送费以外的最终应付金额归入用户侧票款，确保三项金额严格可加总。
     */
    private V13MoneyView userVisibleTicketAmount(V11OrderQuoteResult quote) {
        BigDecimal deliveryFee = quote.deliveryFeeAmount() == null ? BigDecimal.ZERO : quote.deliveryFeeAmount();
        BigDecimal payAmount = quote.payAmount() == null ? BigDecimal.ZERO : quote.payAmount();
        BigDecimal ticketAmount = payAmount.subtract(deliveryFee).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return money(moneyFromMajor(ticketAmount));
    }

    private long configuredDeliveryFee(String providerCode, String deliveryMethod) {
        if (!"EXPRESS".equals(deliveryMethod)) return 0L;
        try {
            Long amount = jdbcTemplate.queryForObject("""
                    SELECT amount_minor
                    FROM ticket_source_delivery_fee_config
                    WHERE provider_code=? AND delivery_method='EXPRESS' AND enabled=1
                    ORDER BY config_id DESC LIMIT 1
                    """, Long.class, providerCode);
            return amount == null ? EXPRESS_DELIVERY_FEE_MINOR : amount;
        } catch (RuntimeException ex) {
            return EXPRESS_DELIVERY_FEE_MINOR;
        }
    }

    private String displayName(String credential, String delivery) {
        return switch (credential) {
            case "DYNAMIC_QR" -> "电子票 - 动态二维码";
            case "STATIC_QR" -> "电子票 - 普通二维码";
            case "ID_CARD" -> "电子票 - 身份证入场";
            case "SMS_CODE" -> "电子票 - 短信凭证";
            case "PAPER_TICKET" -> "EXPRESS".equals(delivery) ? "纸质票 - 快递配送" : "纸质票 - 现场取票";
            case "PROVIDER_WALLET" -> "电子票 - 官方票夹";
            default -> "电子凭证";
        };
    }

    private List<String> tips(String credential, String delivery) {
        if ("PAPER_TICKET".equals(credential) && "SELF_PICKUP".equals(delivery)) {
            return List.of("请于演出当天提前到演出场馆现场取票，具体以场馆现场指引为准");
        }
        if ("PAPER_TICKET".equals(credential) && "EXPRESS".equals(delivery)) {
            return List.of("纸质票将按订单收货地址寄送，请关注票夹中的物流状态");
        }
        return List.of("支付成功后，前往票夹查看入场凭证");
    }

    private V11OrderSkuContext requireSku(Long projectId, Long sessionId, Long skuId) {
        if (projectId == null || sessionId == null || skuId == null) throw new BusinessException("projectId/sessionId/skuId不能为空");
        List<V11OrderSkuContext> rows = v11OrderMapper.selectSkuContexts(projectId, sessionId, List.of(skuId));
        if (rows.size() != 1) throw new BusinessException("当前票档暂不可购买，请重新选择");
        return rows.get(0);
    }

    private String inventoryText(Integer availableStock) {
        if (availableStock == null) return "库存待实时确认";
        if (availableStock <= 0) return "已售罄";
        if (availableStock <= 10) return "少量余票";
        return "库存充足";
    }

    private int normalizeQuantity(Integer value) {
        if (value == null || value <= 0) return 1;
        return value;
    }

    private V13MoneyView money(ProviderMoney value) {
        if (value == null) return new V13MoneyView(0, "CNY");
        return new V13MoneyView(value.amountMinor(), value.currency());
    }

    private ProviderMoney moneyFromMajor(BigDecimal amount) {
        return ProviderMoney.fromMajor(amount == null ? BigDecimal.ZERO.setScale(2) : amount.setScale(2, RoundingMode.HALF_UP), "CNY");
    }

    private BigDecimal selectOrderPayAmount(Long orderId) {
        try {
            return jdbcTemplate.queryForObject("SELECT pay_amount FROM ticket_order WHERE order_id=?", BigDecimal.class, orderId);
        } catch (EmptyResultDataAccessException ex) {
            throw new BusinessException("订单不存在");
        }
    }

    private V13SubmitOrderView viewFromOrder(Long orderId, String priceStatus, String userMessage) {
        try {
            return jdbcTemplate.queryForObject("""
                    SELECT order_id, order_no, order_status, pay_amount, pay_expire_time
                    FROM ticket_order WHERE order_id=?
                    """, (rs, rowNum) -> new V13SubmitOrderView(
                    rs.getLong("order_id"), rs.getString("order_no"), rs.getString("order_status"), priceStatus,
                    null, null, money(moneyFromMajor(rs.getBigDecimal("pay_amount"))),
                    rs.getTimestamp("pay_expire_time") == null ? null : rs.getTimestamp("pay_expire_time").toLocalDateTime(),
                    "立即支付", userMessage), orderId);
        } catch (EmptyResultDataAccessException ex) {
            throw new BusinessException("幂等订单不存在，请刷新页面后重试");
        }
    }

    private V13SubmitOrderView resolveExistingIdempotency(Map<String, Object> existing, String fingerprint) {
        if (existing == null) return null;
        String existingFingerprint = String.valueOf(existing.get("request_fingerprint"));
        if (!existingFingerprint.equals(fingerprint)) {
            throw new BusinessException("本次提交内容已变化，请刷新页面后重试");
        }
        Object orderId = existing.get("order_id");
        String status = String.valueOf(existing.get("submit_status"));
        if (orderId != null && IDEMPOTENCY_STATUS_SUCCESS.equals(status)) {
            return viewFromOrder(((Number) orderId).longValue(), "CONFIRMED", "订单已提交，请在支付时限内完成支付");
        }
        if (IDEMPOTENCY_STATUS_FAILED.equals(status)) {
            throw new BusinessException("上一次提交失败，请刷新页面后重试");
        }
        if (IDEMPOTENCY_STATUS_MANUAL_REVIEW.equals(status)) {
            throw new BusinessException(503, "订单提交结果需要进一步确认，请稍后查看订单状态");
        }
        if (orderId != null && IDEMPOTENCY_STATUS_UNKNOWN_RESULT.equals(status)) {
            Long localOrderId = ((Number) orderId).longValue();
            try {
                V11OrderCreateResult recovered = orderService.recoverUnknownCreate(localOrderId);
                markIdempotencySuccess(String.valueOf(existing.get("client_submit_no")), recovered.orderId());
                return viewFromOrder(recovered.orderId(), "CONFIRMED", "订单已确认，请在支付时限内完成支付");
            } catch (V11CreateUnknownResultException ex) {
                markIdempotencyUnknown(String.valueOf(existing.get("client_submit_no")), ex.getOrderId(),
                        ex.getRecoveryStatus(), sanitize(ex.getMessage()));
                if (IDEMPOTENCY_STATUS_MANUAL_REVIEW.equals(ex.getRecoveryStatus())) {
                    throw new BusinessException(503, "订单提交结果需要进一步确认，请稍后查看订单状态");
                }
                throw new BusinessException(503, "订单提交结果正在确认，请稍后查看订单状态");
            }
        }
        throw new BusinessException("订单正在提交，请稍后查看订单状态");
    }

    private Map<String, Object> findIdempotency(String clientSubmitNo) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT client_submit_no,user_id,request_fingerprint,order_id,submit_status,error_code,error_message FROM ticket_source_submit_idempotency WHERE client_submit_no=? LIMIT 1",
                clientSubmitNo);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private void insertIdempotency(V13SubmitOrderRequest request, String fingerprint) {
        int rows = jdbcTemplate.update("""
                INSERT INTO ticket_source_submit_idempotency
                (client_submit_no,user_id,request_fingerprint,submit_status,create_time,update_time)
                VALUES (?,?,?,'PROCESSING',NOW(),NOW())
                """, request.clientSubmitNo().trim(), request.userId(), fingerprint);
        if (rows != 1) throw new BusinessException("提交订单失败，请重试");
    }

    private void markIdempotencySuccess(String clientSubmitNo, Long orderId) {
        jdbcTemplate.update("""
                UPDATE ticket_source_submit_idempotency
                SET order_id=?, submit_status=?, error_code=NULL, error_message=NULL, update_time=NOW()
                WHERE client_submit_no=?
                """, orderId, IDEMPOTENCY_STATUS_SUCCESS, clientSubmitNo);
    }

    private void markIdempotencyUnknown(String clientSubmitNo, Long orderId, String recoveryStatus, String errorMessage) {
        String status = IDEMPOTENCY_STATUS_MANUAL_REVIEW.equals(recoveryStatus)
                ? IDEMPOTENCY_STATUS_MANUAL_REVIEW : IDEMPOTENCY_STATUS_UNKNOWN_RESULT;
        jdbcTemplate.update("""
                UPDATE ticket_source_submit_idempotency
                SET order_id=?, submit_status=?, error_code='UNKNOWN_RESULT', error_message=?, update_time=NOW()
                WHERE client_submit_no=? AND submit_status IN ('PROCESSING','UNKNOWN_RESULT','MANUAL_REVIEW')
                """, orderId, status, errorMessage, clientSubmitNo);
    }

    private void markIdempotencyFailed(String clientSubmitNo, String errorCode, String errorMessage) {
        jdbcTemplate.update("""
                UPDATE ticket_source_submit_idempotency
                SET submit_status=?, error_code=?, error_message=?, update_time=NOW()
                WHERE client_submit_no=? AND submit_status='PROCESSING'
                """, IDEMPOTENCY_STATUS_FAILED, errorCode, errorMessage, clientSubmitNo);
    }

    private String fingerprint(V13SubmitOrderRequest request) {
        String ticketPart = request.tickets().stream()
                .sorted(Comparator.comparing(V13TicketSubmitLine::clientTicketNo))
                .map(t -> t.clientTicketNo().trim() + ":" + t.audienceId())
                .collect(Collectors.joining("|"));
        String raw = request.userId() + ";" + request.projectId() + ";" + request.sessionId() + ";" + request.skuId() + ";"
                + normalizeQuantity(request.quantity()) + ";" + request.fulfillmentOptionCode().trim() + ";"
                + (request.addressId() == null ? "" : request.addressId()) + ";"
                + (request.expectedPayAmountMinor() == null ? "" : request.expectedPayAmountMinor()) + ";" + ticketPart;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) builder.append(String.format("%02x", b));
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) return "提交订单失败";
        return message.replace("第三方票源", "票务系统")
                .replace("第三方", "票务系统")
                .replaceAll("(?i)provider", "system")
                .replaceAll("MOCK-SKU-[A-Za-z0-9-]+", "当前票档");
    }
}
