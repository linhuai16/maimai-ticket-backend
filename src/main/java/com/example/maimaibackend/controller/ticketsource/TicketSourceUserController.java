package com.example.maimaibackend.controller.ticketsource;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.refund.RefundOrderRequest;
import com.example.maimaibackend.service.RefundService;
import com.example.maimaibackend.service.TicketService;
import com.example.maimaibackend.ticketsource.provider.model.ProviderMoney;
import com.example.maimaibackend.ticketsource.provider.model.ProviderRefundQuote;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.V11ShipmentService;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.model.V11ShipmentView;
import com.example.maimaibackend.ticketsource.workflow.TicketSourceWorkflowService;
import com.example.maimaibackend.ticketsource.workflow.model.V12DynamicCredentialView;
import com.example.maimaibackend.vo.refund.RefundApplyPageVO;
import com.example.maimaibackend.vo.refund.RefundOrderResponse;
import com.example.maimaibackend.vo.refund.RefundProgressVO;
import com.example.maimaibackend.vo.ticket.TicketDetailPageVO;
import com.example.maimaibackend.vo.ticket.TicketFolderPageVO;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户侧票务安全 Facade。
 * 只返回鸿蒙页面需要的数据；providerOrderId/providerTicketId/quoteId/结算成本等字段不得进入用户响应。
 */
@RestController
@RequestMapping("/api/ticket-source/user/v13")
public class TicketSourceUserController {

    private final TicketService ticketService;
    private final V11ShipmentService shipmentService;
    private final TicketSourceWorkflowService workflowService;
    private final RefundService refundService;

    public TicketSourceUserController(TicketService ticketService,
                                    V11ShipmentService shipmentService,
                                    TicketSourceWorkflowService workflowService,
                                    RefundService refundService) {
        this.ticketService = ticketService;
        this.shipmentService = shipmentService;
        this.workflowService = workflowService;
        this.refundService = refundService;
    }

    @GetMapping("/users/{userId}/ticket-folder")
    public Result<TicketFolderPageVO> ticketFolder(@PathVariable Long userId) {
        return Result.success(ticketService.getTicketFolder(userId));
    }

    @GetMapping("/orders/{orderId}/ticket-detail")
    public Result<TicketDetailPageVO> ticketDetail(@PathVariable Long orderId,
                                                    @RequestParam Long userId,
                                                    @RequestParam(required = false) Long ticketId) {
        return Result.success(ticketService.getTicketDetail(userId, orderId, ticketId));
    }

    @PostMapping("/orders/{orderId}/tickets/{ticketId}/dynamic-credential:refresh")
    public Result<DynamicCredentialView> refreshDynamicCredential(@PathVariable Long orderId,
                                                                   @PathVariable Long ticketId,
                                                                   @RequestParam Long userId) {
        V12DynamicCredentialView value = workflowService.refreshDynamicCredential(orderId, ticketId, userId);
        return Result.success(new DynamicCredentialView(
                value.ticketId(), value.credentialType(), value.credentialPayload(), value.credentialVersion(),
                value.issuedAt(), value.expiresAt(), value.refreshAfterSeconds(), value.serverTime()));
    }

    @GetMapping("/orders/{orderId}/shipment")
    public Result<ShipmentView> shipment(@PathVariable Long orderId, @RequestParam Long userId) {
        return Result.success(toShipment(shipmentService.get(orderId, userId)));
    }

    @PostMapping("/orders/{orderId}/shipment/sync")
    public Result<ShipmentView> syncShipment(@PathVariable Long orderId, @RequestBody UserRequest request) {
        requireUser(request);
        return Result.success(toShipment(shipmentService.sync(orderId, request.userId())));
    }

    @GetMapping("/orders/{orderId}/refund-confirm")
    public Result<RefundApplyPageVO> refundConfirm(@PathVariable Long orderId, @RequestParam Long userId) {
        return Result.success(refundService.getRefundConfirm(userId, orderId));
    }

    @GetMapping("/orders/{orderId}/refund-quote")
    public Result<RefundQuoteView> refundQuote(@PathVariable Long orderId, @RequestParam Long userId) {
        ProviderRefundQuote value = workflowService.quoteRefund(orderId, userId);
        return Result.success(new RefundQuoteView(
                value.refundScope().name(), value.refundable(), money(value.orderAmount()), money(value.refundableAmount()),
                money(value.serviceFee()), money(value.refundableDeliveryFee()), money(value.nonRefundableDeliveryFee()),
                money(value.promotionRollbackAmount()), value.refundable() ? null : "当前订单暂不支持退款", value.quoteExpireAt()));
    }

    @PostMapping("/orders/{orderId}/refunds")
    public Result<RefundApplyResultView> applyRefund(@PathVariable Long orderId, @RequestBody RefundOrderRequest request) {
        RefundOrderResponse value = refundService.applyRefund(orderId, request);
        return Result.success(new RefundApplyResultView(
                value.getRefundId(), value.getStatus(), value.getExpectedRefundAmount()));
    }

