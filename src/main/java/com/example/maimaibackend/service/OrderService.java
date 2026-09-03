package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.order.CancelOrderBaseDTO;
import com.example.maimaibackend.dto.order.CancelOrderRequest;
import com.example.maimaibackend.dto.order.CreateOrderRequest;
import com.example.maimaibackend.dto.order.OrderAddressSnapshotDTO;
import com.example.maimaibackend.dto.order.OrderAudienceSnapshotDTO;
import com.example.maimaibackend.dto.order.OrderConfirmBaseDTO;
import com.example.maimaibackend.dto.order.OrderItemInsertDTO;
import com.example.maimaibackend.dto.order.OrderItemReleaseDTO;
import com.example.maimaibackend.dto.order.TicketOrderInsertDTO;
import com.example.maimaibackend.mapper.OrderMapper;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceOrderMapper;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceGateway;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCallResult;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCancelOrderRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCreateOrderRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceOrderAudience;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProviderOrder;
import com.example.maimaibackend.ticketsource.order.model.TicketSourceOrderBridge;
import com.example.maimaibackend.ticketsource.order.model.TicketSourceOrderSkuContext;
import com.example.maimaibackend.vo.order.CancelOrderResponse;
import com.example.maimaibackend.vo.order.CreateOrderResponse;
import com.example.maimaibackend.vo.order.SubmitOrderPageVO;
import com.example.maimaibackend.vo.performance.ServiceTagVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final int DEFAULT_PAY_EXPIRE_MINUTES = 15;
    private static final String ORDER_STATUS_WAIT_PAY = "WAIT_PAY";
    private static final String ORDER_STATUS_CANCELED = "CANCELED";
    private static final String DELIVERY_TYPE_PAPER_TICKET = "PAPER_TICKET";
    private static final String FULFILLMENT_LOCAL = "LOCAL_COMPAT";
    private static final String FULFILLMENT_SOURCE = "TICKET_SOURCE";

    private final OrderMapper orderMapper;
    private final TicketSourceOrderMapper sourceOrderMapper;
    private final TicketSourceGateway sourceGateway;
    private final TransactionTemplate transactionTemplate;

    public OrderService(
            OrderMapper orderMapper,
            TicketSourceOrderMapper sourceOrderMapper,
            TicketSourceGateway sourceGateway,
            PlatformTransactionManager transactionManager
    ) {
        this.orderMapper = orderMapper;
        this.sourceOrderMapper = sourceOrderMapper;
        this.sourceGateway = sourceGateway;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public SubmitOrderPageVO getSubmitOrderPage(Long projectId, Long sessionId, Long skuId, Integer quantity) {
        validateId(projectId, "projectId");
        validateId(sessionId, "sessionId");
        validateId(skuId, "skuId");
        validateQuantity(quantity);

        OrderConfirmBaseDTO base = orderMapper.selectOrderConfirmBase(projectId, sessionId, skuId);
        if (base == null) throw new BusinessException("演出、场次或票档不存在");
        validateProject(base);
        validateSession(base);
        refreshRealtimeInventory(base);
        validateSku(base, quantity);

        AmountResult amount = calculateAmount(base, quantity);
        SubmitOrderPageVO vo = new SubmitOrderPageVO();
        vo.setProjectId(base.getProjectId());
        vo.setSessionId(base.getSessionId());
        vo.setSkuId(base.getSkuId());
        vo.setTitle(base.getTitle());
        vo.setPosterUrl(base.getPosterUrl());
        vo.setCityName(base.getCityName());
        vo.setStationName(base.getStationName());
        vo.setStartTime(base.getStartTime());
        vo.setVenueName(base.getVenueName());
        vo.setVenueAddress(base.getVenueAddress());
        vo.setSessionStatus(base.getSessionStatus());
        vo.setSkuName(base.getSkuName());
        vo.setSkuStatus(base.getSkuStatus());
        vo.setUnitPrice(base.getUnitPrice());
        vo.setQuantity(quantity);
        vo.setRequiredAudienceCount(quantity);
        vo.setDeliveryType(base.getDeliveryType());
        vo.setLimitPerOrder(base.getLimitPerOrder());
        vo.setStockAvailable(base.getStockAvailable());
        vo.setTicketAmount(amount.getTicketAmount());
        vo.setServiceFeeAmount(amount.getServiceFeeAmount());
        vo.setDeliveryFeeAmount(amount.getDeliveryFeeAmount());
        vo.setDiscountAmount(amount.getDiscountAmount());
        vo.setTotalAmount(amount.getTotalAmount());
        vo.setPayAmount(amount.getPayAmount());
        vo.setPayExpireMinutes(DEFAULT_PAY_EXPIRE_MINUTES);
        vo.setServiceTags(loadServiceTags(projectId));
        return vo;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        validateCreateOrderRequest(request);
        PreparedOrder prepared = transactionTemplate.execute(status -> prepareOrder(request));
        if (prepared == null) throw new BusinessException("创建订单失败");
        if (!prepared.sourceOrder) return prepared.response;

        TicketSourceCreateOrderRequest sourceRequest = buildSourceCreateRequest(prepared);
        TicketSourceCallResult<TicketSourceProviderOrder> call = sourceGateway.createOrder(
                prepared.sourceContext.getProviderCode(), sourceRequest);
        if ((call == null || !call.isSuccess() || call.getData() == null)
                && call != null && call.isRetryable()) {
            // 创建订单属于结果可能不确定的操作，只能复用同一幂等键重试，禁止生成新键。
            call = sourceGateway.createOrder(prepared.sourceContext.getProviderCode(), sourceRequest);
        }
        if (call == null || !call.isSuccess() || call.getData() == null) {
            markCreateFailed(prepared.order.getOrderId(), call);
            throw new BusinessException(buildSourceError("第三方票源预占失败", call));
        }

        final TicketSourceCallResult<TicketSourceProviderOrder> finalCall = call;
        TicketSourceProviderOrder providerOrder = finalCall.getData();
        validateProviderCreateResponse(prepared, providerOrder);
        try {
            transactionTemplate.executeWithoutResult(status -> {
                int rows = sourceOrderMapper.markReserved(
                        prepared.order.getOrderId(),
                        providerOrder.getProviderOrderId(),
                        providerOrder.getProviderOrderNo(),
                        providerOrder.getOrderStatus(),
                        providerOrder.getReservationExpireTime(),
                        providerOrder.getCreateTime(),
                        sourceResponseSnapshot(finalCall, providerOrder)
                );
                if (rows != 1) throw new BusinessException("保存第三方订单映射失败");
                updateInventorySnapshot(prepared, providerOrder);
            });
        } catch (RuntimeException e) {
            compensateCreateFailure(prepared, providerOrder);
            throw e;
        }

        prepared.response.setSourceOrderStatus("RESERVED");
        prepared.response.setProviderCode(prepared.sourceContext.getProviderCode());
        prepared.response.setProviderOrderId(providerOrder.getProviderOrderId());
        if (providerOrder.getReservationExpireTime() != null) {
            prepared.response.setPayExpireTime(providerOrder.getReservationExpireTime());
        }
        return prepared.response;
    }

    public CancelOrderResponse cancelOrder(Long orderId, CancelOrderRequest request) {
        validateId(orderId, "orderId");
        if (request == null) throw new BusinessException("请求体不能为空");
        validateId(request.getUserId(), "userId");

        CancelPlan plan = transactionTemplate.execute(status -> prepareCancel(orderId, request.getUserId()));
        if (plan == null) throw new BusinessException("取消订单失败");
        if (plan.completedResponse != null) return plan.completedResponse;
        if (!plan.sourceOrder) return cancelLocalOrder(plan);

        TicketSourceCancelOrderRequest sourceRequest = new TicketSourceCancelOrderRequest();
        sourceRequest.setClientOrderNo(plan.bridge.getOrderNo());
        sourceRequest.setReason("USER_CANCEL");
        sourceRequest.setIdempotencyKey(plan.bridge.getCancelIdempotencyKey());
        TicketSourceCallResult<TicketSourceProviderOrder> call = sourceGateway.cancelOrder(
                plan.bridge.getProviderCode(), plan.bridge.getProviderOrderId(), sourceRequest);
        if (!call.isSuccess() || call.getData() == null) {
            transactionTemplate.executeWithoutResult(status -> sourceOrderMapper.restoreReservedAfterCancelFailure(
                    orderId, sourceErrorCode(call), safeMessage(call), call != null && call.isRetryable()));
            throw new BusinessException(buildSourceError("第三方票源释放预占失败", call));
        }

        TicketSourceProviderOrder providerOrder = call.getData();
        LocalDateTime cancelTime = providerOrder.getCancelTime() == null
                ? LocalDateTime.now() : providerOrder.getCancelTime();
        transactionTemplate.executeWithoutResult(status -> {
            TicketSourceOrderBridge current = sourceOrderMapper.selectBridgeByOrderIdForUpdate(orderId);
            if (current == null) throw new BusinessException("第三方订单映射不存在");
            int localRows = sourceOrderMapper.updateOrderCanceledWithoutLocalStock(orderId, cancelTime);
            if (localRows != 1 && !ORDER_STATUS_CANCELED.equals(current.getLocalOrderStatus())) {
                throw new BusinessException("更新本地取消状态失败");
            }
            String bridgeStatus = "EXPIRED".equals(providerOrder.getOrderStatus()) ? "EXPIRED" : "CANCELED";
            sourceOrderMapper.markCanceled(
                    orderId, bridgeStatus, providerOrder.getOrderStatus(), cancelTime,
                    sourceResponseSnapshot(call, providerOrder));
            updateInventorySnapshot(current, providerOrder);
        });

        CancelOrderResponse response = buildCancelResponse(orderId, cancelTime, FULFILLMENT_SOURCE);
        response.setSourceOrderStatus("EXPIRED".equals(providerOrder.getOrderStatus()) ? "EXPIRED" : "CANCELED");
        return response;
    }

    private PreparedOrder prepareOrder(CreateOrderRequest request) {
        if (orderMapper.countUserById(request.getUserId()) <= 0) throw new BusinessException("用户不存在");

        OrderConfirmBaseDTO base = orderMapper.selectOrderConfirmBaseForUpdate(
                request.getProjectId(), request.getSessionId(), request.getSkuId());
        if (base == null) throw new BusinessException("演出、场次或票档不存在");
        validateProject(base);
        validateSession(base);
        validateSku(base, request.getQuantity());
        validateDeliveryType(base.getDeliveryType(), request.getDeliveryType());

        List<Long> distinctAudienceIds = distinctIds(request.getAudienceIds());
        if (distinctAudienceIds.size() != request.getQuantity()) {
            throw new BusinessException("观演人数量必须等于购票数量，且不能重复选择同一观演人");
        }
        List<OrderAudienceSnapshotDTO> audiences = orderMapper.selectAudienceSnapshots(
                request.getUserId(), distinctAudienceIds);
        if (audiences.size() != request.getQuantity()) throw new BusinessException("观演人不存在或不属于当前用户");
        validateRepeatedAudienceForSession(base.getSessionId(), audiences);

        OrderAddressSnapshotDTO address = null;
        if (DELIVERY_TYPE_PAPER_TICKET.equals(base.getDeliveryType())) {
            validateId(request.getAddressId(), "addressId");
            address = orderMapper.selectAddressSnapshot(request.getUserId(), request.getAddressId());
            if (address == null) throw new BusinessException("收货地址不存在或不属于当前用户");
        }

        boolean sourceOrder = isSourceInventory(base.getInventoryAuthority());
        TicketSourceOrderSkuContext sourceContext = null;
        if (sourceOrder) {
            sourceContext = sourceOrderMapper.selectSkuContext(base.getSkuId());
            validateSourceContext(sourceContext, base);
        } else {
            int lockRows = orderMapper.lockSkuStock(request.getSkuId(), request.getQuantity());
            if (lockRows != 1) throw new BusinessException("当前票档库存不足");
        }

        AmountResult amount = calculateAmount(base, request.getQuantity());
        LocalDateTime payExpireTime = LocalDateTime.now().plusMinutes(DEFAULT_PAY_EXPIRE_MINUTES);
        TicketOrderInsertDTO order = new TicketOrderInsertDTO();
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.getUserId());
        order.setProjectId(base.getProjectId());
        order.setSessionId(base.getSessionId());
        order.setOrderStatus(ORDER_STATUS_WAIT_PAY);
        order.setDeliveryType(base.getDeliveryType());
        order.setFulfillmentMode(sourceOrder ? FULFILLMENT_SOURCE : FULFILLMENT_LOCAL);
        order.setTicketAmount(amount.getTicketAmount());
        order.setServiceFeeAmount(amount.getServiceFeeAmount());
        order.setDeliveryFeeAmount(amount.getDeliveryFeeAmount());
        order.setDiscountAmount(amount.getDiscountAmount());
        order.setTotalAmount(amount.getTotalAmount());
        order.setPayAmount(amount.getPayAmount());
        order.setPaymentStatus("UNPAID");
        order.setPayExpireTime(payExpireTime);
        orderMapper.insertTicketOrder(order);

        OrderItemInsertDTO orderItem = new OrderItemInsertDTO();
        orderItem.setOrderId(order.getOrderId());
        orderItem.setSkuId(base.getSkuId());
        orderItem.setSkuName(base.getSkuName());
        orderItem.setUnitPrice(base.getUnitPrice());
        orderItem.setQuantity(request.getQuantity());
        orderItem.setSubtotalAmount(amount.getTicketAmount());
        orderMapper.insertOrderItem(orderItem);
        for (OrderAudienceSnapshotDTO audience : audiences) {
            orderMapper.insertOrderAudienceSnapshot(order.getOrderId(), audience);
        }
        if (address != null) orderMapper.insertOrderAddressSnapshot(order.getOrderId(), address);

        if (sourceOrder) {
            sourceOrderMapper.insertBridge(
                    order.getOrderId(), sourceContext.getProviderId(), sourceContext.getSkuMappingId(),
                    sourceContext.getProviderProjectId(), sourceContext.getProviderSessionId(),
                    sourceContext.getProviderSkuId(), request.getQuantity(), base.getUnitPrice(),
                    amount.getPayAmount(), sourceContext.getCurrencyCode(),
                    "CREATE:" + order.getOrderNo(), "PAY:" + order.getOrderNo(),
                    "CANCEL:" + order.getOrderNo(), payExpireTime,
                    "orderNo=" + order.getOrderNo() + ",skuId=" + base.getSkuId()
                            + ",quantity=" + request.getQuantity() + ",payAmount=" + amount.getPayAmount()
            );
        }

        CreateOrderResponse response = buildCreateResponse(order);
        response.setFulfillmentMode(order.getFulfillmentMode());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setSourceOrderStatus(sourceOrder ? "INITIATING" : null);
        return new PreparedOrder(order, base, sourceContext, audiences, response, sourceOrder);
    }

    private CancelPlan prepareCancel(Long orderId, Long userId) {
        CancelOrderBaseDTO order = orderMapper.selectOrderForCancel(orderId, userId);
        if (order == null) throw new BusinessException("订单不存在或不属于当前用户");
        if (ORDER_STATUS_CANCELED.equals(order.getOrderStatus())) {
            CancelOrderResponse response = buildCancelResponse(orderId, LocalDateTime.now(), order.getFulfillmentMode());
            TicketSourceOrderBridge bridge = sourceOrderMapper.selectBridgeByOrderId(orderId);
            response.setSourceOrderStatus(bridge == null ? null : bridge.getBridgeStatus());
            return CancelPlan.completed(response);
        }
        if (!ORDER_STATUS_WAIT_PAY.equals(order.getOrderStatus())) throw new BusinessException("只有待支付订单可以取消");

        if (!FULFILLMENT_SOURCE.equals(order.getFulfillmentMode())) {
            List<OrderItemReleaseDTO> items = orderMapper.selectOrderItemsForRelease(orderId);
            if (items == null || items.isEmpty()) throw new BusinessException("订单项不存在，无法取消订单");
            return CancelPlan.local(order, items);
        }

        TicketSourceOrderBridge bridge = sourceOrderMapper.selectBridgeByOrderIdForUpdate(orderId);
        if (bridge == null) throw new BusinessException("第三方订单映射不存在");
        if ("CANCELED".equals(bridge.getBridgeStatus()) || "EXPIRED".equals(bridge.getBridgeStatus())) {
            return CancelPlan.source(order, bridge);
        }
        if (!("RESERVED".equals(bridge.getBridgeStatus()) || "CANCELING".equals(bridge.getBridgeStatus()))) {
            throw new BusinessException("第三方订单当前正在处理中，请稍后重试");
        }
        if (sourceOrderMapper.markCanceling(orderId) != 1) {
            throw new BusinessException("第三方订单状态已变化，请刷新后重试");
        }
        bridge.setBridgeStatus("CANCELING");
        return CancelPlan.source(order, bridge);
    }

    private CancelOrderResponse cancelLocalOrder(CancelPlan plan) {
        return transactionTemplate.execute(status -> {
            for (OrderItemReleaseDTO item : plan.items) {
                if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                    throw new BusinessException("订单项数据异常，无法释放库存");
                }
                if (orderMapper.releaseSkuLockedStock(item.getSkuId(), item.getQuantity()) != 1) {
                    throw new BusinessException("释放锁定库存失败，请刷新后重试");
                }
            }
            LocalDateTime cancelTime = LocalDateTime.now();
            if (orderMapper.updateOrderCanceled(plan.order.getOrderId(), cancelTime) != 1) {
                throw new BusinessException("取消订单失败，请刷新后重试");
            }
            return buildCancelResponse(plan.order.getOrderId(), cancelTime, FULFILLMENT_LOCAL);
        });
    }

    private void refreshRealtimeInventory(OrderConfirmBaseDTO base) {
        if (!"PROVIDER_REALTIME".equals(base.getInventoryAuthority())) return;
        TicketSourceOrderSkuContext context = sourceOrderMapper.selectSkuContext(base.getSkuId());
        validateSourceContext(context, base);
        TicketSourceCallResult<TicketSourceInventory> result = sourceGateway.queryInventory(
                context.getProviderCode(), context.getProviderSkuId());
        if (!result.isSuccess() || result.getData() == null || result.getData().getAvailableStock() == null) {
            throw new BusinessException(buildSourceError("第三方实时库存查询失败", result));
        }
        base.setStockAvailable(result.getData().getAvailableStock());
    }

    private TicketSourceCreateOrderRequest buildSourceCreateRequest(PreparedOrder prepared) {
        TicketSourceCreateOrderRequest request = new TicketSourceCreateOrderRequest();
        request.setClientOrderNo(prepared.order.getOrderNo());
        request.setProviderProjectId(prepared.sourceContext.getProviderProjectId());
        request.setProviderSessionId(prepared.sourceContext.getProviderSessionId());
        request.setProviderSkuId(prepared.sourceContext.getProviderSkuId());
        request.setQuantity(prepared.audiences.size());
        request.setExpectedUnitPrice(prepared.base.getUnitPrice());
        request.setPayAmount(prepared.order.getPayAmount());
        request.setCurrencyCode(prepared.sourceContext.getCurrencyCode());
        request.setReservationExpireTime(prepared.order.getPayExpireTime());
        request.setIdempotencyKey("CREATE:" + prepared.order.getOrderNo());
        List<TicketSourceOrderAudience> sourceAudiences = new ArrayList<>();
        for (OrderAudienceSnapshotDTO audience : prepared.audiences) {
            sourceAudiences.add(new TicketSourceOrderAudience(
                    audience.getRealName(), audience.getCertificateType(),
                    audience.getCertificateNo(), audience.getPhone()));
        }
        request.setAudiences(sourceAudiences);
        return request;
    }

    private void validateProviderCreateResponse(PreparedOrder prepared, TicketSourceProviderOrder providerOrder) {
        if (providerOrder.getProviderOrderId() == null || providerOrder.getProviderOrderId().trim().isEmpty()) {
            throw new BusinessException("第三方订单响应缺少 providerOrderId");
        }
        if (!"WAIT_PAY".equals(providerOrder.getOrderStatus())) {
            compensateCreateFailure(prepared, providerOrder);
            throw new BusinessException("第三方订单创建后状态异常：" + providerOrder.getOrderStatus());
        }
        if (providerOrder.getQuantity() != null
                && !providerOrder.getQuantity().equals(prepared.audiences.size())) {
            compensateCreateFailure(prepared, providerOrder);
            throw new BusinessException("第三方订单数量与本地订单不一致");
        }
        if (providerOrder.getTotalAmount() != null
                && providerOrder.getTotalAmount().compareTo(prepared.order.getPayAmount()) != 0) {
            compensateCreateFailure(prepared, providerOrder);
            throw new BusinessException("第三方订单金额与本地订单不一致");
        }
    }

    private void markCreateFailed(Long orderId, TicketSourceCallResult<?> call) {
        transactionTemplate.executeWithoutResult(status -> {
            boolean uncertain = call != null && call.isRetryable();
            if (uncertain) {
                sourceOrderMapper.markCreateUncertain(
                        orderId, sourceErrorCode(call), safeMessage(call), true,
                        sourceResponseSnapshot(call, null));
            } else {
                sourceOrderMapper.markCreateFailed(
                        orderId, sourceErrorCode(call), safeMessage(call), false,
                        sourceResponseSnapshot(call, null));
            }
            sourceOrderMapper.updateOrderCanceledWithoutLocalStock(orderId, LocalDateTime.now());
        });
    }

    private void compensateCreateFailure(PreparedOrder prepared, TicketSourceProviderOrder providerOrder) {
        TicketSourceCancelOrderRequest request = new TicketSourceCancelOrderRequest();
        request.setClientOrderNo(prepared.order.getOrderNo());
        request.setReason("LOCAL_BRIDGE_FINALIZE_FAILED");
        request.setIdempotencyKey("CANCEL:" + prepared.order.getOrderNo());
        TicketSourceCallResult<TicketSourceProviderOrder> cancel = sourceGateway.cancelOrder(
                prepared.sourceContext.getProviderCode(), providerOrder.getProviderOrderId(), request);

        transactionTemplate.executeWithoutResult(status -> {
            LocalDateTime cancelTime = LocalDateTime.now();
            if (cancel != null && cancel.isSuccess() && cancel.getData() != null) {
                TicketSourceProviderOrder canceled = cancel.getData();
                if (canceled.getCancelTime() != null) cancelTime = canceled.getCancelTime();
                sourceOrderMapper.markCreateCompensated(
                        prepared.order.getOrderId(), canceled.getProviderOrderId(), canceled.getProviderOrderNo(),
                        canceled.getOrderStatus(), cancelTime, sourceResponseSnapshot(cancel, canceled));
                updateInventorySnapshot(prepared, canceled);
            } else {
                sourceOrderMapper.markManualReview(
                        prepared.order.getOrderId(), providerOrder.getProviderOrderId(), providerOrder.getProviderOrderNo(),
                        providerOrder.getOrderStatus(), sourceErrorCode(cancel), safeMessage(cancel),
                        cancel != null && cancel.isRetryable(), sourceResponseSnapshot(cancel, providerOrder));
            }
            sourceOrderMapper.updateOrderCanceledWithoutLocalStock(prepared.order.getOrderId(), cancelTime);
        });
    }

    private void updateInventorySnapshot(PreparedOrder prepared, TicketSourceProviderOrder providerOrder) {
        sourceOrderMapper.updateProviderInventorySnapshot(
                prepared.sourceContext.getProviderId(), prepared.sourceContext.getSkuMappingId(),
                prepared.base.getSkuId(), providerOrder.getRemainingStock(), providerOrder.getDataVersion());
    }

    private void updateInventorySnapshot(TicketSourceOrderBridge bridge, TicketSourceProviderOrder providerOrder) {
        sourceOrderMapper.updateProviderInventorySnapshot(
                bridge.getProviderId(), bridge.getSkuMappingId(), bridge.getSkuId(),
                providerOrder.getRemainingStock(), providerOrder.getDataVersion());
    }

    private void validateSourceContext(TicketSourceOrderSkuContext context, OrderConfirmBaseDTO base) {
        if (context == null) throw new BusinessException("第三方票档映射不存在");
        if (!"ENABLED".equals(context.getProviderStatus())) throw new BusinessException("第三方票源未启用");
        if (!"BOUND".equals(context.getProjectMappingStatus())
                || !"BOUND".equals(context.getSessionMappingStatus())
                || !"BOUND".equals(context.getSkuMappingStatus())) {
            throw new BusinessException("第三方资源映射未完成");
        }
        if (!("ON_SALE".equals(context.getSourceSaleStatus())
                || "PRESALE".equals(context.getSourceSaleStatus()))) {
            throw new BusinessException("第三方票档当前不可购买");
        }
        if (context.getSalePrice() == null || base.getUnitPrice().compareTo(context.getSalePrice()) != 0) {
            throw new BusinessException("第三方票价与本地票价不一致，请先同步资源");
        }
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request == null) throw new BusinessException("请求体不能为空");
        validateId(request.getUserId(), "userId");
        validateId(request.getProjectId(), "projectId");
        validateId(request.getSessionId(), "sessionId");
        validateId(request.getSkuId(), "skuId");
        validateQuantity(request.getQuantity());
        if (request.getAudienceIds() == null || request.getAudienceIds().isEmpty()) {
            throw new BusinessException("请选择观演人");
        }
        if (request.getDeliveryType() == null || request.getDeliveryType().trim().isEmpty()) {
            throw new BusinessException("配送方式不能为空");
        }
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) throw new BusinessException("请求参数无效：" + name);
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) throw new BusinessException("购票数量必须大于 0");
    }

    private void validateProject(OrderConfirmBaseDTO base) {
        if ("OFFLINE".equals(base.getProjectStatus())) throw new BusinessException("当前演出已下架");
    }

    private void validateSession(OrderConfirmBaseDTO base) {
        String status = base.getSessionStatus();
        if (!("ON_SALE".equals(status) || "PRESALE".equals(status))) {
            throw new BusinessException("当前场次不可购买");
        }
    }

    private void validateSku(OrderConfirmBaseDTO base, Integer quantity) {
        String skuStatus = base.getSkuStatus();
        if (!("ON_SALE".equals(skuStatus) || "PRESALE".equals(skuStatus))) {
            throw new BusinessException("当前票档不可购买");
        }
        Integer stockAvailable = base.getStockAvailable() == null ? 0 : base.getStockAvailable();
        if (stockAvailable < quantity) throw new BusinessException("当前票档库存不足");
        Integer limitPerOrder = base.getLimitPerOrder() == null ? 0 : base.getLimitPerOrder();
        if (limitPerOrder > 0 && quantity > limitPerOrder) throw new BusinessException("购票数量超过每单限购");
    }

    private void validateDeliveryType(String sessionDeliveryType, String requestDeliveryType) {
        if (!sessionDeliveryType.equals(requestDeliveryType)) throw new BusinessException("配送方式与当前场次不一致");
    }

    private boolean isSourceInventory(String authority) {
        return authority != null && !"LOCAL_COMPAT".equals(authority);
    }

    private List<Long> distinctIds(List<Long> ids) {
        List<Long> result = new ArrayList<>();
        Set<Long> exists = new HashSet<>();
        for (Long id : ids) {
            validateId(id, "audienceId");
            if (exists.add(id)) result.add(id);
        }
        return result;
    }

    private void validateRepeatedAudienceForSession(Long sessionId, List<OrderAudienceSnapshotDTO> audiences) {
        List<String> hashes = new ArrayList<>();
        Set<String> exists = new HashSet<>();
        for (OrderAudienceSnapshotDTO audience : audiences) {
            String hash = audience.getCertificateNoHash();
            if (hash == null || hash.trim().isEmpty()) throw new BusinessException("观演人证件信息异常");
            if (!exists.add(hash)) throw new BusinessException("同一订单不能选择重复实名信息的观演人");
            hashes.add(hash);
        }
        if (orderMapper.countExistingAudienceBySessionAndCertHashes(sessionId, hashes) > 0) {
            throw new BusinessException("同一实名信息同场次不可重复购买");
        }
    }

    private AmountResult calculateAmount(OrderConfirmBaseDTO base, Integer quantity) {
        BigDecimal ticketAmount = base.getUnitPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2);
        BigDecimal serviceFeeAmount = ZERO;
        BigDecimal deliveryFeeAmount = DELIVERY_TYPE_PAPER_TICKET.equals(base.getDeliveryType())
                ? new BigDecimal("12.00") : ZERO;
        BigDecimal discountAmount = ZERO;
        BigDecimal totalAmount = ticketAmount.add(serviceFeeAmount).add(deliveryFeeAmount)
                .subtract(discountAmount).setScale(2);
        return new AmountResult(ticketAmount, serviceFeeAmount, deliveryFeeAmount,
                discountAmount, totalAmount, totalAmount);
    }

    private List<ServiceTagVO> loadServiceTags(Long projectId) {
        List<ServiceTagVO> tags = orderMapper.selectOrderServiceTags(projectId);
        List<String> refundDetailItems = orderMapper.selectOrderRefundServiceTagDetailItems(projectId);
        Map<String, ServiceTagVO> uniqueMap = new LinkedHashMap<>();
        ServiceTagVO refundTag = null;
        for (ServiceTagVO tag : tags) {
            if (tag == null || tag.getTagName() == null) continue;
            if ("条件退".equals(tag.getTagName()) || "不可退".equals(tag.getTagName())) {
                tag.setDetailItems(refundDetailItems == null ? List.of() : refundDetailItems);
                refundTag = tag;
                continue;
            }
            if (uniqueMap.size() < 4) uniqueMap.putIfAbsent(tag.getTagName(), tag);
        }
        if (refundTag != null) uniqueMap.putIfAbsent(refundTag.getTagName(), refundTag);
        return new ArrayList<>(uniqueMap.values());
    }

    private CreateOrderResponse buildCreateResponse(TicketOrderInsertDTO order) {
        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setOrderStatus(order.getOrderStatus());
        response.setPayAmount(order.getPayAmount());
        response.setPayExpireTime(order.getPayExpireTime());
        return response;
    }

    private CancelOrderResponse buildCancelResponse(Long orderId, LocalDateTime cancelTime, String fulfillmentMode) {
        CancelOrderResponse response = new CancelOrderResponse();
        response.setSuccess(true);
        response.setOrderId(orderId);
        response.setOrderStatus(ORDER_STATUS_CANCELED);
        response.setCancelTime(cancelTime);
        response.setFulfillmentMode(fulfillmentMode);
        return response;
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String buildSourceError(String prefix, TicketSourceCallResult<?> result) {
        return prefix + "：" + sourceErrorCode(result) + " - " + safeMessage(result);
    }

    private String sourceErrorCode(TicketSourceCallResult<?> result) {
        if (result == null) return "SOURCE_CALL_EMPTY";
        if (result.getProviderErrorCode() != null) return result.getProviderErrorCode();
        return result.getErrorCode() == null ? "SOURCE_UNKNOWN_ERROR" : result.getErrorCode().name();
    }

    private String safeMessage(TicketSourceCallResult<?> result) {
        return result == null || result.getMessage() == null ? "第三方票源无响应" : result.getMessage();
    }

    private String sourceResponseSnapshot(TicketSourceCallResult<?> call, TicketSourceProviderOrder order) {
        return "requestId=" + (call == null ? null : call.getRequestId())
                + ",success=" + (call != null && call.isSuccess())
                + ",providerOrderId=" + (order == null ? null : order.getProviderOrderId())
                + ",providerStatus=" + (order == null ? null : order.getOrderStatus())
                + ",remainingStock=" + (order == null ? null : order.getRemainingStock());
    }

    private static class PreparedOrder {
        private final TicketOrderInsertDTO order;
        private final OrderConfirmBaseDTO base;
        private final TicketSourceOrderSkuContext sourceContext;
        private final List<OrderAudienceSnapshotDTO> audiences;
        private final CreateOrderResponse response;
        private final boolean sourceOrder;

        private PreparedOrder(TicketOrderInsertDTO order, OrderConfirmBaseDTO base,
                              TicketSourceOrderSkuContext sourceContext,
                              List<OrderAudienceSnapshotDTO> audiences,
                              CreateOrderResponse response, boolean sourceOrder) {
            this.order = order;
            this.base = base;
            this.sourceContext = sourceContext;
            this.audiences = audiences;
            this.response = response;
            this.sourceOrder = sourceOrder;
        }
    }

    private static class CancelPlan {
        private final CancelOrderBaseDTO order;
        private final List<OrderItemReleaseDTO> items;
        private final TicketSourceOrderBridge bridge;
        private final boolean sourceOrder;
        private final CancelOrderResponse completedResponse;

        private CancelPlan(CancelOrderBaseDTO order, List<OrderItemReleaseDTO> items,
                           TicketSourceOrderBridge bridge, boolean sourceOrder,
                           CancelOrderResponse completedResponse) {
            this.order = order;
            this.items = items;
            this.bridge = bridge;
            this.sourceOrder = sourceOrder;
            this.completedResponse = completedResponse;
        }

        private static CancelPlan local(CancelOrderBaseDTO order, List<OrderItemReleaseDTO> items) {
            return new CancelPlan(order, items, null, false, null);
        }
        private static CancelPlan source(CancelOrderBaseDTO order, TicketSourceOrderBridge bridge) {
            return new CancelPlan(order, null, bridge, true, null);
        }
        private static CancelPlan completed(CancelOrderResponse response) {
            return new CancelPlan(null, null, null, false, response);
        }
    }

    private static class AmountResult {
        private final BigDecimal ticketAmount;
        private final BigDecimal serviceFeeAmount;
        private final BigDecimal deliveryFeeAmount;
        private final BigDecimal discountAmount;
        private final BigDecimal totalAmount;
        private final BigDecimal payAmount;

        private AmountResult(BigDecimal ticketAmount, BigDecimal serviceFeeAmount,
                             BigDecimal deliveryFeeAmount, BigDecimal discountAmount,
                             BigDecimal totalAmount, BigDecimal payAmount) {
            this.ticketAmount = ticketAmount;
            this.serviceFeeAmount = serviceFeeAmount;
            this.deliveryFeeAmount = deliveryFeeAmount;
            this.discountAmount = discountAmount;
            this.totalAmount = totalAmount;
            this.payAmount = payAmount;
        }
        public BigDecimal getTicketAmount() { return ticketAmount; }
        public BigDecimal getServiceFeeAmount() { return serviceFeeAmount; }
        public BigDecimal getDeliveryFeeAmount() { return deliveryFeeAmount; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getPayAmount() { return payAmount; }
    }
}
