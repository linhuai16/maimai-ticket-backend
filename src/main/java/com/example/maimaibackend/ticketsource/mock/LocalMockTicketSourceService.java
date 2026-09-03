package com.example.maimaibackend.ticketsource.mock;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.ticketsource.LocalMockTicketSourceMapper;
import com.example.maimaibackend.ticketsource.domain.enums.TicketSourceInventoryMode;
import com.example.maimaibackend.ticketsource.domain.enums.TicketSourceSaleStatus;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceAdapterException;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceGatewayErrorCode;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceOperation;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCancelOrderRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceConfirmPaymentRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCreateOrderRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceHealth;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceDelivery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCredential;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceIssueRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourcePage;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProjectQuery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProviderOrder;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefund;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefundRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSku;
import com.example.maimaibackend.ticketsource.mock.dto.MockBehaviorUpdateRequest;
import com.example.maimaibackend.ticketsource.mock.dto.MockInventoryUpdateRequest;
import com.example.maimaibackend.ticketsource.mock.dto.MockIssuePlanUpdateRequest;
import com.example.maimaibackend.ticketsource.mock.dto.MockRefundPlanUpdateRequest;
import com.example.maimaibackend.ticketsource.mock.dto.MockSaleStatusUpdateRequest;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceBehavior;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceOrder;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceOrderSku;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceDelivery;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceCredential;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceRefund;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceRefundPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class LocalMockTicketSourceService {
    private final LocalMockTicketSourceMapper mapper;

    public LocalMockTicketSourceService(LocalMockTicketSourceMapper mapper) {
        this.mapper = mapper;
    }

    public TicketSourceHealth health() {
        applyBehavior(TicketSourceOperation.HEALTH);
        TicketSourceHealth health = new TicketSourceHealth();
        health.setAvailable(true);
        health.setProviderTime(LocalDateTime.now().toString());
        health.setProjectCount(mapper.countAllProjects());
        health.setSessionCount(mapper.countAllSessions());
        health.setSkuCount(mapper.countAllSkus());
        health.setCheckedAt(LocalDateTime.now());
        return health;
    }

    public TicketSourcePage<TicketSourceProject> queryProjects(TicketSourceProjectQuery rawQuery) {
        applyBehavior(TicketSourceOperation.QUERY_PROJECTS);
        TicketSourceProjectQuery query = rawQuery == null
                ? new TicketSourceProjectQuery().normalized()
                : rawQuery.normalized();
        int total = mapper.countProjects(query.getKeyword(), query.getCityName());
        List<TicketSourceProject> records = mapper.selectProjects(
                query.getKeyword(), query.getCityName(), query.offset(), query.getPageSize());
        return new TicketSourcePage<>(records, total, query.getPageNo(), query.getPageSize());
    }

    public TicketSourceProject getProject(String providerProjectId) {
        applyBehavior(TicketSourceOperation.GET_PROJECT);
        TicketSourceProject project = mapper.selectProjectById(providerProjectId);
        if (project == null) {
            throw notFound("MOCK_PROJECT_NOT_FOUND", "模拟票源项目不存在: " + providerProjectId);
        }
        return project;
    }

    public List<TicketSourceSession> querySessions(String providerProjectId) {
        applyBehavior(TicketSourceOperation.QUERY_SESSIONS);
        if (mapper.selectProjectById(providerProjectId) == null) {
            throw notFound("MOCK_PROJECT_NOT_FOUND", "模拟票源项目不存在: " + providerProjectId);
        }
        return mapper.selectSessionsByProjectId(providerProjectId);
    }

    public List<TicketSourceSku> querySkus(String providerSessionId) {
        applyBehavior(TicketSourceOperation.QUERY_SKUS);
        if (mapper.countSessionById(providerSessionId) == 0) {
            throw notFound("MOCK_SESSION_NOT_FOUND", "模拟票源场次不存在: " + providerSessionId);
        }
        return mapper.selectSkusBySessionId(providerSessionId);
    }

    public TicketSourceInventory queryInventory(String providerSkuId) {
        applyBehavior(TicketSourceOperation.QUERY_INVENTORY);
        TicketSourceInventory inventory = mapper.selectInventoryBySkuId(providerSkuId);
        if (inventory == null) {
            throw notFound("MOCK_SKU_NOT_FOUND", "模拟票源票档不存在: " + providerSkuId);
        }
        return inventory;
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketSourceProviderOrder createOrder(TicketSourceCreateOrderRequest request) {
        applyBehavior(TicketSourceOperation.CREATE_ORDER);
        validateCreateOrderRequest(request);

        MockTicketSourceOrder existing = mapper.selectOrderByCreateIdempotencyKey(request.getIdempotencyKey().trim());
        if (existing != null) {
            validateIdempotentCreate(existing, request);
            return toProviderOrder(existing);
        }

        String providerSkuId = request.getProviderSkuId().trim();
        MockTicketSourceOrderSku sku = mapper.selectOrderSkuById(providerSkuId);
        if (sku == null || !Boolean.TRUE.equals(sku.getEnabled())) {
            throw notFound("MOCK_SKU_NOT_FOUND", "模拟票源票档不存在: " + providerSkuId);
        }
        if (!("ON_SALE".equals(sku.getSaleStatus()) || "PRESALE".equals(sku.getSaleStatus()))) {
            throw remoteError("MOCK_SKU_NOT_SALEABLE", "模拟票源票档当前不可购买", false);
        }
        if (sku.getAvailableStock() == null) {
            throw remoteError("MOCK_INVENTORY_UNKNOWN", "模拟票源未返回可预占库存", true);
        }
        if (request.getExpectedUnitPrice() == null
                || sku.getSalePrice().compareTo(request.getExpectedUnitPrice()) != 0) {
            throw remoteError("MOCK_PRICE_CHANGED", "第三方票价已变化，请刷新后重试", false);
        }
        BigDecimal expectedTotal = sku.getSalePrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(2);
        if (request.getPayAmount() == null || expectedTotal.compareTo(request.getPayAmount()) != 0) {
            throw remoteError("MOCK_AMOUNT_MISMATCH", "第三方订单金额不一致", false);
        }
        if (!sku.getSourceProjectId().equals(request.getProviderProjectId())
                || !sku.getSourceSessionId().equals(request.getProviderSessionId())) {
            throw remoteError("MOCK_RESOURCE_MISMATCH", "第三方项目、场次和票档不匹配", false);
        }

        String dataVersion = nextDataVersion("order-reserve");
        int reserveRows = mapper.reserveSkuStock(providerSkuId, request.getQuantity(), dataVersion);
        if (reserveRows != 1) {
            throw remoteError("MOCK_INVENTORY_NOT_ENOUGH", "第三方票源库存不足", false);
        }

        LocalDateTime now = LocalDateTime.now();
        MockTicketSourceOrder order = new MockTicketSourceOrder();
        order.setProviderOrderId(generateProviderOrderId());
        order.setProviderOrderNo(generateProviderOrderNo());
        order.setClientOrderNo(request.getClientOrderNo().trim());
        order.setSourceProjectId(sku.getSourceProjectId());
        order.setSourceSessionId(sku.getSourceSessionId());
        order.setSourceSkuId(providerSkuId);
        order.setQuantity(request.getQuantity());
        order.setUnitPrice(sku.getSalePrice());
        order.setTotalAmount(expectedTotal);
        order.setCurrencyCode(normalizeCurrency(request.getCurrencyCode(), sku.getCurrencyCode()));
        order.setOrderStatus("WAIT_PAY");
        order.setCreateIdempotencyKey(request.getIdempotencyKey().trim());
        order.setReservationExpireTime(normalizeExpireTime(request.getReservationExpireTime(), now));
        order.setDataVersion(dataVersion);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        mapper.insertMockOrder(order);
        return toProviderOrder(mapper.selectOrderByProviderOrderId(order.getProviderOrderId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketSourceProviderOrder confirmPayment(
            String providerOrderId,
            TicketSourceConfirmPaymentRequest request
    ) {
        applyBehavior(TicketSourceOperation.CONFIRM_PAYMENT);
        String normalizedOrderId = normalizeRequiredId(providerOrderId, "第三方订单ID");
        if (request == null || normalizeNullable(request.getIdempotencyKey()) == null) {
            throw remoteError("MOCK_PAYMENT_PARAM_INVALID", "支付确认参数不完整", false);
        }
        MockTicketSourceOrder order = mapper.selectOrderByProviderOrderId(normalizedOrderId);
        if (order == null) {
            throw notFound("MOCK_ORDER_NOT_FOUND", "模拟第三方订单不存在: " + normalizedOrderId);
        }
        if ("PAID".equals(order.getOrderStatus())) {
            if (order.getPaymentIdempotencyKey() != null
                    && !order.getPaymentIdempotencyKey().equals(request.getIdempotencyKey().trim())) {
                throw remoteError("MOCK_PAYMENT_IDEMPOTENCY_CONFLICT", "支付幂等键冲突", false);
            }
            return toProviderOrder(order);
        }
        if (!("WAIT_PAY".equals(order.getOrderStatus()))) {
            throw remoteError("MOCK_ORDER_NOT_PAYABLE", "第三方订单当前不可支付", false);
        }
        LocalDateTime now = request.getPayTime() == null ? LocalDateTime.now() : request.getPayTime();
        if (order.getReservationExpireTime() != null && now.isAfter(order.getReservationExpireTime())) {
            throw remoteError("MOCK_ORDER_EXPIRED", "第三方订单预占已过期，请先释放预占", false);
        }
        if (request.getPayAmount() == null || order.getTotalAmount().compareTo(request.getPayAmount()) != 0) {
            throw remoteError("MOCK_PAYMENT_AMOUNT_MISMATCH", "支付金额与第三方订单不一致", false);
        }
        int rows = mapper.markMockOrderPaid(
                normalizedOrderId,
                request.getIdempotencyKey().trim(),
                now,
                nextDataVersion("order-paid")
        );
        if (rows != 1) {
            throw remoteError("MOCK_PAYMENT_STATE_CONFLICT", "第三方支付状态冲突", true);
        }
        return toProviderOrder(mapper.selectOrderByProviderOrderId(normalizedOrderId));
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketSourceProviderOrder cancelOrder(
            String providerOrderId,
            TicketSourceCancelOrderRequest request
    ) {
        applyBehavior(TicketSourceOperation.CANCEL_ORDER);
        String normalizedOrderId = normalizeRequiredId(providerOrderId, "第三方订单ID");
        if (request == null || normalizeNullable(request.getIdempotencyKey()) == null) {
            throw remoteError("MOCK_CANCEL_PARAM_INVALID", "取消订单参数不完整", false);
        }
        MockTicketSourceOrder order = mapper.selectOrderByProviderOrderId(normalizedOrderId);
        if (order == null) {
            throw notFound("MOCK_ORDER_NOT_FOUND", "模拟第三方订单不存在: " + normalizedOrderId);
        }
        if ("CANCELED".equals(order.getOrderStatus()) || "EXPIRED".equals(order.getOrderStatus())) {
            if (order.getCancelIdempotencyKey() != null
                    && !order.getCancelIdempotencyKey().equals(request.getIdempotencyKey().trim())) {
                throw remoteError("MOCK_CANCEL_IDEMPOTENCY_CONFLICT", "取消幂等键冲突", false);
            }
            return toProviderOrder(order);
        }
        if ("PAID".equals(order.getOrderStatus())) {
            throw remoteError("MOCK_PAID_ORDER_CANNOT_CANCEL", "已支付第三方订单需走退款流程", false);
        }
        LocalDateTime now = LocalDateTime.now();
        int restoreRows = mapper.restoreSkuStock(
                order.getSourceSkuId(), order.getQuantity(), nextDataVersion("order-release"));
        if (restoreRows != 1) {
            throw remoteError("MOCK_RELEASE_INVENTORY_FAILED", "第三方库存释放失败", true);
        }
        int updateRows = mapper.markMockOrderCanceled(
                normalizedOrderId,
                request.getIdempotencyKey().trim(),
                now,
                normalizeNullable(request.getReason()),
                "CANCELED",
                nextDataVersion("order-canceled")
        );
        if (updateRows != 1) {
            throw remoteError("MOCK_CANCEL_STATE_CONFLICT", "第三方取消状态冲突", true);
        }
        return toProviderOrder(mapper.selectOrderByProviderOrderId(normalizedOrderId));
    }

    public TicketSourceProviderOrder getOrder(String providerOrderId) {
        applyBehavior(TicketSourceOperation.GET_ORDER);
        MockTicketSourceOrder order = mapper.selectOrderByProviderOrderId(
                normalizeRequiredId(providerOrderId, "第三方订单ID"));
        if (order == null) {
            throw notFound("MOCK_ORDER_NOT_FOUND", "模拟第三方订单不存在: " + providerOrderId);
        }
        return toProviderOrder(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketSourceDelivery requestTickets(String providerOrderId, TicketSourceIssueRequest request) {
        applyBehavior(TicketSourceOperation.REQUEST_TICKETS);
        String normalizedOrderId = normalizeRequiredId(providerOrderId, "第三方订单ID");
        if (request == null || normalizeNullable(request.getIdempotencyKey()) == null
                || request.getExpectedTicketCount() == null || request.getExpectedTicketCount() <= 0) {
            throw remoteError("MOCK_ISSUE_PARAM_INVALID", "出票请求参数不完整", false);
        }
        MockTicketSourceOrder order = requirePaidOrder(normalizedOrderId);
        if (!order.getQuantity().equals(request.getExpectedTicketCount())) {
            throw remoteError("MOCK_ISSUE_COUNT_MISMATCH", "出票数量与第三方订单不一致", false);
        }
        MockTicketSourceDelivery delivery = mapper.selectDelivery(normalizedOrderId);
        if (delivery == null) {
            delivery = newDefaultDelivery(order);
            mapper.insertDefaultDelivery(delivery);
        }
        if (delivery.getRequestIdempotencyKey() != null
                && !delivery.getRequestIdempotencyKey().equals(request.getIdempotencyKey().trim())) {
            throw remoteError("MOCK_ISSUE_IDEMPOTENCY_CONFLICT", "出票幂等键冲突", false);
        }
        if (mapper.bindDeliveryRequest(normalizedOrderId, request.getIdempotencyKey().trim(),
                nextDataVersion("issue-request")) != 1) {
            throw remoteError("MOCK_ISSUE_REQUEST_CONFLICT", "第三方出票请求状态冲突", true);
        }
        return materializeDelivery(normalizedOrderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketSourceDelivery getTickets(String providerOrderId) {
        applyBehavior(TicketSourceOperation.GET_TICKETS);
        String normalizedOrderId = normalizeRequiredId(providerOrderId, "第三方订单ID");
        requireOrderWithTickets(normalizedOrderId);
        if (mapper.selectDelivery(normalizedOrderId) == null) {
            throw notFound("MOCK_DELIVERY_NOT_FOUND", "第三方订单尚未发起出票");
        }
        return materializeDelivery(normalizedOrderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketSourceRefund requestRefund(String providerOrderId, TicketSourceRefundRequest request) {
        applyBehavior(TicketSourceOperation.REQUEST_REFUND);
        String normalizedOrderId = normalizeRequiredId(providerOrderId, "第三方订单ID");
        if (request == null || normalizeNullable(request.getClientRefundNo()) == null
                || normalizeNullable(request.getIdempotencyKey()) == null
                || request.getRefundAmount() == null || request.getRefundAmount().signum() <= 0) {
            throw remoteError("MOCK_REFUND_PARAM_INVALID", "退款请求参数不完整", false);
        }
        MockTicketSourceRefund existing = mapper.selectRefundByIdempotencyKey(request.getIdempotencyKey().trim());
        if (existing != null) {
            validateIdempotentRefund(existing, normalizedOrderId, request);
            return toRefund(existing);
        }
        MockTicketSourceOrder order = mapper.selectOrderByProviderOrderId(normalizedOrderId);
        if (order == null) throw notFound("MOCK_ORDER_NOT_FOUND", "模拟第三方订单不存在: " + normalizedOrderId);
        MockTicketSourceRefund existingOrderRefund = mapper.selectRefundByProviderOrderId(normalizedOrderId);
        if (existingOrderRefund != null) {
            throw remoteError("MOCK_REFUND_ALREADY_EXISTS", "该第三方订单已存在退款记录，请使用原幂等键查询", false);
        }
        if (!"PAID".equals(order.getOrderStatus())) {
            throw remoteError("MOCK_ORDER_NOT_REFUNDABLE", "第三方订单当前不可退款", false);
        }
        if (request.getRefundAmount().compareTo(order.getTotalAmount()) > 0) {
            throw remoteError("MOCK_REFUND_AMOUNT_INVALID", "退款金额不能超过第三方订单金额", false);
        }
        MockTicketSourceRefundPlan plan = mapper.selectRefundPlan(normalizedOrderId);
        String mode = plan == null ? "IMMEDIATE" : plan.getRefundMode();
        LocalDateTime now = LocalDateTime.now();
        MockTicketSourceRefund refund = new MockTicketSourceRefund();
        refund.setProviderRefundId(generateProviderRefundId());
        refund.setProviderRefundNo(generateProviderRefundNo());
        refund.setProviderOrderId(normalizedOrderId);
        refund.setClientRefundNo(request.getClientRefundNo().trim());
        refund.setRefundStatus("REJECT".equals(mode) ? "FAILED" : "PROCESSING");
        refund.setRefundMode(mode);
        refund.setRefundAmount(request.getRefundAmount().setScale(2));
        refund.setFeeAmount(BigDecimal.ZERO.setScale(2));
        refund.setCurrencyCode(normalizeCurrency(request.getCurrencyCode(), order.getCurrencyCode()));
        refund.setReason(normalizeNullable(request.getReason()));
        refund.setRequestIdempotencyKey(request.getIdempotencyKey().trim());
        refund.setAvailableTime("DELAYED".equals(mode)
                ? (plan == null || plan.getAvailableTime() == null ? now.plusSeconds(30) : plan.getAvailableTime())
                : now);
        refund.setErrorCode("REJECT".equals(mode) ? "MOCK_REFUND_REJECTED" : null);
        refund.setErrorMessage("REJECT".equals(mode) ? "模拟第三方拒绝退款" : null);
        refund.setInventoryRestored(false);
        refund.setDataVersion(nextDataVersion("refund-create"));
        refund.setCreateTime(now);
        refund.setUpdateTime(now);
        mapper.insertMockRefund(refund);
        if ("REJECT".equals(mode)) {
            return toRefund(mapper.selectRefundByProviderRefundId(refund.getProviderRefundId()));
        }
        mapper.markMockOrderRefunding(normalizedOrderId, nextDataVersion("order-refunding"));
        if ("IMMEDIATE".equals(mode)) {
            completeRefund(refund.getProviderRefundId());
        }
        return toRefund(mapper.selectRefundByProviderRefundId(refund.getProviderRefundId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketSourceRefund getRefund(String providerRefundId) {
        applyBehavior(TicketSourceOperation.GET_REFUND);
        String normalizedRefundId = normalizeRequiredId(providerRefundId, "第三方退款ID");
        MockTicketSourceRefund refund = mapper.selectRefundByProviderRefundId(normalizedRefundId);
        if (refund == null) throw notFound("MOCK_REFUND_NOT_FOUND", "模拟第三方退款不存在: " + normalizedRefundId);
        boolean providerRefundDue = "PROCESSING".equals(refund.getRefundStatus())
                && refund.getAvailableTime() != null
                && !LocalDateTime.now().isBefore(refund.getAvailableTime());
        boolean inventoryRecoveryRequired = "SUCCESS".equals(refund.getRefundStatus())
                && !Boolean.TRUE.equals(refund.getInventoryRestored());
        if (providerRefundDue || inventoryRecoveryRequired) {
            completeRefund(normalizedRefundId);
            refund = mapper.selectRefundByProviderRefundId(normalizedRefundId);
        }
        return toRefund(refund);
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketSourceRefund makeRefundAvailableNow(String providerRefundId) {
        String normalizedRefundId = normalizeRequiredId(providerRefundId, "第三方退款ID");
        MockTicketSourceRefund refund = mapper.selectRefundByProviderRefundId(normalizedRefundId);
        if (refund == null) throw new BusinessException("模拟第三方退款不存在: " + normalizedRefundId);
        if ("SUCCESS".equals(refund.getRefundStatus())) {
            // 已成功但库存恢复标记缺失时，复用完成逻辑进行幂等自愈。
            completeRefund(normalizedRefundId);
            return toRefund(mapper.selectRefundByProviderRefundId(normalizedRefundId));
        }
        if (!"PROCESSING".equals(refund.getRefundStatus())) {
            throw new BusinessException("只有处理中的模拟退款可以提前完成");
        }
        mapper.makeMockRefundAvailableNow(normalizedRefundId, LocalDateTime.now(), nextDataVersion("refund-available-now"));
        return getRefund(normalizedRefundId);
    }

    @Transactional(rollbackFor = Exception.class)
    public MockTicketSourceRefundPlan configureRefundPlan(
            String providerOrderId, MockRefundPlanUpdateRequest request
    ) {
        String normalizedOrderId = normalizeRequiredId(providerOrderId, "第三方订单ID");
        MockTicketSourceOrder order = mapper.selectOrderByProviderOrderId(normalizedOrderId);
        if (order == null) throw new BusinessException("模拟第三方订单不存在: " + normalizedOrderId);
        if (request == null) throw new BusinessException("退款计划参数不能为空");
        String mode = normalizeEnum(request.getRefundMode(), "IMMEDIATE",
                List.of("IMMEDIATE", "DELAYED", "REJECT"), "refundMode");
        int delaySeconds = request.getDelaySeconds() == null ? 0 : request.getDelaySeconds();
        if (delaySeconds < 0 || delaySeconds > 86400) {
            throw new BusinessException("delaySeconds 必须在 0 到 86400 之间");
        }
        LocalDateTime availableTime = "DELAYED".equals(mode)
                ? LocalDateTime.now().plusSeconds(Math.max(1, delaySeconds))
                : LocalDateTime.now();
        mapper.upsertRefundPlan(normalizedOrderId, mode, availableTime, nextDataVersion("refund-plan"));
        return mapper.selectRefundPlan(normalizedOrderId);
    }

    private void completeRefund(String providerRefundId) {
        MockTicketSourceRefund refund = mapper.selectRefundByProviderRefundId(providerRefundId);
        if (refund == null) return;
        MockTicketSourceOrder order = mapper.selectOrderByProviderOrderId(refund.getProviderOrderId());
        if (order == null) throw notFound("MOCK_ORDER_NOT_FOUND", "退款关联订单不存在");
        LocalDateTime now = LocalDateTime.now();

        // 订单可能已在上一次超时请求中写成 REFUNDED，因此库存恢复不能依赖本次订单 UPDATE 的行数。
        mapper.markMockOrderRefunded(order.getProviderOrderId(), nextDataVersion("order-refunded"));

        if (!Boolean.TRUE.equals(refund.getInventoryRestored())) {
            // 先通过 inventory_restored=0 条件原子抢占恢复权。若后续恢复失败，事务会把标记一起回滚。
            int claimRows = mapper.markMockRefundInventoryRestored(providerRefundId);
            if (claimRows == 1 && mapper.restoreSkuStock(order.getSourceSkuId(), order.getQuantity(),
                    nextDataVersion("refund-stock-restore")) != 1) {
                throw remoteError("MOCK_REFUND_STOCK_RESTORE_FAILED", "退款库存恢复失败", true);
            }
        }

        mapper.voidCredentialsByOrderId(order.getProviderOrderId(), now, nextDataVersion("credential-void"));
        mapper.markDeliveryRefunded(order.getProviderOrderId(), nextDataVersion("delivery-refunded"));
        mapper.markMockRefundSuccess(providerRefundId, now, nextDataVersion("refund-success"));
    }

    private void validateIdempotentRefund(
            MockTicketSourceRefund existing, String providerOrderId, TicketSourceRefundRequest request
    ) {
        if (!existing.getProviderOrderId().equals(providerOrderId)
                || !existing.getClientRefundNo().equals(request.getClientRefundNo().trim())
                || existing.getRefundAmount().compareTo(request.getRefundAmount()) != 0) {
            throw remoteError("MOCK_REFUND_IDEMPOTENCY_CONFLICT", "退款幂等键对应的请求参数不一致", false);
        }
    }

    private TicketSourceRefund toRefund(MockTicketSourceRefund source) {
        TicketSourceRefund result = new TicketSourceRefund();
        result.setProviderRefundId(source.getProviderRefundId());
        result.setProviderRefundNo(source.getProviderRefundNo());
        result.setProviderOrderId(source.getProviderOrderId());
        result.setClientRefundNo(source.getClientRefundNo());
        result.setRefundStatus(source.getRefundStatus());
        result.setRefundAmount(source.getRefundAmount());
        result.setFeeAmount(source.getFeeAmount());
        result.setCurrencyCode(source.getCurrencyCode());
        result.setNextPollTime("PROCESSING".equals(source.getRefundStatus()) ? source.getAvailableTime() : null);
        result.setRefundTime(source.getRefundTime());
        result.setErrorCode(source.getErrorCode());
        result.setErrorMessage(source.getErrorMessage());
        result.setDataVersion(source.getDataVersion());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public MockTicketSourceDelivery configureIssuePlan(
            String providerOrderId, MockIssuePlanUpdateRequest request
    ) {
        String normalizedOrderId = normalizeRequiredId(providerOrderId, "第三方订单ID");
        MockTicketSourceOrder order = requirePaidOrder(normalizedOrderId);
        if (request == null) throw new BusinessException("出票计划参数不能为空");
        String issueMode = normalizeEnum(request.getIssueMode(), "IMMEDIATE",
                List.of("IMMEDIATE", "DELAYED", "PARTIAL_FAIL", "ALL_FAIL"), "issueMode");
        String seatMode = normalizeEnum(request.getSeatMode(), "PROVIDER_ASSIGNED",
                List.of("NONE", "PROVIDER_ASSIGNED"), "seatMode");
        String credentialType = normalizeEnum(request.getCredentialType(), "QR_CODE",
                List.of("QR_CODE", "BARCODE", "TEXT", "URL", "DYNAMIC_QR"), "credentialType");
        int delaySeconds = request.getDelaySeconds() == null ? 0 : request.getDelaySeconds();
        if (delaySeconds < 0 || delaySeconds > 86400) throw new BusinessException("delaySeconds 必须在 0 到 86400 之间");
        Integer failIndex = request.getFailTicketIndex();
        if ("PARTIAL_FAIL".equals(issueMode)) {
            if (failIndex == null) failIndex = 1;
            if (failIndex < 1 || failIndex > order.getQuantity()) {
                throw new BusinessException("failTicketIndex 必须在订单票数范围内");
            }
        } else {
            failIndex = null;
        }
        LocalDateTime now = LocalDateTime.now();
        MockTicketSourceDelivery plan = new MockTicketSourceDelivery();
        plan.setProviderOrderId(normalizedOrderId);
        plan.setDeliveryStatus("PENDING");
        plan.setIssueMode(issueMode);
        plan.setSeatMode(seatMode);
        plan.setCredentialType(credentialType);
        plan.setFailTicketIndex(failIndex);
        plan.setAvailableTime("DELAYED".equals(issueMode) ? now.plusSeconds(Math.max(1, delaySeconds)) : now);
        plan.setExpectedTicketCount(order.getQuantity());
        plan.setDataVersion(nextDataVersion("issue-plan"));
        plan.setCreateTime(now);
        plan.setUpdateTime(now);
        mapper.upsertDeliveryPlan(plan);
        return mapper.selectDelivery(normalizedOrderId);
    }

    private MockTicketSourceOrder requirePaidOrder(String providerOrderId) {
        MockTicketSourceOrder order = mapper.selectOrderByProviderOrderId(providerOrderId);
        if (order == null) throw notFound("MOCK_ORDER_NOT_FOUND", "模拟第三方订单不存在: " + providerOrderId);
        if (!"PAID".equals(order.getOrderStatus())) {
            throw remoteError("MOCK_ORDER_NOT_PAID", "只有已支付第三方订单可以出票", false);
        }
        return order;
    }

    private MockTicketSourceOrder requireOrderWithTickets(String providerOrderId) {
        MockTicketSourceOrder order = mapper.selectOrderByProviderOrderId(providerOrderId);
        if (order == null) throw notFound("MOCK_ORDER_NOT_FOUND", "模拟第三方订单不存在: " + providerOrderId);
        if (!List.of("PAID", "REFUNDING", "REFUNDED").contains(order.getOrderStatus())) {
            throw remoteError("MOCK_ORDER_HAS_NO_TICKETS", "第三方订单当前没有可查询的凭证", false);
        }
        return order;
    }

    private MockTicketSourceDelivery newDefaultDelivery(MockTicketSourceOrder order) {
        LocalDateTime now = LocalDateTime.now();
        MockTicketSourceDelivery delivery = new MockTicketSourceDelivery();
        delivery.setProviderOrderId(order.getProviderOrderId());
        delivery.setDeliveryStatus("PENDING");
        delivery.setIssueMode("IMMEDIATE");
        delivery.setSeatMode("PROVIDER_ASSIGNED");
        delivery.setCredentialType("QR_CODE");
        delivery.setAvailableTime(now);
        delivery.setExpectedTicketCount(order.getQuantity());
        delivery.setDataVersion(nextDataVersion("delivery"));
        delivery.setCreateTime(now);
        delivery.setUpdateTime(now);
        return delivery;
    }

    private TicketSourceDelivery materializeDelivery(String providerOrderId) {
        MockTicketSourceDelivery delivery = mapper.selectDelivery(providerOrderId);
        if (delivery == null) throw notFound("MOCK_DELIVERY_NOT_FOUND", "第三方出票批次不存在");
        List<MockTicketSourceCredential> existing = mapper.selectCredentials(providerOrderId);
        if (List.of("ISSUED", "PARTIAL", "FAILED", "REFUNDED").contains(delivery.getDeliveryStatus())
                && existing.size() == delivery.getExpectedTicketCount()) {
            return toDelivery(delivery, existing);
        }
        LocalDateTime now = LocalDateTime.now();
        if ("DELAYED".equals(delivery.getIssueMode())
                && delivery.getAvailableTime() != null && now.isBefore(delivery.getAvailableTime())) {
            mapper.updateDeliveryResult(providerOrderId, "PENDING", 0, 0, null, null,
                    nextDataVersion("delivery-pending"));
            return toDelivery(mapper.selectDelivery(providerOrderId), mapper.selectCredentials(providerOrderId));
        }

        int issued = 0;
        int failed = 0;
        for (int index = 1; index <= delivery.getExpectedTicketCount(); index++) {
            boolean fail = "ALL_FAIL".equals(delivery.getIssueMode())
                    || ("PARTIAL_FAIL".equals(delivery.getIssueMode())
                    && Integer.valueOf(index).equals(delivery.getFailTicketIndex()));
            MockTicketSourceCredential credential = buildCredential(delivery, index, fail, now);
            mapper.upsertCredential(credential);
            if (fail) failed++; else issued++;
        }
        String status = failed == 0 ? "ISSUED" : issued == 0 ? "FAILED" : "PARTIAL";
        String errorCode = failed == 0 ? null : "MOCK_TICKET_ISSUE_FAILED";
        String errorMessage = failed == 0 ? null : "模拟第三方单票出票失败";
        mapper.updateDeliveryResult(providerOrderId, status, issued, failed, errorCode, errorMessage,
                nextDataVersion("delivery-result"));
        return toDelivery(mapper.selectDelivery(providerOrderId), mapper.selectCredentials(providerOrderId));
    }

    private MockTicketSourceCredential buildCredential(
            MockTicketSourceDelivery delivery, int index, boolean fail, LocalDateTime now
    ) {
        String version = nextDataVersion("ticket-" + index);
        MockTicketSourceCredential c = new MockTicketSourceCredential();
        c.setProviderTicketId("MOCK-TICKET-" + delivery.getProviderOrderId() + "-" + index);
        c.setProviderOrderId(delivery.getProviderOrderId());
        c.setTicketIndex(index);
        c.setTicketStatus(fail ? "FAILED" : "ISSUED");
        c.setCredentialType(fail ? null : delivery.getCredentialType());
        c.setCredentialPayload(fail ? null : buildCredentialPayload(delivery, index, version));
        c.setCredentialVersion(fail ? null : version);
        if (!fail && "PROVIDER_ASSIGNED".equals(delivery.getSeatMode())) {
            c.setSeatZone("A区");
            c.setSeatRow(String.valueOf(8 + index));
            c.setSeatNumber(String.valueOf(15 + index));
            c.setEntranceInfo("东门电子票通道");
        }
        c.setIssueTime(fail ? null : now);
        c.setErrorCode(fail ? "MOCK_SINGLE_TICKET_FAILED" : null);
        c.setErrorMessage(fail ? "第 " + index + " 张票模拟出票失败" : null);
        c.setDataVersion(version);
        c.setCreateTime(now);
        c.setUpdateTime(now);
        return c;
    }

    private String buildCredentialPayload(MockTicketSourceDelivery delivery, int index, String version) {
        if ("URL".equals(delivery.getCredentialType())) {
            return "https://mock-ticket.local/credential/" + delivery.getProviderOrderId() + "/" + index;
        }
        if ("TEXT".equals(delivery.getCredentialType())) {
            return "MOCK-CODE-" + delivery.getProviderOrderId() + "-" + index;
        }
        return "MOCK:" + delivery.getCredentialType() + ":" + delivery.getProviderOrderId()
                + ":" + index + ":" + version;
    }

    private TicketSourceDelivery toDelivery(
            MockTicketSourceDelivery source, List<MockTicketSourceCredential> credentials
    ) {
        TicketSourceDelivery result = new TicketSourceDelivery();
        result.setProviderOrderId(source.getProviderOrderId());
        result.setDeliveryStatus(source.getDeliveryStatus());
        result.setExpectedTicketCount(source.getExpectedTicketCount());
        result.setIssuedCount(source.getIssuedCount());
        result.setFailedCount(source.getFailedCount());
        if ("PENDING".equals(source.getDeliveryStatus())) result.setNextPollTime(source.getAvailableTime());
        result.setDataVersion(source.getDataVersion());
        result.setTickets(credentials.stream().map(this::toCredential).toList());
        return result;
    }

    private TicketSourceCredential toCredential(MockTicketSourceCredential source) {
        TicketSourceCredential result = new TicketSourceCredential();
        result.setProviderTicketId(source.getProviderTicketId());
        result.setTicketIndex(source.getTicketIndex());
        result.setTicketStatus(source.getTicketStatus());
        result.setCredentialType(source.getCredentialType());
        result.setCredentialPayload(source.getCredentialPayload());
        result.setCredentialVersion(source.getCredentialVersion());
        result.setSeatZone(source.getSeatZone());
        result.setSeatRow(source.getSeatRow());
        result.setSeatNumber(source.getSeatNumber());
        result.setEntranceInfo(source.getEntranceInfo());
        result.setIssueTime(source.getIssueTime());
        result.setExpireTime(source.getExpireTime());
        result.setErrorCode(source.getErrorCode());
        result.setErrorMessage(source.getErrorMessage());
        result.setDataVersion(source.getDataVersion());
        return result;
    }

    private String normalizeEnum(String value, String defaultValue, List<String> allowed, String field) {
        String normalized = normalizeNullable(value);
        if (normalized == null) return defaultValue;
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) throw new BusinessException(field + " 不支持: " + value);
        return normalized;
    }

    public List<MockTicketSourceBehavior> listBehaviors() { return mapper.selectBehaviors(); }

    @Transactional
    public MockTicketSourceBehavior updateBehavior(String operationCode, MockBehaviorUpdateRequest request) {
        TicketSourceOperation operation = parseOperation(operationCode);
        if (request == null) throw new BusinessException("模拟行为参数不能为空");
        int delayMs = request.getDelayMs() == null ? 0 : request.getDelayMs();
        if (delayMs < 0 || delayMs > 60000) throw new BusinessException("delayMs 必须在 0 到 60000 之间");
        boolean enabled = Boolean.TRUE.equals(request.getEnabled());
        String errorCode = normalizeNullable(request.getForcedErrorCode());
        String errorMessage = normalizeNullable(request.getForcedErrorMessage());
        if (errorCode == null) errorMessage = null;
        else if (errorMessage == null) errorMessage = "本地模拟器强制返回错误";
        int updated = mapper.updateBehavior(operation.name(), enabled, delayMs, errorCode, errorMessage);
        if (updated != 1) throw new BusinessException("模拟行为配置不存在: " + operation.name());
        return mapper.selectBehavior(operation.name());
    }

    @Transactional
    public List<MockTicketSourceBehavior> resetBehaviors() {
        mapper.resetBehaviors();
        return mapper.selectBehaviors();
    }

    @Transactional
    public TicketSourceProject updateProjectSaleStatus(String providerProjectId, MockSaleStatusUpdateRequest request) {
        String saleStatus = parseSaleStatus(request == null ? null : request.getSaleStatus());
        int updated = mapper.updateProjectSaleStatus(
                normalizeRequiredId(providerProjectId, "第三方项目ID"), saleStatus, nextDataVersion("project"));
        if (updated != 1) throw new BusinessException("模拟票源项目不存在: " + providerProjectId);
        return mapper.selectProjectById(providerProjectId.trim());
    }

    @Transactional
    public TicketSourceSession updateSessionSaleStatus(String providerSessionId, MockSaleStatusUpdateRequest request) {
        String normalizedSessionId = normalizeRequiredId(providerSessionId, "第三方场次ID");
        String saleStatus = parseSaleStatus(request == null ? null : request.getSaleStatus());
        int updated = mapper.updateSessionSaleStatus(normalizedSessionId, saleStatus, nextDataVersion("session"));
        if (updated != 1) throw new BusinessException("模拟票源场次不存在: " + providerSessionId);
        return mapper.selectSessionById(normalizedSessionId);
    }

    @Transactional
    public TicketSourceInventory updateInventory(String providerSkuId, MockInventoryUpdateRequest request) {
        if (request == null) throw new BusinessException("库存更新参数不能为空");
        String normalizedSkuId = normalizeRequiredId(providerSkuId, "第三方票档ID");
        String inventoryMode = parseInventoryMode(request.getInventoryMode());
        String saleStatus = parseSaleStatus(request.getSaleStatus());
        Integer availableStock = request.getAvailableStock();
        if (availableStock != null && availableStock < 0) throw new BusinessException("availableStock 不能小于 0");
        int updated = mapper.updateSkuInventory(
                normalizedSkuId, inventoryMode, availableStock, saleStatus, nextDataVersion("sku"));
        if (updated != 1) throw new BusinessException("模拟票源票档不存在: " + providerSkuId);
        return mapper.selectInventoryBySkuId(normalizedSkuId);
    }

    private void validateCreateOrderRequest(TicketSourceCreateOrderRequest request) {
        if (request == null) throw remoteError("MOCK_ORDER_PARAM_INVALID", "订单参数不能为空", false);
        normalizeRequiredId(request.getClientOrderNo(), "本地订单号");
        normalizeRequiredId(request.getProviderProjectId(), "第三方项目ID");
        normalizeRequiredId(request.getProviderSessionId(), "第三方场次ID");
        normalizeRequiredId(request.getProviderSkuId(), "第三方票档ID");
        normalizeRequiredId(request.getIdempotencyKey(), "创建订单幂等键");
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw remoteError("MOCK_QUANTITY_INVALID", "购票数量必须大于0", false);
        }
    }

    private void validateIdempotentCreate(MockTicketSourceOrder existing, TicketSourceCreateOrderRequest request) {
        if (!existing.getClientOrderNo().equals(request.getClientOrderNo().trim())
                || !existing.getSourceSkuId().equals(request.getProviderSkuId().trim())
                || !existing.getQuantity().equals(request.getQuantity())) {
            throw remoteError("MOCK_CREATE_IDEMPOTENCY_CONFLICT", "创建订单幂等键对应的请求参数不一致", false);
        }
    }

    private TicketSourceProviderOrder toProviderOrder(MockTicketSourceOrder order) {
        TicketSourceProviderOrder result = new TicketSourceProviderOrder();
        result.setProviderOrderId(order.getProviderOrderId());
        result.setProviderOrderNo(order.getProviderOrderNo());
        result.setClientOrderNo(order.getClientOrderNo());
        result.setProviderProjectId(order.getSourceProjectId());
        result.setProviderSessionId(order.getSourceSessionId());
        result.setProviderSkuId(order.getSourceSkuId());
        result.setQuantity(order.getQuantity());
        result.setUnitPrice(order.getUnitPrice());
        result.setTotalAmount(order.getTotalAmount());
        result.setCurrencyCode(order.getCurrencyCode());
        result.setOrderStatus(order.getOrderStatus());
        TicketSourceInventory inventory = mapper.selectInventoryBySkuId(order.getSourceSkuId());
        result.setRemainingStock(inventory == null ? null : inventory.getAvailableStock());
        result.setReservationExpireTime(order.getReservationExpireTime());
        result.setCreateTime(order.getCreateTime());
        result.setPayTime(order.getPayTime());
        result.setCancelTime(order.getCancelTime());
        result.setDataVersion(order.getDataVersion());
        return result;
    }

    private LocalDateTime normalizeExpireTime(LocalDateTime value, LocalDateTime now) {
        if (value == null || value.isBefore(now.plusMinutes(1))) return now.plusMinutes(15);
        return value;
    }

    private String normalizeCurrency(String requestCurrency, String skuCurrency) {
        String currency = normalizeNullable(requestCurrency);
        return currency == null ? skuCurrency : currency.toUpperCase(Locale.ROOT);
    }

    private String generateProviderOrderId() {
        return "MOCK-ORDER-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String generateProviderOrderNo() {
        return "MTS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String generateProviderRefundId() {
        return "MOCK-REFUND-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String generateProviderRefundNo() {
        return "MRF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private void applyBehavior(TicketSourceOperation operation) {
        MockTicketSourceBehavior behavior = mapper.selectBehavior(operation.name());
        if (behavior == null || !Boolean.TRUE.equals(behavior.getEnabled())) return;
        int delayMs = behavior.getDelayMs() == null ? 0 : behavior.getDelayMs();
        if (delayMs > 0) {
            try { Thread.sleep(delayMs); }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TicketSourceAdapterException(
                        TicketSourceGatewayErrorCode.TIMEOUT, "MOCK_INTERRUPTED",
                        "模拟票源调用被超时中断", true);
            }
        }
        String errorCode = normalizeNullable(behavior.getForcedErrorCode());
        if (errorCode != null) {
            throw new TicketSourceAdapterException(
                    TicketSourceGatewayErrorCode.REMOTE_ERROR,
                    errorCode,
                    normalizeNullable(behavior.getForcedErrorMessage()) == null
                            ? "本地模拟器强制返回错误" : behavior.getForcedErrorMessage(),
                    false);
        }
    }

    private TicketSourceAdapterException notFound(String code, String message) {
        return new TicketSourceAdapterException(
                TicketSourceGatewayErrorCode.REMOTE_NOT_FOUND, code, message, false);
    }

    private TicketSourceAdapterException remoteError(String code, String message, boolean retryable) {
        return new TicketSourceAdapterException(
                TicketSourceGatewayErrorCode.REMOTE_ERROR, code, message, retryable);
    }

    private String parseSaleStatus(String value) {
        if (value == null || value.isBlank()) throw new BusinessException("saleStatus 不能为空");
        try { return TicketSourceSaleStatus.valueOf(value.trim().toUpperCase(Locale.ROOT)).name(); }
        catch (IllegalArgumentException e) { throw new BusinessException("不支持的销售状态: " + value); }
    }

    private String parseInventoryMode(String value) {
        if (value == null || value.isBlank()) throw new BusinessException("inventoryMode 不能为空");
        try { return TicketSourceInventoryMode.valueOf(value.trim().toUpperCase(Locale.ROOT)).name(); }
        catch (IllegalArgumentException e) { throw new BusinessException("不支持的库存模式: " + value); }
    }

    private String normalizeRequiredId(String value, String fieldName) {
        if (value == null || value.isBlank()) throw new BusinessException(fieldName + "不能为空");
        return value.trim();
    }

    private String nextDataVersion(String prefix) { return "mock-" + prefix + "-" + System.currentTimeMillis(); }

    private TicketSourceOperation parseOperation(String operationCode) {
        if (operationCode == null || operationCode.isBlank()) throw new BusinessException("operationCode 不能为空");
        try { return TicketSourceOperation.valueOf(operationCode.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { throw new BusinessException("不支持的模拟操作: " + operationCode); }
    }

    private String normalizeNullable(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
