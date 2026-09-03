package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.dto.payment.MockPaySuccessRequest;
import com.example.maimaibackend.dto.payment.PayOrderAudienceDTO;
import com.example.maimaibackend.dto.payment.PayOrderBaseDTO;
import com.example.maimaibackend.dto.payment.PayOrderItemDTO;
import com.example.maimaibackend.mapper.PaymentMapper;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceOrderMapper;
import com.example.maimaibackend.service.admin.AdminTicketLogService;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceGateway;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCallResult;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceConfirmPaymentRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProviderOrder;
import com.example.maimaibackend.ticketsource.order.model.TicketSourceOrderBridge;
import com.example.maimaibackend.ticketsource.issue.TicketSourceIssueService;
import com.example.maimaibackend.vo.payment.MockPaySuccessResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private static final String ORDER_STATUS_WAIT_PAY = "WAIT_PAY";
    private static final String ORDER_STATUS_WAIT_USE = "WAIT_USE";
    private static final String TICKET_STATUS_GENERATING = "GENERATING";
    private static final String FULFILLMENT_SOURCE = "TICKET_SOURCE";

    private final PaymentMapper paymentMapper;
    private final TicketSourceOrderMapper sourceOrderMapper;
    private final TicketSourceGateway sourceGateway;
    private final AdminTicketLogService adminTicketLogService;
    private final TicketSourceIssueService issueService;
    private final TransactionTemplate transactionTemplate;

    public PaymentService(
            PaymentMapper paymentMapper,
            TicketSourceOrderMapper sourceOrderMapper,
            TicketSourceGateway sourceGateway,
            AdminTicketLogService adminTicketLogService,
            TicketSourceIssueService issueService,
            PlatformTransactionManager transactionManager
    ) {
        this.paymentMapper = paymentMapper;
        this.sourceOrderMapper = sourceOrderMapper;
        this.sourceGateway = sourceGateway;
        this.adminTicketLogService = adminTicketLogService;
        this.issueService = issueService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public MockPaySuccessResponse mockPaySuccess(Long orderId, MockPaySuccessRequest request) {
        validateId(orderId, "orderId");
        if (request == null) throw new BusinessException("请求体不能为空");
        validateId(request.getUserId(), "userId");
        validatePayMethod(request.getPayMethod());

        PaymentPlan plan = transactionTemplate.execute(status -> preparePayment(orderId, request));
        if (plan == null) throw new BusinessException("支付准备失败");
        if (plan.completedResponse != null) return plan.completedResponse;
        if (!plan.sourceOrder) {
            recordIssueTaskLog(orderId, plan.totalQuantity);
            return plan.localResponse;
        }

        TicketSourceConfirmPaymentRequest sourceRequest = new TicketSourceConfirmPaymentRequest();
        sourceRequest.setClientOrderNo(plan.order.getOrderNo());
        sourceRequest.setPayAmount(plan.order.getPayAmount());
        sourceRequest.setCurrencyCode(plan.bridge.getCurrencyCode());
        sourceRequest.setPayMethod(request.getPayMethod().trim());
        sourceRequest.setPayTime(LocalDateTime.now());
        sourceRequest.setIdempotencyKey(plan.bridge.getPaymentIdempotencyKey());

        TicketSourceCallResult<TicketSourceProviderOrder> call = sourceGateway.confirmPayment(
                plan.bridge.getProviderCode(), plan.bridge.getProviderOrderId(), sourceRequest);
        if (!call.isSuccess() || call.getData() == null) {
            transactionTemplate.executeWithoutResult(status -> sourceOrderMapper.restoreReservedAfterPaymentFailure(
                    orderId, sourceErrorCode(call), safeMessage(call), call != null && call.isRetryable()));
            throw new BusinessException(buildSourceError("第三方票源支付确认失败", call));
        }

        TicketSourceProviderOrder providerOrder = call.getData();
        if (!"PAID".equals(providerOrder.getOrderStatus())) {
            handleUnexpectedProviderPaymentStatus(orderId, plan, call, providerOrder);
            throw new BusinessException("第三方订单未完成支付，当前状态：" + providerOrder.getOrderStatus());
        }
        PaymentExecution execution = transactionTemplate.execute(status -> finalizeSourcePayment(
                orderId, request, plan, call, providerOrder));
        if (execution == null) throw new BusinessException("保存支付结果失败");
        if (execution.newlyPaid) recordIssueTaskLog(orderId, plan.totalQuantity);
        return execution.response;
    }

    private PaymentPlan preparePayment(Long orderId, MockPaySuccessRequest request) {
        PayOrderBaseDTO order = paymentMapper.selectOrderForPay(orderId, request.getUserId());
        if (order == null) throw new BusinessException("订单不存在或不属于当前用户");
        if (ORDER_STATUS_WAIT_USE.equals(order.getOrderStatus()) && isPaid(order.getPaymentStatus())) {
            MockPaySuccessResponse completed = buildResponse(order, null);
            if (FULFILLMENT_SOURCE.equals(order.getFulfillmentMode())) {
                TicketSourceOrderBridge bridge = sourceOrderMapper.selectBridgeByOrderId(orderId);
                if (bridge != null) {
                    completed.setSourceOrderStatus(bridge.getBridgeStatus());
                    completed.setProviderOrderId(bridge.getProviderOrderId());
                }
            }
            return PaymentPlan.completed(completed);
        }
        if (!ORDER_STATUS_WAIT_PAY.equals(order.getOrderStatus())) {
            throw new BusinessException("只有待支付订单可以支付");
        }

        LocalDateTime now = LocalDateTime.now();
        if (order.getPayExpireTime() != null && now.isAfter(order.getPayExpireTime())) {
            throw new BusinessException("订单已超时，请取消后重新下单");
        }
        if (paymentMapper.countElectronicTicketsByOrderId(orderId) > 0) {
            throw new BusinessException("订单已生成电子票，请勿重复支付");
        }

        List<PayOrderItemDTO> items = paymentMapper.selectOrderItemsForPay(orderId);
        if (items == null || items.isEmpty()) throw new BusinessException("订单项不存在，支付失败");
        List<PayOrderAudienceDTO> audiences = paymentMapper.selectOrderAudiencesForTicket(orderId);
        int totalQuantity = validateItemsAndAudiences(items, audiences);

        if (!FULFILLMENT_SOURCE.equals(order.getFulfillmentMode())) {
            for (PayOrderItemDTO item : items) {
                if (paymentMapper.reduceLockedStockToSold(item.getSkuId(), item.getQuantity()) != 1) {
                    throw new BusinessException("锁定库存不足，支付失败");
                }
            }
            if (paymentMapper.updateOrderPaid(orderId, request.getPayMethod().trim(), now) != 1) {
                throw new BusinessException("更新订单支付状态失败");
            }
            insertGeneratingTickets(orderId, items, audiences, now);
            order.setOrderStatus(ORDER_STATUS_WAIT_USE);
            order.setPaymentStatus("PAID");
            order.setPayTime(now);
            MockPaySuccessResponse response = buildResponse(order, null);
            response.setFulfillmentMode("LOCAL_COMPAT");
            response.setPaymentStatus("PAID");
            return PaymentPlan.local(order, items, audiences, totalQuantity, response);
        }

        TicketSourceOrderBridge bridge = sourceOrderMapper.selectBridgeByOrderIdForUpdate(orderId);
        if (bridge == null) throw new BusinessException("第三方订单映射不存在");
        if (!("RESERVED".equals(bridge.getBridgeStatus())
                || "PAYMENT_CONFIRMING".equals(bridge.getBridgeStatus())
                || "PAID".equals(bridge.getBridgeStatus()))) {
            throw new BusinessException("第三方订单尚未完成预占或当前不可支付");
        }
        if (bridge.getProviderOrderId() == null) throw new BusinessException("第三方订单ID缺失");
        if (!"PAID".equals(bridge.getBridgeStatus())
                && sourceOrderMapper.markPaymentConfirming(orderId) != 1) {
            throw new BusinessException("第三方订单支付状态已变化，请刷新后重试");
        }
        return PaymentPlan.source(order, bridge, items, audiences, totalQuantity);
    }

    private void handleUnexpectedProviderPaymentStatus(
            Long orderId,
            PaymentPlan plan,
            TicketSourceCallResult<TicketSourceProviderOrder> call,
            TicketSourceProviderOrder providerOrder
    ) {
        String providerStatus = providerOrder.getOrderStatus();
        if ("CANCELED".equals(providerStatus) || "EXPIRED".equals(providerStatus)) {
            transactionTemplate.executeWithoutResult(status -> {
                LocalDateTime cancelTime = providerOrder.getCancelTime() == null
                        ? LocalDateTime.now() : providerOrder.getCancelTime();
                sourceOrderMapper.updateOrderCanceledWithoutLocalStock(orderId, cancelTime);
                sourceOrderMapper.markCanceled(
                        orderId, "EXPIRED", providerStatus, cancelTime,
                        sourceResponseSnapshot(call, providerOrder));
                sourceOrderMapper.updateProviderInventorySnapshot(
                        plan.bridge.getProviderId(), plan.bridge.getSkuMappingId(), plan.bridge.getSkuId(),
                        providerOrder.getRemainingStock(), providerOrder.getDataVersion());
            });
            return;
        }
        transactionTemplate.executeWithoutResult(status -> sourceOrderMapper.restoreReservedAfterPaymentFailure(
                orderId, "SOURCE_PAYMENT_STATUS_INVALID",
                "第三方支付确认返回状态 " + providerStatus, false));
    }

    private PaymentExecution finalizeSourcePayment(
            Long orderId,
            MockPaySuccessRequest request,
            PaymentPlan plan,
            TicketSourceCallResult<TicketSourceProviderOrder> call,
            TicketSourceProviderOrder providerOrder
    ) {
        PayOrderBaseDTO current = paymentMapper.selectOrderForPay(orderId, request.getUserId());
        if (current == null) throw new BusinessException("订单不存在");
        if (ORDER_STATUS_WAIT_USE.equals(current.getOrderStatus()) && isPaid(current.getPaymentStatus())) {
            return new PaymentExecution(buildResponse(current, providerOrder), false);
        }
        if (!ORDER_STATUS_WAIT_PAY.equals(current.getOrderStatus())) {
            throw new BusinessException("本地订单状态不允许完成支付");
        }
        if (!"PAID".equals(providerOrder.getOrderStatus())) {
            throw new BusinessException("第三方订单未返回已支付状态");
        }
        if (paymentMapper.countElectronicTicketsByOrderId(orderId) > 0) {
            throw new BusinessException("订单已生成电子票，请勿重复处理");
        }

        LocalDateTime payTime = providerOrder.getPayTime() == null
                ? LocalDateTime.now() : providerOrder.getPayTime();
        if (paymentMapper.updateOrderPaid(orderId, request.getPayMethod().trim(), payTime) != 1) {
            throw new BusinessException("更新本地支付状态失败");
        }
        insertGeneratingTickets(orderId, plan.items, plan.audiences, payTime);
        if (sourceOrderMapper.markPaid(
                orderId, providerOrder.getOrderStatus(), payTime,
                sourceResponseSnapshot(call, providerOrder)) != 1) {
            throw new BusinessException("更新第三方订单桥接状态失败");
        }
        sourceOrderMapper.updateProviderInventorySnapshot(
                plan.bridge.getProviderId(), plan.bridge.getSkuMappingId(), plan.bridge.getSkuId(),
                providerOrder.getRemainingStock(), providerOrder.getDataVersion());
        issueService.createTask(orderId, plan.bridge.getBridgeId(), plan.bridge.getProviderId(),
                plan.bridge.getProviderOrderId(), plan.totalQuantity, current.getOrderNo(), payTime);

        current.setOrderStatus(ORDER_STATUS_WAIT_USE);
        current.setPaymentStatus("PROVIDER_CONFIRMED");
        current.setPayTime(payTime);
        return new PaymentExecution(buildResponse(current, providerOrder), true);
    }

    private int validateItemsAndAudiences(
            List<PayOrderItemDTO> items,
            List<PayOrderAudienceDTO> audiences
    ) {
        int totalQuantity = 0;
        for (PayOrderItemDTO item : items) {
            if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("订单项数据异常，支付失败");
            }
            totalQuantity += item.getQuantity();
        }
        if (audiences == null || audiences.size() != totalQuantity) {
            throw new BusinessException("订单观演人数量与购票数量不一致，支付失败");
        }
        return totalQuantity;
    }

    private void insertGeneratingTickets(
            Long orderId,
            List<PayOrderItemDTO> items,
            List<PayOrderAudienceDTO> audiences,
            LocalDateTime now
    ) {
        int audienceIndex = 0;
        int ticketIndex = 1;
        for (PayOrderItemDTO item : items) {
            for (int i = 0; i < item.getQuantity(); i++) {
                PayOrderAudienceDTO audience = audiences.get(audienceIndex++);
                paymentMapper.insertElectronicTicket(
                        generateTicketNo(orderId, ticketIndex++), orderId,
                        item.getOrderItemId(), audience.getOrderAudienceId(),
                        TICKET_STATUS_GENERATING, now);
            }
        }
    }

    private MockPaySuccessResponse buildResponse(
            PayOrderBaseDTO order,
            TicketSourceProviderOrder providerOrder
    ) {
        MockPaySuccessResponse response = new MockPaySuccessResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setOrderStatus(ORDER_STATUS_WAIT_USE);
        response.setPayAmount(order.getPayAmount());
        response.setPayTime(order.getPayTime());
        response.setFulfillmentMode(order.getFulfillmentMode());
        response.setPaymentStatus(order.getPaymentStatus());
        if (providerOrder != null) {
            response.setSourceOrderStatus(providerOrder.getOrderStatus());
            response.setProviderOrderId(providerOrder.getProviderOrderId());
            response.setPaymentStatus("PROVIDER_CONFIRMED");
            response.setFulfillmentMode(FULFILLMENT_SOURCE);
        }
        return response;
    }

    private boolean isPaid(String paymentStatus) {
        return "PAID".equals(paymentStatus) || "PROVIDER_CONFIRMED".equals(paymentStatus);
    }

    private void recordIssueTaskLog(Long orderId, int totalQuantity) {
        adminTicketLogService.recordSuccess(
                TicketOperationContext.system("PaymentService"),
                "ISSUE", "CREATE_ISSUE_TASK", "ORDER", orderId, orderId, null,
                "NO_TICKET", TICKET_STATUS_GENERATING,
                "支付成功后创建出票任务，共生成 " + totalQuantity + " 张待出票电子票", null);
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) throw new BusinessException("请求参数无效：" + name);
    }

    private void validatePayMethod(String payMethod) {
        if (payMethod == null || payMethod.trim().isEmpty()) throw new BusinessException("请选择支付方式");
    }

    private String generateTicketNo(Long orderId, int index) {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(100, 1000);
        return "ET" + timePart + orderId + String.format("%03d", index) + randomPart;
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

    private String sourceResponseSnapshot(
            TicketSourceCallResult<?> call,
            TicketSourceProviderOrder order
    ) {
        return "requestId=" + (call == null ? null : call.getRequestId())
                + ",success=" + (call != null && call.isSuccess())
                + ",providerOrderId=" + (order == null ? null : order.getProviderOrderId())
                + ",providerStatus=" + (order == null ? null : order.getOrderStatus())
                + ",remainingStock=" + (order == null ? null : order.getRemainingStock());
    }

    private static class PaymentPlan {
        private final PayOrderBaseDTO order;
        private final TicketSourceOrderBridge bridge;
        private final List<PayOrderItemDTO> items;
        private final List<PayOrderAudienceDTO> audiences;
        private final int totalQuantity;
        private final boolean sourceOrder;
        private final MockPaySuccessResponse localResponse;
        private final MockPaySuccessResponse completedResponse;

        private PaymentPlan(PayOrderBaseDTO order, TicketSourceOrderBridge bridge,
                            List<PayOrderItemDTO> items, List<PayOrderAudienceDTO> audiences,
                            int totalQuantity, boolean sourceOrder,
                            MockPaySuccessResponse localResponse,
                            MockPaySuccessResponse completedResponse) {
            this.order = order;
            this.bridge = bridge;
            this.items = items;
            this.audiences = audiences;
            this.totalQuantity = totalQuantity;
            this.sourceOrder = sourceOrder;
            this.localResponse = localResponse;
            this.completedResponse = completedResponse;
        }

        private static PaymentPlan local(PayOrderBaseDTO order, List<PayOrderItemDTO> items,
                                         List<PayOrderAudienceDTO> audiences, int totalQuantity,
                                         MockPaySuccessResponse response) {
            return new PaymentPlan(order, null, items, audiences, totalQuantity, false, response, null);
        }
        private static PaymentPlan source(PayOrderBaseDTO order, TicketSourceOrderBridge bridge,
                                          List<PayOrderItemDTO> items,
                                          List<PayOrderAudienceDTO> audiences, int totalQuantity) {
            return new PaymentPlan(order, bridge, items, audiences, totalQuantity, true, null, null);
        }
        private static PaymentPlan completed(MockPaySuccessResponse response) {
            return new PaymentPlan(null, null, null, null, 0, false, null, response);
        }
    }

    private static class PaymentExecution {
        private final MockPaySuccessResponse response;
        private final boolean newlyPaid;
        private PaymentExecution(MockPaySuccessResponse response, boolean newlyPaid) {
            this.response = response;
            this.newlyPaid = newlyPaid;
        }
    }
}
