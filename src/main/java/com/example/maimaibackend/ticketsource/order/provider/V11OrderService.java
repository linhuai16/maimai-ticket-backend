package com.example.maimaibackend.ticketsource.order.provider;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.order.*;
import com.example.maimaibackend.mapper.OrderMapper;
import com.example.maimaibackend.mapper.ticketsource.V11OrderMapper;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceOperation;
import com.example.maimaibackend.ticketsource.provider.adapter.V11AdapterException;
import com.example.maimaibackend.ticketsource.provider.enums.*;
import com.example.maimaibackend.ticketsource.provider.model.*;
import com.example.maimaibackend.ticketsource.order.provider.model.*;
import com.example.maimaibackend.ticketsource.resource.provider.V11ResourceAdapterInvoker;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.V11ShipmentService;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class V11OrderService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final int QUOTE_EXPIRE_MINUTES = 10;
    private static final int PAY_EXPIRE_MINUTES = 15;
    private static final String FULFILLMENT_SOURCE = "TICKET_SOURCE";
    private static final int UNKNOWN_CREATE_MANUAL_REVIEW_THRESHOLD = 3;

    private final OrderMapper orderMapper;
    private final V11OrderMapper mapper;
    private final V11ResourceAdapterInvoker invoker;
    private final ObjectMapper objectMapper;
    private final V11ShipmentService shipmentService;
    private final TransactionTemplate tx;

    public V11OrderService(OrderMapper orderMapper,
                           V11OrderMapper mapper,
                           V11ResourceAdapterInvoker invoker,
                           ObjectMapper objectMapper,
                           V11ShipmentService shipmentService,
                           PlatformTransactionManager transactionManager) {
        this.orderMapper = orderMapper;
        this.mapper = mapper;
        this.invoker = invoker;
        this.objectMapper = objectMapper;
        this.shipmentService = shipmentService;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public V11OrderQuoteResult quote(V11OrderQuoteRequest request) {
        validateQuoteRequest(request);
        if (orderMapper.countUserById(request.userId()) <= 0) throw new BusinessException("用户不存在");

        List<V11OrderSkuContext> contexts = mapper.selectSkuContexts(
                request.projectId(), request.sessionId(), List.of(request.skuId()));
        if (contexts.size() != 1) throw new BusinessException("票档不存在、未映射或不属于当前场次");
        V11OrderSkuContext skuContext = contexts.get(0);
        validateContext(skuContext, request);

        List<Long> audienceIds = request.tickets().stream().map(V11TicketSelection::audienceId).toList();
        List<OrderAudienceSnapshotDTO> audiences = orderMapper.selectAudienceSnapshots(request.userId(), audienceIds);
        if (audiences.size() != audienceIds.size()) throw new BusinessException("观演人不存在或不属于当前用户");
        validateAudienceSession(request.sessionId(), audiences);

        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(skuContext.getProviderCode());
        PurchaseMode purchaseMode = enumValue(PurchaseMode.class, request.purchaseMode(), "purchaseMode");
        CredentialType ticketMode = enumValue(CredentialType.class, request.ticketMode(), "ticketMode");
        DeliveryMode deliveryMode = enumValue(DeliveryMode.class, request.deliveryMode(), "deliveryMode");
        ProviderCapabilities capabilities = call("查询第三方能力",
                () -> invoker.invoke(target, TicketSourceOperation.HEALTH, (adapter, ctx) -> adapter.capabilities(ctx)));
        validateCapabilities(capabilities, purchaseMode, ticketMode, deliveryMode);

        List<ProviderTicketProduct> providerProducts = call("查询第三方票档",
                () -> invoker.invoke(target, TicketSourceOperation.QUERY_SKUS,
                        (adapter, ctx) -> adapter.queryTicketProducts(ctx, skuContext.getProviderSessionId())));
        ProviderTicketProduct product = providerProducts.stream()
                .filter(p -> skuContext.getProviderSkuId().equals(p.ticketProductId()))
                .findFirst().orElseThrow(() -> new BusinessException("第三方票档不存在: " + skuContext.getProviderSkuId()));
        validateSkuFulfillment(product, ticketMode, deliveryMode);

        int quantity = request.tickets().size();
        validateProviderProduct(product, quantity);
        ProviderInventory inventory = call("查询第三方库存",
                () -> invoker.invoke(target, TicketSourceOperation.QUERY_INVENTORY,
                        (adapter, ctx) -> adapter.queryInventory(ctx, skuContext.getProviderSkuId())));
        validateInventory(inventory, quantity);

        Integer sessionLimit = skuContext.getLimitPerOrder();
        if (sessionLimit != null && sessionLimit > 0 && quantity > sessionLimit) {
            throw new BusinessException("购买数量超过场次限购");
        }

        BigDecimal faceUnitPrice = money(product.facePrice(), product.salePrice());
        BigDecimal saleUnitPrice = money(product.salePrice(), null);
        BigDecimal settlementUnitPrice = money(product.settlementPrice(), product.salePrice());
        boolean fixedPlatformPrice = "FIXED".equalsIgnoreCase(skuContext.getPriceMode());
        BigDecimal platformUnitPrice = fixedPlatformPrice ? skuContext.getLocalUnitPrice() : saleUnitPrice;
        if (platformUnitPrice == null || platformUnitPrice.signum() <= 0) {
            throw new BusinessException("麦麦平台售价未配置");
        }
        BigDecimal faceAmount = faceUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2);
        BigDecimal providerTicketAmount = saleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2);
        BigDecimal ticketAmount = platformUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2);
        BigDecimal settlementAmount = settlementUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2);
        V11OrderQuoteItem quoteItem = new V11OrderQuoteItem(
                skuContext.getSkuId(), skuContext.getSkuName(), skuContext.getProviderSkuId(), quantity,
                faceUnitPrice, saleUnitPrice, settlementUnitPrice, ticketAmount,
                request.tickets(), product.version());

        List<ProviderPromotionRule> rules = queryPromotions(target, skuContext.getProviderProjectId());
        PromotionEvaluation promotion = evaluatePromotions(rules, skuContext, List.of(quoteItem), providerTicketAmount);
        ProviderDeliveryQuote deliveryQuote = null;
        BigDecimal deliveryFee = ZERO;
        if (deliveryMode == DeliveryMode.EXPRESS) {
            if (request.addressId() == null) throw new BusinessException("快递票必须选择收货地址");
            OrderAddressSnapshotDTO address = orderMapper.selectAddressSnapshot(request.userId(), request.addressId());
            if (address == null) throw new BusinessException("收货地址不存在或不属于当前用户");
            ProviderAddress providerAddress = toProviderAddress(address);
            deliveryQuote = call("试算第三方运费", () -> invoker.invoke(target, TicketSourceOperation.QUOTE_DELIVERY,
                    (adapter, ctx) -> adapter.quoteDelivery(ctx,
                            new ProviderDeliveryQuoteRequest(skuContext.getProviderProjectId(), skuContext.getProviderSessionId(),
                                    List.of(new ProviderDeliveryQuoteItem(skuContext.getProviderSkuId(), quantity)),
                                    providerAddress))));
            if (!deliveryQuote.deliveryAvailable()) {
                throw new BusinessException("当前地址不支持配送: " + deliveryQuote.unavailableReason());
            }
            deliveryFee = deliveryQuote.deliveryFee().toMajor();
        }

        BigDecimal providerDiscount = promotion.discountAmount();
        BigDecimal providerPayAmount = providerTicketAmount.subtract(providerDiscount).add(deliveryFee).setScale(2);
        BigDecimal discount = fixedPlatformPrice ? ZERO : providerDiscount;
        BigDecimal serviceFee = ZERO;
        BigDecimal payAmount = ticketAmount.subtract(discount).add(deliveryFee).add(serviceFee).setScale(2);
        if (payAmount.signum() < 0 || providerPayAmount.signum() < 0) throw new BusinessException("第三方计价结果异常");
        String quoteId = "V11-B6-Q-" + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(QUOTE_EXPIRE_MINUTES);
        if (deliveryQuote != null && deliveryQuote.expiresAt() != null) {
            LocalDateTime deliveryExpire = local(deliveryQuote.expiresAt());
            if (deliveryExpire.isBefore(expireTime)) expireTime = deliveryExpire;
        }

        V11OrderQuoteRecord record = new V11OrderQuoteRecord();
        record.setQuoteId(quoteId);
        record.setUserId(request.userId());
        record.setProviderId(skuContext.getProviderId());
        record.setProviderCode(skuContext.getProviderCode());
        record.setProjectId(request.projectId());
        record.setSessionId(request.sessionId());
        record.setProviderProjectId(skuContext.getProviderProjectId());
        record.setProviderSessionId(skuContext.getProviderSessionId());
        record.setPurchaseMode(purchaseMode.name());
        record.setTicketMode(ticketMode.name());
        record.setDeliveryMode(deliveryMode.name());
        record.setAddressId(request.addressId());
        record.setFaceAmount(faceAmount);
        record.setTicketAmount(ticketAmount);
        record.setProviderTicketAmount(providerTicketAmount);
        record.setProviderDiscountAmount(providerDiscount);
        record.setProviderPayAmount(providerPayAmount);
        record.setSettlementAmount(settlementAmount);
        record.setDiscountAmount(discount);
        record.setDeliveryFeeAmount(deliveryFee);
        record.setServiceFeeAmount(serviceFee);
        record.setPayAmount(payAmount);
        record.setProviderDeliveryQuoteId(deliveryQuote == null ? null : deliveryQuote.quoteId());
        record.setRequestSnapshot(json(request));
        record.setItemsSnapshot(json(quoteItem));
        record.setPromotionSnapshot(json(promotion.applied()));
        record.setExpireTime(expireTime);
        if (mapper.insertQuote(record) != 1) throw new BusinessException("保存服务端计价单失败");

        List<String> warnings = new ArrayList<>();
        if (rules.isEmpty()) warnings.add("当前票源未提供可计算的交易优惠，最终价格仍会在创建第三方订单时再次确认");
        return new V11OrderQuoteResult(quoteId, request.userId(), request.projectId(), request.sessionId(),
                skuContext.getProviderCode(), skuContext.getProviderProjectId(), skuContext.getProviderSessionId(),
                record.getPurchaseMode(), record.getTicketMode(), record.getDeliveryMode(), request.addressId(),
                quoteItem, fixedPlatformPrice ? List.of() : promotion.applied(), quantity, faceAmount, ticketAmount, settlementAmount,
                discount, deliveryFee, serviceFee, payAmount, record.getProviderDeliveryQuoteId(), expireTime, warnings);
    }

    public V11OrderCreateResult create(V11OrderCreateRequest request) {
        if (request == null || request.userId() == null || request.userId() <= 0 || blank(request.quoteId())) {
            throw new BusinessException("userId和quoteId不能为空");
        }
        PreparedCreation prepared = tx.execute(status -> prepareLocalOrder(request));
        if (prepared == null) throw new BusinessException("创建本地订单失败");
        if (prepared.existingResult() != null) return prepared.existingResult();

        ProviderOrder providerOrder;
        try {
            providerOrder = invoker.invoke(prepared.target(), TicketSourceOperation.CREATE_ORDER,
                    (adapter, ctx) -> adapter.createOrder(ctx, prepared.providerRequest()));
        } catch (RuntimeException sourceError) {
            if (isUnknownResult(sourceError)) {
                // G2：Provider 可能已经创建订单。保留 WAIT_PAY 订单壳，绝不能取消，也绝不能再次 CREATE_ORDER。
                tx.executeWithoutResult(status -> mapper.markCreateUnknownResult(
                        prepared.order().getOrderId(), errorCode(sourceError), safeMessage(sourceError), true));
                return recoverUnknownCreate(prepared.order().getOrderId());
            }
            // G1：明确失败，沿用 R4 行为：bridge=FAILED + 本地失败订单壳取消。
            tx.executeWithoutResult(status -> {
                mapper.markCreateFailed(prepared.order().getOrderId(), errorCode(sourceError), safeMessage(sourceError), retryable(sourceError));
                mapper.cancelLocalAfterCreateFailure(prepared.order().getOrderId(), LocalDateTime.now());
            });
            throw invoker.translate("创建第三方单票档订单", sourceError);
        }
        return finalizeCreatedOrder(prepared, providerOrder);
    }

    /**
     * R5 G2 补查：只允许 FIND_ORDER，禁止再次 CREATE_ORDER。
     * UNKNOWN_RESULT 会自动补查；MANUAL_REVIEW 只能由明确的管理员恢复入口调用本方法。
     */
    public V11OrderCreateResult recoverUnknownCreate(Long orderId) {
        if (orderId == null || orderId <= 0) throw new BusinessException("orderId不能为空");
        V11LocalOrderContext current = mapper.selectOrderContextForRecovery(orderId);
        if (current == null) throw new BusinessException("待补查订单不存在");
        if ("RESERVED".equals(current.getBridgeStatus())) {
            return result(current.getOrderId(), current.getOrderNo(), current.getOrderStatus(), current.getPaymentStatus(),
                    current.getProviderCode(), current.getProviderOrderId(), current.getProviderOrderStatus(),
                    mapper.countOrderItems(orderId), mapper.countOrderTickets(orderId), current.getPayAmount(), current.getPayExpireTime());
        }
        if (!("UNKNOWN_RESULT".equals(current.getBridgeStatus()) || "MANUAL_REVIEW".equals(current.getBridgeStatus()))) {
            throw new BusinessException("当前订单不是创建结果待补查状态");
        }

        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(current.getProviderCode());
        ProviderOrder providerOrder;
        try {
            providerOrder = invoker.invoke(target, TicketSourceOperation.FIND_ORDER,
                    (adapter, ctx) -> adapter.findOrder(ctx,
                            new ProviderOrderLookupRequest(current.getOrderNo(), current.getCreateIdempotencyKey())));
        } catch (RuntimeException lookupError) {
            tx.executeWithoutResult(status -> mapper.markCreateRecoveryFailure(orderId,
                    errorCode(lookupError), safeMessage(lookupError), retryable(lookupError),
                    UNKNOWN_CREATE_MANUAL_REVIEW_THRESHOLD));
            V11LocalOrderContext after = mapper.selectOrderContextForRecovery(orderId);
            String recoveryStatus = after == null ? "UNKNOWN_RESULT" : after.getBridgeStatus();
            if ("MANUAL_REVIEW".equals(recoveryStatus)) {
                mapper.markRecoveredSubmitIdempotencyManualReview(orderId, errorCode(lookupError), safeMessage(lookupError));
            }
            throw new V11CreateUnknownResultException(orderId, recoveryStatus,
                    "订单提交结果正在确认，请稍后查看订单状态");
        }

        try {
            validateRecoveredProviderIdentityAndAmount(providerOrder, current);
        } catch (RuntimeException conflict) {
            markManualReviewBestEffort(orderId, "CREATE_ORDER_LOOKUP", providerOrder,
                    "PROVIDER_ORDER_IDENTITY_CONFLICT", conflict);
            mapper.markRecoveredSubmitIdempotencyManualReview(orderId,
                    "PROVIDER_ORDER_IDENTITY_CONFLICT", safeMessage(conflict));
            throw new V11CreateUnknownResultException(orderId, "MANUAL_REVIEW",
                    "订单提交结果需要人工确认，请稍后查看订单状态");
        }
        ProviderOrderStatus recoveredStatus = providerOrder.orderStatus().status();

        // Provider 已经找到订单，但自身仍处于 CREATING/UNKNOWN：结果尚未稳定，继续按 UNKNOWN_RESULT 补查。
        if (recoveredStatus == ProviderOrderStatus.CREATING || recoveredStatus == ProviderOrderStatus.UNKNOWN) {
            String code = "PROVIDER_ORDER_" + recoveredStatus.name();
            String message = "票务订单创建状态仍待确认: " + recoveredStatus.name();
            tx.executeWithoutResult(status -> mapper.markCreateRecoveryFailure(orderId, code, message, true,
                    UNKNOWN_CREATE_MANUAL_REVIEW_THRESHOLD));
            V11LocalOrderContext after = mapper.selectOrderContextForRecovery(orderId);
            String recoveryStatus = after == null ? "UNKNOWN_RESULT" : after.getBridgeStatus();
            if ("MANUAL_REVIEW".equals(recoveryStatus)) {
                mapper.markRecoveredSubmitIdempotencyManualReview(orderId, code, message);
            }
            throw new V11CreateUnknownResultException(orderId, recoveryStatus,
                    "订单提交结果正在确认，请稍后查看订单状态");
        }

        // 已明确过期/取消/失败：G2 已经“查明结果”，安全关闭本地 WAIT_PAY，不再长期占用 UNKNOWN_RESULT。
        if (recoveredStatus == ProviderOrderStatus.EXPIRED
                || recoveredStatus == ProviderOrderStatus.CANCELLED
                || recoveredStatus == ProviderOrderStatus.FAILED) {
            String bridgeStatus = recoveredStatus == ProviderOrderStatus.EXPIRED ? "EXPIRED"
                    : recoveredStatus == ProviderOrderStatus.CANCELLED ? "CANCELED" : "FAILED";
            String code = "PROVIDER_CREATE_RESULT_" + recoveredStatus.name();
            String message = "票务订单创建结果已确认，但订单未能保留: " + recoveredStatus.name();
            tx.executeWithoutResult(status -> {
                int rows = mapper.markRecoveredProviderTerminal(orderId, bridgeStatus, providerOrder.providerOrderId(),
                        providerOrder.providerOrderNo(), recoveredStatus.name(), local(providerOrder.createdAt()), json(providerOrder));
                if (rows != 1) throw new BusinessException("保存已确认的票务订单终态失败");
                mapper.cancelLocalAfterCreateFailure(orderId, LocalDateTime.now());
                mapper.markRecoveredSubmitIdempotencyFailed(orderId, code, message);
            });
            throw new BusinessException("票务订单未能保留，请重新提交");
        }

        // 本地下单阶段尚未支付；如果补查却出现 PAID/出票/退款等更深状态，属于跨系统事实矛盾，必须人工复核。
        if (recoveredStatus != ProviderOrderStatus.RESERVED) {
            BusinessException conflict = new BusinessException("补查到的票务订单状态与本地下单阶段不一致: " + recoveredStatus);
            markManualReviewBestEffort(orderId, "CREATE_ORDER_LOOKUP", providerOrder,
                    "PROVIDER_ORDER_STATUS_CONFLICT", conflict);
            mapper.markRecoveredSubmitIdempotencyManualReview(orderId,
                    "PROVIDER_ORDER_STATUS_CONFLICT", safeMessage(conflict));
            throw new V11CreateUnknownResultException(orderId, "MANUAL_REVIEW",
                    "订单提交结果需要人工确认，请稍后查看订单状态");
        }

        try {
            List<V11OrderItemBridgeInsert> bridgeItems = mapper.selectBridgeItems(current.getBridgeId());
            tx.executeWithoutResult(status -> {
                int rows = mapper.markBridgeReserved(orderId, providerOrder.providerOrderId(), providerOrder.providerOrderNo(),
                        recoveredStatus.name(), local(providerOrder.reservationExpireAt()),
                        local(providerOrder.createdAt()), json(providerOrder));
                if (rows != 1) throw new BusinessException("保存补查到的票务订单桥接失败");
                updateProviderSubOrderIds(current.getBridgeId(), providerOrder.tickets());
                if (DeliveryMode.EXPRESS.name().equals(current.getDeliveryMode())) {
                    shipmentService.ensureWaitShipment(current.getBridgeId());
                }
                syncBridgeItemInventories(target, bridgeItems);
                mapper.markRecoveredSubmitIdempotencySuccess(orderId);
            });
        } catch (RuntimeException localFinalizeError) {
            markManualReviewBestEffort(orderId, "CREATE_ORDER_LOOKUP", providerOrder,
                    "LOCAL_UNKNOWN_RESULT_FINALIZE_FAILED", localFinalizeError);
            mapper.markRecoveredSubmitIdempotencyManualReview(orderId,
                    "LOCAL_UNKNOWN_RESULT_FINALIZE_FAILED", safeMessage(localFinalizeError));
            throw new V11CreateUnknownResultException(orderId, "MANUAL_REVIEW",
                    "订单提交结果需要人工确认，请稍后查看订单状态");
        }
        return result(current.getOrderId(), current.getOrderNo(), current.getOrderStatus(), current.getPaymentStatus(),
                current.getProviderCode(), providerOrder.providerOrderId(), providerOrder.orderStatus().status().name(),
                mapper.countOrderItems(orderId), mapper.countOrderTickets(orderId), current.getPayAmount(),
                local(providerOrder.reservationExpireAt()));
    }

    public V11UnknownCreateRecoveryBatchResult recoverUnknownCreates(int requestedLimit) {
        int limit = Math.max(1, Math.min(100, requestedLimit));
        List<Long> ids = mapper.selectUnknownCreateOrderIds(limit);
        List<Long> recoveredIds = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        int unresolved = 0;
        int manualReview = 0;
        for (Long orderId : ids) {
            try {
                recoverUnknownCreate(orderId);
                recoveredIds.add(orderId);
            } catch (V11CreateUnknownResultException ex) {
                if ("MANUAL_REVIEW".equals(ex.getRecoveryStatus())) manualReview++;
                else unresolved++;
                failures.add(orderId + ": " + ex.getRecoveryStatus());
            } catch (RuntimeException ex) {
                unresolved++;
                failures.add(orderId + ": " + safeMessage(ex));
            }
        }
        return new V11UnknownCreateRecoveryBatchResult(ids.size(), recoveredIds.size(), unresolved,
                manualReview, recoveredIds, failures);
    }

    private V11OrderCreateResult finalizeCreatedOrder(PreparedCreation prepared, ProviderOrder providerOrder) {
        try {
            validateProviderOrder(providerOrder, prepared.quote());
            tx.executeWithoutResult(status -> {
                int rows = mapper.markBridgeReserved(prepared.order().getOrderId(), providerOrder.providerOrderId(),
                        providerOrder.providerOrderNo(), providerOrder.orderStatus().status().name(),
                        local(providerOrder.reservationExpireAt()), local(providerOrder.createdAt()), json(providerOrder));
                if (rows != 1) throw new BusinessException("保存第三方订单桥接失败");
                updateProviderSubOrderIds(prepared.bridge().getBridgeId(), providerOrder.tickets());
                if (DeliveryMode.EXPRESS.name().equals(prepared.quote().getDeliveryMode())) {
                    shipmentService.ensureWaitShipment(prepared.bridge().getBridgeId());
                }
                syncInventories(prepared.target(), prepared.contexts());
            });
        } catch (RuntimeException localFinalizeError) {
            compensateProviderOrder(prepared, providerOrder, localFinalizeError);
            throw localFinalizeError;
        }
        return result(prepared.order().getOrderId(), prepared.order().getOrderNo(), "WAIT_PAY", "UNPAID",
                prepared.quote().getProviderCode(), providerOrder.providerOrderId(),
                providerOrder.orderStatus().status().name(), prepared.items().size(), prepared.totalTickets(),
                prepared.order().getPayAmount(), local(providerOrder.reservationExpireAt()));
    }

    private void validateRecoveredProviderIdentityAndAmount(ProviderOrder providerOrder, V11LocalOrderContext current) {
        if (providerOrder == null || providerOrder.providerOrderId() == null || providerOrder.orderStatus() == null
                || providerOrder.orderStatus().status() == null) {
            throw new BusinessException("补查未返回有效票务订单");
        }
        if (!Objects.equals(providerOrder.clientOrderNo(), current.getOrderNo())) {
            throw new BusinessException("补查结果与本地商户订单号不一致");
        }
        if (providerOrder.price() == null || providerOrder.price().payAmount() == null
                || current.getProviderPayAmount() == null
                || providerOrder.price().payAmount().toMajor().compareTo(current.getProviderPayAmount()) != 0) {
            throw new BusinessException("补查结果金额与本地订单不一致");
        }
    }

    public V11OrderActionResult pay(Long orderId, V11OrderActionRequest request) {
        validateAction(orderId, request);
        V11LocalOrderContext order = tx.execute(status -> {
            V11LocalOrderContext current = mapper.selectOrderContextForUpdate(orderId, request.userId());
            if (current == null) throw new BusinessException("V1.2单票档订单不存在");
            if ("WAIT_USE".equals(current.getOrderStatus()) && "PROVIDER_CONFIRMED".equals(current.getPaymentStatus())) return current;
            if (!"WAIT_PAY".equals(current.getOrderStatus()) || !"RESERVED".equals(current.getBridgeStatus())) {
                throw new BusinessException("当前订单状态不允许支付");
            }
            if (current.getPayExpireTime() != null && current.getPayExpireTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException("订单已超过支付时间，请取消后重新下单");
            }
            if (mapper.markPaymentConfirming(orderId) != 1) throw new BusinessException("订单支付状态已变化");
            return current;
        });
        if ("WAIT_USE".equals(order.getOrderStatus())) return actionResult(order,  mapper.countOrderTickets(orderId), "订单已支付");

        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(order.getProviderCode());
        ProviderOrder providerOrder;
        try {
            providerOrder = invoker.invoke(target, TicketSourceOperation.CONFIRM_PAYMENT,
                    (adapter, ctx) -> adapter.confirmPayment(ctx, order.getProviderOrderId(),
                            new ProviderConfirmPaymentRequest(order.getOrderNo(), ProviderMoney.fromMajor(order.getProviderPayAmount(), "CNY"),
                                    defaultText(request.payMethod(), "WECHAT"), OffsetDateTime.now(), order.getPaymentIdempotencyKey())));
        } catch (RuntimeException sourceError) {
            tx.executeWithoutResult(status -> mapper.restoreReservedAfterPaymentFailure(orderId,
                    errorCode(sourceError), safeMessage(sourceError), retryable(sourceError)));
            throw invoker.translate("确认第三方支付", sourceError);
        }
        LocalDateTime payTime = providerOrder.paidAt() == null ? LocalDateTime.now() : local(providerOrder.paidAt());
        try {
            validateProviderPayment(providerOrder, order);
            tx.executeWithoutResult(status -> {
                if (mapper.updateOrderPaid(orderId, defaultText(request.payMethod(), "WECHAT"), payTime) != 1) {
                    throw new BusinessException("本地订单支付状态已变化");
                }
                if (mapper.markBridgePaid(orderId, providerOrder.orderStatus().status().name(), payTime, json(providerOrder)) != 1) {
                    throw new BusinessException("保存第三方支付状态失败");
                }
                mapper.insertGeneratingTickets(orderId);
                mapper.insertV12IssueTask(orderId);
            });
        } catch (RuntimeException localFinalizeError) {
            markManualReviewBestEffort(orderId, "CONFIRM_PAYMENT", providerOrder,
                    "LOCAL_PAYMENT_FINALIZE_FAILED", localFinalizeError);
            throw localFinalizeError;
        }
        V11LocalOrderContext updated = tx.execute(status -> mapper.selectOrderContextForUpdate(orderId, request.userId()));
        return actionResult(updated, mapper.countOrderTickets(orderId),
                "支付已由第三方确认；已创建V1.2第三方履约任务");
    }

    public V11OrderActionResult cancel(Long orderId, V11OrderActionRequest request) {
        validateAction(orderId, request);
        V11LocalOrderContext order = tx.execute(status -> {
            V11LocalOrderContext current = mapper.selectOrderContextForUpdate(orderId, request.userId());
            if (current == null) throw new BusinessException("V1.2单票档订单不存在");
            if ("CANCELED".equals(current.getOrderStatus())) return current;
            if (!"WAIT_PAY".equals(current.getOrderStatus())) {
                throw new BusinessException("当前订单状态不允许取消");
            }
            boolean normalCancel = "RESERVED".equals(current.getBridgeStatus());
            boolean recoverCanceling = "CANCELING".equals(current.getBridgeStatus());
            boolean recoverManualReview = "MANUAL_REVIEW".equals(current.getBridgeStatus())
                    && "CANCEL_ORDER".equals(current.getLastOperation())
                    && ("CANCELLED".equals(current.getProviderOrderStatus())
                    || "CANCELED".equals(current.getProviderOrderStatus())
                    || "EXPIRED".equals(current.getProviderOrderStatus()));
            if (!normalCancel && !recoverCanceling && !recoverManualReview) {
                throw new BusinessException("当前订单状态不允许取消");
            }
            if (normalCancel && mapper.markCanceling(orderId) != 1) {
                throw new BusinessException("订单取消状态已变化");
            }
            return current;
        });
        if ("CANCELED".equals(order.getOrderStatus())) return actionResult(order, mapper.countOrderTickets(orderId), "订单已取消");

        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(order.getProviderCode());
        ProviderOrder providerOrder;
        try {
            providerOrder = invoker.invoke(target, TicketSourceOperation.CANCEL_ORDER,
                    (adapter, ctx) -> adapter.cancelOrder(ctx, order.getProviderOrderId(),
                            new ProviderCancelOrderRequest(order.getOrderNo(), defaultText(request.reason(), "USER_CANCEL"),
                                    order.getCancelIdempotencyKey())));
        } catch (RuntimeException sourceError) {
            tx.executeWithoutResult(status -> mapper.restoreReservedAfterCancelFailure(orderId,
                    errorCode(sourceError), safeMessage(sourceError), retryable(sourceError)));
            throw invoker.translate("取消第三方订单", sourceError);
        }
        LocalDateTime cancelTime = providerOrder.cancelledAt() == null ? LocalDateTime.now() : local(providerOrder.cancelledAt());
        List<V11OrderItemBridgeInsert> bridgeItems = mapper.selectBridgeItems(order.getBridgeId());
        try {
            validateProviderCancellation(providerOrder, order);
            tx.executeWithoutResult(status -> {
                if (mapper.updateOrderCanceled(orderId, cancelTime) != 1) {
                    throw new BusinessException("本地订单取消状态已变化");
                }
                String bridgeStatus = providerOrder.orderStatus().status() == ProviderOrderStatus.EXPIRED ? "EXPIRED" : "CANCELED";
                if (mapper.markBridgeCanceled(orderId, bridgeStatus, providerOrder.orderStatus().status().name(), cancelTime, json(providerOrder)) != 1) {
                    throw new BusinessException("保存第三方取消状态失败");
                }
                syncBridgeItemInventories(target, bridgeItems);
                shipmentService.markNotRequired(order.getBridgeId());
            });
        } catch (RuntimeException localFinalizeError) {
            markManualReviewBestEffort(orderId, "CANCEL_ORDER", providerOrder,
                    "LOCAL_CANCEL_FINALIZE_FAILED", localFinalizeError);
            throw localFinalizeError;
        }
        V11LocalOrderContext updated = tx.execute(status -> mapper.selectOrderContextForUpdate(orderId, request.userId()));
        return actionResult(updated, mapper.countOrderTickets(orderId), "第三方库存已释放并同步本地快照");
    }

    private PreparedCreation prepareLocalOrder(V11OrderCreateRequest request) {
        V11OrderQuoteRecord quote = mapper.selectQuoteForUpdate(request.quoteId(), request.userId());
        if (quote == null) throw new BusinessException("计价单不存在或不属于当前用户");
        if (quote.getUsedOrderId() != null) {
            V11LocalOrderContext existing = mapper.selectOrderContextForUpdate(quote.getUsedOrderId(), request.userId());
            if (existing == null) throw new BusinessException("计价单已使用但订单不存在，请联系管理员");
            if ("INITIATING".equals(existing.getBridgeStatus())) {
                throw new BusinessException("票务订单正在创建，请稍后使用同一quoteId重试");
            }
            if ("UNKNOWN_RESULT".equals(existing.getBridgeStatus()) || "MANUAL_REVIEW".equals(existing.getBridgeStatus())) {
                throw new V11CreateUnknownResultException(existing.getOrderId(), existing.getBridgeStatus(),
                        "订单提交结果正在确认，请稍后查看订单状态");
            }
            if ("FAILED".equals(existing.getBridgeStatus())) {
                throw new BusinessException("本次计价单创建失败，请重新计价");
            }
            return new PreparedCreation(null, null, null, null, null, 0,
                    result(existing.getOrderId(), existing.getOrderNo(), existing.getOrderStatus(), existing.getPaymentStatus(),
                            existing.getProviderCode(), existing.getProviderOrderId(), existing.getProviderOrderStatus(),
                            mapper.countOrderItems(existing.getOrderId()), mapper.countOrderTickets(existing.getOrderId()),
                            existing.getPayAmount(), existing.getPayExpireTime()));
        }
        if (quote.getExpireTime() == null || !quote.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("计价单已过期，请重新计价");
        }
        V11OrderQuoteItem item = read(quote.getItemsSnapshot(), V11OrderQuoteItem.class);
        if (item == null || item.skuId() == null || item.tickets().isEmpty()) {
            throw new BusinessException("计价单票档为空");
        }

        List<V11OrderSkuContext> contexts = mapper.selectSkuContexts(
                quote.getProjectId(), quote.getSessionId(), List.of(item.skuId()));
        if (contexts.size() != 1) throw new BusinessException("计价单中的票档映射已变化，请重新计价");
        V11OrderSkuContext skuContext = contexts.get(0);

        List<Long> audienceIds = item.tickets().stream().map(V11TicketSelection::audienceId).toList();
        List<OrderAudienceSnapshotDTO> audiences = orderMapper.selectAudienceSnapshots(request.userId(), audienceIds);
        if (audiences.size() != audienceIds.size()) throw new BusinessException("观演人信息已变化，请重新计价");
        Map<Long, OrderAudienceSnapshotDTO> audienceById = audiences.stream()
                .collect(Collectors.toMap(OrderAudienceSnapshotDTO::getAudienceId, a -> a));
        validateAudienceSession(quote.getSessionId(), audiences);

        OrderAddressSnapshotDTO address = null;
        if (DeliveryMode.EXPRESS.name().equals(quote.getDeliveryMode())) {
            address = orderMapper.selectAddressSnapshot(request.userId(), quote.getAddressId());
            if (address == null) throw new BusinessException("收货地址已失效，请重新计价");
        }

        LocalDateTime payExpire = LocalDateTime.now().plusMinutes(PAY_EXPIRE_MINUTES);
        TicketOrderInsertDTO order = new TicketOrderInsertDTO();
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.userId());
        order.setProjectId(quote.getProjectId());
        order.setSessionId(quote.getSessionId());
        order.setOrderStatus("WAIT_PAY");
        order.setDeliveryType(CredentialType.PAPER_TICKET.name().equals(quote.getTicketMode()) ? "PAPER_TICKET" : "ETICKET");
        order.setFulfillmentMode(FULFILLMENT_SOURCE);
        order.setTicketAmount(quote.getTicketAmount());
        order.setServiceFeeAmount(quote.getServiceFeeAmount());
        order.setDeliveryFeeAmount(quote.getDeliveryFeeAmount());
        order.setDiscountAmount(quote.getDiscountAmount());
        order.setTotalAmount(quote.getPayAmount());
        order.setPayAmount(quote.getPayAmount());
        order.setPaymentStatus("UNPAID");
        order.setPayExpireTime(payExpire);
        if (orderMapper.insertTicketOrder(order) != 1) throw new BusinessException("保存本地订单失败");

        OrderItemInsertDTO localItem = new OrderItemInsertDTO();
        localItem.setOrderId(order.getOrderId());
        localItem.setSkuId(item.skuId());
        localItem.setSkuName(item.skuName());
        localItem.setUnitPrice(item.subtotalAmount().divide(BigDecimal.valueOf(item.quantity()), 2, RoundingMode.HALF_UP));
        localItem.setFacePrice(item.faceUnitPrice());
        localItem.setProviderSalePrice(item.providerSaleUnitPrice());
        localItem.setSettlementPrice(item.settlementUnitPrice());
        localItem.setQuantity(item.quantity());
        localItem.setSubtotalAmount(item.subtotalAmount());
        if (orderMapper.insertOrderItem(localItem) != 1) throw new BusinessException("保存订单票档失败");

        Map<String, ProviderPerson> holders = new LinkedHashMap<>();
        List<ProviderTicketAssignmentRequest> providerTickets = new ArrayList<>();
        for (V11TicketSelection ticket : item.tickets()) {
            OrderAudienceSnapshotDTO audience = audienceById.get(ticket.audienceId());
            if (audience == null) throw new BusinessException("观演人不存在: " + ticket.audienceId());
            if (mapper.insertOrderAudience(order.getOrderId(), localItem.getOrderItemId(), ticket.clientTicketNo(), audience) != 1) {
                throw new BusinessException("保存逐票观演人关系失败");
            }
            String holderRef = holderRef(ticket.audienceId());
            holders.put(holderRef, toProviderPerson(audience));
            providerTickets.add(new ProviderTicketAssignmentRequest(ticket.clientTicketNo(), holderRef));
        }
        if (address != null) orderMapper.insertOrderAddressSnapshot(order.getOrderId(), address);

        V11OrderItemBridgeInsert bridgeItem = new V11OrderItemBridgeInsert(null, localItem.getOrderItemId(),
                skuContext.getSkuMappingId(), item.providerSkuId(), item.quantity(),
                item.providerSaleUnitPrice(), item.settlementUnitPrice());

        V11OrderBridgeInsert bridge = new V11OrderBridgeInsert();
        bridge.setOrderId(order.getOrderId());
        bridge.setProviderId(quote.getProviderId());
        bridge.setProviderProjectId(quote.getProviderProjectId());
        bridge.setProviderSessionId(quote.getProviderSessionId());
        bridge.setSkuMappingId(skuContext.getSkuMappingId());
        bridge.setProviderSkuId(item.providerSkuId());
        bridge.setQuantity(item.quantity());
        bridge.setUnitPrice(item.providerSaleUnitPrice());
        bridge.setPayAmount(quote.getProviderPayAmount());
        bridge.setCreateIdempotencyKey("V11-CREATE:" + quote.getQuoteId());
        bridge.setPaymentIdempotencyKey("V11-PAY:" + order.getOrderNo());
        bridge.setCancelIdempotencyKey("V11-CANCEL:" + order.getOrderNo());
        bridge.setReservationExpireTime(payExpire);
        bridge.setRequestSnapshot(summarySnapshot(quote, item));
        bridge.setQuoteId(quote.getQuoteId());
        if (mapper.insertBridge(bridge) != 1) throw new BusinessException("保存第三方订单桥接失败");
        bridgeItem.setBridgeId(bridge.getBridgeId());
        if (mapper.insertBridgeItem(bridgeItem) != 1) throw new BusinessException("保存第三方订单项桥接失败");
        if (mapper.markQuoteUsed(quote.getQuoteId(), order.getOrderId()) != 1) {
            throw new BusinessException("计价单已过期或已使用");
        }

        ProviderPerson buyer = toProviderPerson(audiences.get(0));
        ProviderAddress providerAddress = address == null ? null : toProviderAddress(address);
        ProviderContact contact = address == null
                ? new ProviderContact(buyer.name(), "86", buyer.phone(), null)
                : new ProviderContact(address.getReceiverName(), "86", address.getReceiverPhone(), null);
        ProviderOrderCreateRequest providerRequest = new ProviderOrderCreateRequest(
                order.getOrderNo(), quote.getProviderProjectId(), quote.getProviderSessionId(),
                item.providerSkuId(), ProviderMoney.fromMajor(item.providerSaleUnitPrice(), "CNY"),
                providerTickets, holders, buyer, contact,
                PurchaseMode.valueOf(quote.getPurchaseMode()), CredentialType.valueOf(quote.getTicketMode()),
                DeliveryMode.valueOf(quote.getDeliveryMode()), providerAddress,
                ProviderMoney.fromMajor(quote.getProviderTicketAmount(), "CNY"),
                ProviderMoney.fromMajor(quote.getDeliveryFeeAmount(), "CNY"),
                ProviderMoney.fromMajor(quote.getProviderPayAmount(), "CNY"), quote.getProviderDeliveryQuoteId(),
                offset(payExpire), bridge.getCreateIdempotencyKey());
        V11ResourceAdapterInvoker.Target target = invoker.requireEnabled(quote.getProviderCode());
        return new PreparedCreation(quote, order, bridge, List.of(bridgeItem), providerRequest,
                item.quantity(), null, target, List.of(item), List.of(skuContext));
    }

    private void validateQuoteRequest(V11OrderQuoteRequest request) {
        if (request == null || request.userId() == null || request.userId() <= 0
                || request.projectId() == null || request.projectId() <= 0
                || request.sessionId() == null || request.sessionId() <= 0
                || request.skuId() == null || request.skuId() <= 0
                || request.tickets() == null || request.tickets().isEmpty()) {
            throw new BusinessException("用户、项目、场次、票档和观演人不能为空");
        }
        enumValue(PurchaseMode.class, request.purchaseMode(), "purchaseMode");
        enumValue(CredentialType.class, request.ticketMode(), "ticketMode");
        enumValue(DeliveryMode.class, request.deliveryMode(), "deliveryMode");
        Set<Long> audiences = new HashSet<>();
        Set<String> tickets = new HashSet<>();
        for (V11TicketSelection ticket : request.tickets()) {
            if (ticket == null || blank(ticket.clientTicketNo())
                    || ticket.audienceId() == null || ticket.audienceId() <= 0) {
                throw new BusinessException("每张票必须包含clientTicketNo和audienceId");
            }
            if (!tickets.add(ticket.clientTicketNo())) throw new BusinessException("clientTicketNo不能重复");
            if (!audiences.add(ticket.audienceId())) {
                throw new BusinessException("一个观演人在同一订单中只能购买一张票");
            }
        }
    }

    private void validateContext(V11OrderSkuContext context, V11OrderQuoteRequest request) {
        if ("OFFLINE".equals(context.getProjectStatus())) throw new BusinessException("当前演出已下架");
        if (!("ON_SALE".equals(context.getSessionStatus()) || "PRESALE".equals(context.getSessionStatus()))) {
            throw new BusinessException("当前场次不可购买");
        }
        if (!request.projectId().equals(context.getProjectId())
                || !request.sessionId().equals(context.getSessionId())
                || !request.skuId().equals(context.getSkuId())) {
            throw new BusinessException("项目、场次或票档不一致");
        }
        if (!("ON_SALE".equals(context.getSkuStatus()) || "PRESALE".equals(context.getSkuStatus()))) {
            throw new BusinessException("票档不可购买: " + context.getSkuName());
        }
        if ("LOCAL_COMPAT".equals(context.getInventoryAuthority())) {
            throw new BusinessException("V1.2订单只支持第三方票源票档");
        }
    }

    private void validateCapabilities(ProviderCapabilities capabilities,
                                      PurchaseMode purchaseMode,
                                      CredentialType ticketMode,
                                      DeliveryMode deliveryMode) {
        if (capabilities == null) throw new BusinessException("第三方未返回能力声明");
        if (purchaseMode == PurchaseMode.SYSTEM_ASSIGN && !capabilities.systemSeatAssignment()) {
            throw new BusinessException("当前票源不支持系统分配座位");
        }
        if (ticketMode == CredentialType.PAPER_TICKET) {
            if (!capabilities.paperTicket()) throw new BusinessException("当前票源不支持纸质票履约");
            if (deliveryMode == DeliveryMode.EXPRESS && !capabilities.expressDelivery()) {
                throw new BusinessException("当前票源不支持纸质票快递履约");
            }
            if (deliveryMode == DeliveryMode.PAPERLESS) {
                throw new BusinessException("纸质票不能使用PAPERLESS配送模式");
            }
        } else if (deliveryMode != DeliveryMode.PAPERLESS) {
            throw new BusinessException("非纸质票只能使用PAPERLESS履约模式");
        }
        if (ticketMode == CredentialType.DYNAMIC_QR && !capabilities.dynamicQr()) {
            throw new BusinessException("当前票源不支持动态二维码");
        }
        if (ticketMode != CredentialType.PAPER_TICKET && !capabilities.electronicTicket()) {
            throw new BusinessException("当前票源不支持电子凭证");
        }
    }

    /**
     * 履约方式以 SKU 级 subStatus 为事实来源。
     * Provider 全局能力只表示“最多支持什么”，不能给某个 SKU 补它没有声明的方式。
     */
    private void validateSkuFulfillment(ProviderTicketProduct product,
                                        CredentialType ticketMode,
                                        DeliveryMode deliveryMode) {
        Set<String> tokens = fulfillmentTokens(product);
        boolean paperSku = tokens.contains("PAPER_TICKET")
                || tokens.contains("EXPRESS_SUPPORTED")
                || tokens.contains("SELF_PICKUP_SUPPORTED")
                || (product.productName() != null
                    && (product.productName().contains("纸质票")
                        || product.productName().toUpperCase(Locale.ROOT).contains("PAPER")));

        if (ticketMode == CredentialType.PAPER_TICKET) {
            if (!paperSku) throw new BusinessException("当前票档不是纸质票票档");
            if (deliveryMode == DeliveryMode.EXPRESS && !tokens.contains("EXPRESS_SUPPORTED")) {
                throw new BusinessException("当前纸质票票档不支持快递配送");
            }
            if (deliveryMode == DeliveryMode.SELF_PICKUP && !tokens.contains("SELF_PICKUP_SUPPORTED")) {
                throw new BusinessException("当前纸质票票档不支持现场取票");
            }
            if (deliveryMode == DeliveryMode.PAPERLESS) {
                throw new BusinessException("纸质票不能使用PAPERLESS履约模式");
            }
            return;
        }

        if (paperSku) {
            throw new BusinessException("当前票档为纸质票，请选择纸质票履约方式");
        }
        if (deliveryMode != DeliveryMode.PAPERLESS) {
            throw new BusinessException("电子凭证不能使用纸质票履约方式");
        }
    }

    private Set<String> fulfillmentTokens(ProviderTicketProduct product) {
        Set<String> result = new HashSet<>();
        if (product == null || product.subStatus() == null) return result;
        for (String token : product.subStatus().trim().toUpperCase(Locale.ROOT).split("[|,;\\s]+")) {
            if (!token.isBlank()) result.add(token);
        }
        return result;
    }

    private void validateProviderProduct(ProviderTicketProduct product, int quantity) {
        if (product.saleStatus().status() != TicketProductSaleStatus.ON_SALE) throw new BusinessException("第三方票档当前不可购买");
        if (product.salePrice() == null) throw new BusinessException("第三方未返回销售价格");
        if (product.maxQuantityPerOrder() != null && product.maxQuantityPerOrder() > 0 && quantity > product.maxQuantityPerOrder()) {
            throw new BusinessException("票档购买数量超过第三方限购");
        }
    }

    private void validateInventory(ProviderInventory inventory, int quantity) {
        if (inventory == null || inventory.saleStatus().status() != TicketProductSaleStatus.ON_SALE
                || inventory.stockState() == StockState.SOLD_OUT) throw new BusinessException("第三方票档已售罄或不可售");
        if (inventory.availableStock() != null && inventory.availableStock() < quantity) throw new BusinessException("第三方库存不足");
    }

    private void validateAudienceSession(Long sessionId, List<OrderAudienceSnapshotDTO> audiences) {
        List<String> hashes = new ArrayList<>();
        Set<String> unique = new HashSet<>();
        for (OrderAudienceSnapshotDTO audience : audiences) {
            if (blank(audience.getCertificateNoHash())) throw new BusinessException("观演人证件信息异常");
            if (!unique.add(audience.getCertificateNoHash())) throw new BusinessException("同一订单不能选择重复实名信息");
            hashes.add(audience.getCertificateNoHash());
        }
        if (orderMapper.countExistingAudienceBySessionAndCertHashes(sessionId, hashes) > 0) {
            throw new BusinessException("同一实名信息同场次不可重复购买");
        }
    }

    private List<ProviderPromotionRule> queryPromotions(V11ResourceAdapterInvoker.Target target, String projectId) {
        try {
            return invoker.invoke(target, TicketSourceOperation.GET_PROJECT,
                    (adapter, ctx) -> adapter.queryPromotionRules(ctx, projectId));
        } catch (V11AdapterException e) {
            if (e.getErrorCode() == com.example.maimaibackend.ticketsource.provider.adapter.V11ErrorCode.UNSUPPORTED_OPERATION) return List.of();
            throw invoker.translate("查询第三方优惠", e);
        }
    }

    private PromotionEvaluation evaluatePromotions(List<ProviderPromotionRule> rules, V11OrderSkuContext context,
                                                   List<V11OrderQuoteItem> items, BigDecimal ticketAmount) {
        BigDecimal total = ZERO;
        List<V11AppliedPromotion> applied = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        for (ProviderPromotionRule rule : rules) {
            if (rule.validFrom().isAfter(now) || rule.validTo().isBefore(now)) continue;
            if (!rule.projectIds().isEmpty() && !rule.projectIds().contains(context.getProviderProjectId())) continue;
            if (!rule.sessionIds().isEmpty() && !rule.sessionIds().contains(context.getProviderSessionId())) continue;
            if (!rule.ticketProductIds().isEmpty()
                    && items.stream().noneMatch(i -> rule.ticketProductIds().contains(i.providerSkuId()))) continue;
            BigDecimal discount = ZERO;
            if (rule.promotionType() == PromotionType.FULL_REDUCTION) {
                long thresholdMinor = number(rule.ruleData().get("thresholdAmountMinor"));
                long discountMinor = number(rule.ruleData().get("discountAmountMinor"));
                if (ProviderMoney.fromMajor(ticketAmount, "CNY").amountMinor() >= thresholdMinor) {
                    discount = ProviderMoney.cny(discountMinor).toMajor();
                }
            }
            if (discount.signum() > 0) {
                if (!rule.stackable() && !applied.isEmpty()) continue;
                total = total.add(discount).setScale(2);
                applied.add(new V11AppliedPromotion(rule.promotionId(), rule.promotionType().name(), rule.title(), discount));
                if (!rule.stackable()) break;
            }
        }
        if (total.compareTo(ticketAmount) > 0) total = ticketAmount;
        return new PromotionEvaluation(total, applied);
    }

    private void validateProviderOrder(ProviderOrder providerOrder, V11OrderQuoteRecord quote) {
        if (providerOrder == null || providerOrder.providerOrderId() == null) throw new BusinessException("第三方未返回订单号");
        if (providerOrder.price() == null || providerOrder.price().payAmount() == null
                || providerOrder.price().payAmount().toMajor().compareTo(quote.getProviderPayAmount()) != 0) {
            throw new BusinessException("第三方创建订单后的应付金额与计价单不一致");
        }
        if (providerOrder.orderStatus().status() != ProviderOrderStatus.RESERVED) {
            throw new BusinessException("第三方订单未进入预占状态: " + providerOrder.orderStatus().status());
        }
    }

    private void validateProviderPayment(ProviderOrder providerOrder, V11LocalOrderContext localOrder) {
        if (providerOrder == null || !Objects.equals(providerOrder.providerOrderId(), localOrder.getProviderOrderId())) {
            throw new BusinessException("第三方支付确认未返回正确订单");
        }
        ProviderOrderStatus status = providerOrder.orderStatus().status();
        if (!(status == ProviderOrderStatus.PAID || status == ProviderOrderStatus.ISSUING
                || status == ProviderOrderStatus.ISSUED || status == ProviderOrderStatus.PARTIALLY_ISSUED)) {
            throw new BusinessException("第三方订单未进入已支付状态: " + status);
        }
        if (providerOrder.price() == null || providerOrder.price().payAmount() == null
                || providerOrder.price().payAmount().toMajor().compareTo(localOrder.getProviderPayAmount()) != 0) {
            throw new BusinessException("第三方支付金额与本地订单不一致");
        }
    }

    private void validateProviderCancellation(ProviderOrder providerOrder, V11LocalOrderContext localOrder) {
        if (providerOrder == null || !Objects.equals(providerOrder.providerOrderId(), localOrder.getProviderOrderId())) {
            throw new BusinessException("第三方取消未返回正确订单");
        }
        ProviderOrderStatus status = providerOrder.orderStatus().status();
        if (!(status == ProviderOrderStatus.CANCELLED || status == ProviderOrderStatus.EXPIRED)) {
            throw new BusinessException("第三方订单未进入取消终态: " + status);
        }
    }

    private void markManualReviewBestEffort(Long orderId,
                                            String operation,
                                            ProviderOrder providerOrder,
                                            String errorCode,
                                            Throwable error) {
        try {
            String providerStatus = providerOrder == null || providerOrder.orderStatus() == null
                    || providerOrder.orderStatus().status() == null
                    ? null : providerOrder.orderStatus().status().name();
            tx.executeWithoutResult(status -> mapper.markBridgeManualReview(orderId, operation,
                    providerStatus, errorCode, safeMessage(error), providerOrder == null ? null : json(providerOrder)));
        } catch (RuntimeException ignored) {
            // 数据库本身不可用时无法再次落库；保留原异常交由上层和网关日志追踪。
        }
    }

    private void updateProviderSubOrderIds(Long bridgeId, List<ProviderTicketUnit> tickets) {
        // 当前订单项桥接以票档聚合；第三方子订单号在后续对账批次按票档回填。
        // 本批已经保留 provider_order_item_id 字段，不用伪造值。
    }

    private void syncInventories(V11ResourceAdapterInvoker.Target target, List<V11OrderSkuContext> contexts) {
        for (V11OrderSkuContext c : contexts) {
            ProviderInventory inventory = call("同步第三方库存", () -> invoker.invoke(target, TicketSourceOperation.QUERY_INVENTORY,
                    (adapter, ctx) -> adapter.queryInventory(ctx, c.getProviderSkuId())));
            if (inventory.availableStock() == null) continue;
            String status = inventory.saleStatus().status().name();
            mapper.updateLocalSkuInventory(c.getSkuId(), inventory.availableStock(), status);
            mapper.updateSkuMappingInventory(c.getSkuMappingId(), inventory.availableStock(), status, LocalDateTime.now());
        }
    }

    private void syncBridgeItemInventories(V11ResourceAdapterInvoker.Target target, List<V11OrderItemBridgeInsert> items) {
        for (V11OrderItemBridgeInsert item : items) {
            ProviderInventory inventory = call("同步第三方库存", () -> invoker.invoke(target, TicketSourceOperation.QUERY_INVENTORY,
                    (adapter, ctx) -> adapter.queryInventory(ctx, item.getProviderSkuId())));
            if (inventory.availableStock() == null) continue;
            Long skuId = item.getSkuId();
            if (skuId != null) mapper.updateLocalSkuInventory(skuId, inventory.availableStock(), inventory.saleStatus().status().name());
            mapper.updateSkuMappingInventory(item.getSkuMappingId(), inventory.availableStock(),
                    inventory.saleStatus().status().name(), LocalDateTime.now());
        }
    }

    private void compensateProviderOrder(PreparedCreation prepared, ProviderOrder providerOrder, RuntimeException cause) {
        try {
            invoker.invoke(prepared.target(), TicketSourceOperation.CANCEL_ORDER,
                    (adapter, ctx) -> adapter.cancelOrder(ctx, providerOrder.providerOrderId(),
                            new ProviderCancelOrderRequest(prepared.order().getOrderNo(),
                                    "LOCAL_FINALIZE_FAILED", prepared.bridge().getCancelIdempotencyKey())));
            tx.executeWithoutResult(status -> {
                mapper.markCreateFailed(prepared.order().getOrderId(), "LOCAL_FINALIZE_FAILED", safeMessage(cause), false);
                mapper.cancelLocalAfterCreateFailure(prepared.order().getOrderId(), LocalDateTime.now());
            });
        } catch (RuntimeException compensationError) {
            markManualReviewBestEffort(prepared.order().getOrderId(), "CREATE_ORDER", providerOrder,
                    "MANUAL_REVIEW_REQUIRED", compensationError);
        }
    }

    private V11OrderCreateResult result(Long orderId, String orderNo, String orderStatus, String paymentStatus,
                                        String providerCode, String providerOrderId, String providerOrderStatus,
                                        int itemCount, int ticketCount, BigDecimal payAmount, LocalDateTime expire) {
        return new V11OrderCreateResult(orderId, orderNo, orderStatus, paymentStatus, FULFILLMENT_SOURCE,
                providerCode, providerOrderId, providerOrderStatus, itemCount, ticketCount, payAmount, expire);
    }

    private V11OrderActionResult actionResult(V11LocalOrderContext order, int ticketCount, String message) {
        return new V11OrderActionResult(order.getOrderId(), order.getOrderNo(), order.getOrderStatus(),
                order.getPaymentStatus(), order.getBridgeStatus(), order.getProviderOrderId(),
                order.getProviderOrderStatus(), ticketCount, LocalDateTime.now(), message);
    }

    private ProviderPerson toProviderPerson(OrderAudienceSnapshotDTO audience) {
        return new ProviderPerson(audience.getRealName(), audience.getCertificateType(), audience.getCertificateNo(), audience.getPhone());
    }

    private ProviderAddress toProviderAddress(OrderAddressSnapshotDTO address) {
        String countryCode = requiredAddressCode(address.getCountryCode(), "countryCode");
        String provinceCode = requiredRegionCode(address.getProvinceCode(), "provinceCode");
        String cityCode = requiredRegionCode(address.getCityCode(), "cityCode");
        String areaCode = requiredRegionCode(address.getAreaCode(), "areaCode");
        return new ProviderAddress(countryCode, provinceCode, cityCode, areaCode, address.getDetailAddress());
    }

    private String requiredAddressCode(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("该收货地址信息需要更新，请重新编辑后使用");
        }
        String code = value.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z]{2}")) throw new BusinessException("该收货地址信息需要更新，请重新编辑后使用");
        return code;
    }

    private String requiredRegionCode(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("该收货地址信息需要更新，请重新编辑后使用");
        }
        String code = value.trim();
        if (!code.matches("\\d{6}")) throw new BusinessException("该收货地址信息需要更新，请重新编辑后使用");
        return code;
    }

    private void validateAction(Long orderId, V11OrderActionRequest request) {
        if (orderId == null || orderId <= 0 || request == null || request.userId() == null || request.userId() <= 0) {
            throw new BusinessException("orderId和userId不能为空");
        }
    }

    private <T> T call(String action, Supplier<T> supplier) {
        try { return supplier.get(); }
        catch (RuntimeException e) { throw invoker.translate(action, e); }
    }

    private BigDecimal money(ProviderMoney preferred, ProviderMoney fallback) {
        ProviderMoney value = preferred == null ? fallback : preferred;
        if (value == null) throw new BusinessException("第三方价格缺失");
        return value.toMajor().setScale(2, RoundingMode.HALF_UP);
    }

    private <E extends Enum<E>> E enumValue(Class<E> type, String value, String field) {
        if (blank(value)) throw new BusinessException(field + "不能为空");
        try { return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { throw new BusinessException(field + "不合法: " + value); }
    }

    private long number(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value == null) return 0;
        try { return Long.parseLong(String.valueOf(value)); }
        catch (NumberFormatException e) { return 0; }
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new BusinessException("序列化计价快照失败: " + e.getMessage()); }
    }

    private <T> T read(String value, Class<T> type) {
        try { return objectMapper.readValue(value, type); }
        catch (Exception e) { throw new BusinessException("读取计价快照失败: " + e.getMessage()); }
    }


    private String summarySnapshot(V11OrderQuoteRecord quote, V11OrderQuoteItem item) {
        Map<String,Object> summary = new LinkedHashMap<>();
        summary.put("quoteId", quote.getQuoteId());
        summary.put("projectId", quote.getProviderProjectId());
        summary.put("sessionId", quote.getProviderSessionId());
        summary.put("providerSkuId", item.providerSkuId());
        summary.put("quantity", item.quantity());
        summary.put("clientTicketNos", item.tickets().stream().map(V11TicketSelection::clientTicketNo).toList());
        summary.put("payAmount", quote.getPayAmount());
        return json(summary);
    }

    private String generateOrderNo() {
        return "V11B6" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private String holderRef(Long audienceId) { return "AUD-" + audienceId; }
    private LocalDateTime local(OffsetDateTime value) { return value == null ? null : value.toLocalDateTime(); }
    private OffsetDateTime offset(LocalDateTime value) { return value.atOffset(ZoneOffset.ofHours(8)); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String defaultText(String value, String defaultValue) { return blank(value) ? defaultValue : value.trim(); }
    private String errorCode(Throwable e) {
        if (e instanceof V11AdapterException a) return a.getSourceErrorCode() == null ? a.getErrorCode().name() : a.getSourceErrorCode();
        return "V11_ORDER_ERROR";
    }
    private boolean retryable(Throwable e) { return e instanceof V11AdapterException a && a.isRetryable(); }
    private boolean isUnknownResult(Throwable e) { return e instanceof V11AdapterException a && a.isResultUnknown(); }
    private String safeMessage(Throwable e) {
        String value = e == null ? null : e.getMessage();
        if (blank(value)) return "未知错误";
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private record PromotionEvaluation(BigDecimal discountAmount, List<V11AppliedPromotion> applied) {}

    private record PreparedCreation(V11OrderQuoteRecord quote,
                                    TicketOrderInsertDTO order,
                                    V11OrderBridgeInsert bridge,
                                    List<V11OrderItemBridgeInsert> bridgeItems,
                                    ProviderOrderCreateRequest providerRequest,
                                    int totalTickets,
                                    V11OrderCreateResult existingResult,
                                    V11ResourceAdapterInvoker.Target target,
                                    List<V11OrderQuoteItem> items,
                                    List<V11OrderSkuContext> contexts) {
        private PreparedCreation(V11OrderQuoteRecord quote, TicketOrderInsertDTO order,
                                 V11OrderBridgeInsert bridge, List<V11OrderItemBridgeInsert> bridgeItems,
                                 ProviderOrderCreateRequest providerRequest, int totalTickets,
                                 V11OrderCreateResult existingResult) {
            this(quote, order, bridge, bridgeItems, providerRequest, totalTickets, existingResult, null, List.of(), List.of());
        }
    }
}