    @GetMapping("/orders/{orderId}/refund-progress")
    public Result<RefundProgressView> refundProgressByOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        RefundProgressVO value = refundService.getRefundProgress(userId, orderId);
        return Result.success(new RefundProgressView(
                value.getRefundId(), value.getOrderId(), value.getRefundStatus(),
                value.getRefundAmount(), value.getFeeAmount(), value.getApplyTime(), value.getRefundTime(),
                value.getFailReason(), BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2)));
    }

    @GetMapping("/refunds/{refundId}")
    public Result<RefundProgressView> refundProgress(@PathVariable Long refundId, @RequestParam Long userId) {
        Map<String, Object> raw = workflowService.getRefund(refundId, userId);
        return Result.success(new RefundProgressView(
                longValue(raw.get("refundId")), longValue(raw.get("orderId")), text(raw.get("refundStatus")),
                decimal(raw.get("refundAmount")), decimal(raw.get("feeAmount")), localDateTime(raw.get("applyTime")), localDateTime(raw.get("refundTime")),
                text(raw.get("failReason")), decimal(raw.get("refundableDeliveryFee")), decimal(raw.get("nonRefundableDeliveryFee"))));
    }

    private static ShipmentView toShipment(V11ShipmentView value) {
        return new ShipmentView(
                value.orderId(), value.orderNo(), value.deliveryType(), money(value.deliveryFeeAmount()),
                value.shipmentStatus().name(), value.carrierName(), value.waybillNo(), value.trackingUrl(),
                value.shippedTime(), value.signedTime(), value.lastSyncTime(), value.providerSyncSupported(),
                value.deliveryFeeRefundableHint(), userRefundHint(value.deliveryFeeRefundHint()), value.lastSyncStatus(), userWarnings(value.warnings()));
    }

    private static ProviderMoney money(BigDecimal value) {
        return ProviderMoney.fromMajor(value == null ? BigDecimal.ZERO : value, "CNY");
    }

    private static ProviderMoney money(ProviderMoney value) {
        return value == null ? ProviderMoney.cny(0L) : value;
    }

    private static String userRefundHint(String value) {
        if (value == null || value.isBlank()) return "";
        return value.replace("第三方退款试算", "退款确认结果")
                .replace("第三方", "出票方").replace("provider", "出票方");
    }

    private static List<String> userWarnings(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) return List.of();
        return warnings.stream().map(TicketSourceUserController::userRefundHint).toList();
    }

    private static void requireUser(UserRequest request) {
        if (request == null || request.userId() == null || request.userId() <= 0) {
            throw new BusinessException("userId不合法");
        }
    }

    private static Long longValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.valueOf(String.valueOf(value));
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) return BigDecimal.ZERO.setScale(2);
        if (value instanceof BigDecimal b) return b;
        return new BigDecimal(String.valueOf(value));
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static LocalDateTime localDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime t) return t;
        if (value instanceof OffsetDateTime t) return t.toLocalDateTime();
        if (value instanceof java.sql.Timestamp t) return t.toLocalDateTime();
        return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
    }

    public record UserRequest(Long userId) {}

    /** 动态码明文仅存在于本次响应中，服务端和客户端均不得持久化。 */
    public record DynamicCredentialView(
            Long ticketId,
            String credentialType,
            String credentialPayload,
            String credentialVersion,
            OffsetDateTime issuedAt,
            OffsetDateTime expiresAt,
            int refreshAfterSeconds,
            OffsetDateTime serverTime
    ) {}

    public record ShipmentView(
            Long orderId,
            String orderNo,
            String deliveryType,
            ProviderMoney deliveryFee,
            String shipmentStatus,
            String carrierName,
            String waybillNo,
            String trackingUrl,
            LocalDateTime shippedTime,
            LocalDateTime signedTime,
            LocalDateTime lastSyncTime,
            boolean canRefresh,
            boolean deliveryFeeRefundableHint,
            String deliveryFeeRefundHint,
            String lastSyncStatus,
            List<String> warnings
    ) {}

    public record RefundQuoteView(
            String refundScope,
            boolean refundable,
            ProviderMoney orderAmount,
            ProviderMoney refundableAmount,
            ProviderMoney serviceFee,
            ProviderMoney refundableDeliveryFee,
            ProviderMoney nonRefundableDeliveryFee,
            ProviderMoney promotionRollbackAmount,
            String unavailableReason,
            OffsetDateTime quoteExpireAt
    ) {}

    public record RefundApplyResultView(Long refundId, String status, BigDecimal expectedRefundAmount) {}

    public record RefundProgressView(
            Long refundId,
            Long orderId,
            String refundStatus,
            BigDecimal refundAmount,
            BigDecimal feeAmount,
            LocalDateTime applyTime,
            LocalDateTime refundTime,
            String failReason,
            BigDecimal refundableDeliveryFee,
            BigDecimal nonRefundableDeliveryFee
    ) {}
}
