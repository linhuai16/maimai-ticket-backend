package com.example.maimaibackend.controller.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.provider.model.ProviderMoney;
import com.example.maimaibackend.ticketsource.provider.model.ProviderRefundQuote;
import com.example.maimaibackend.ticketsource.order.provider.V11OrderService;
import com.example.maimaibackend.ticketsource.order.provider.model.*;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.V11ShipmentService;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.model.V11ShipmentSyncRequest;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.model.V11ShipmentView;
import com.example.maimaibackend.ticketsource.workflow.TicketSourceWorkflowService;
import com.example.maimaibackend.ticketsource.workflow.model.V12DynamicCredentialView;
import com.example.maimaibackend.ticketsource.user.model.*;
import com.example.maimaibackend.ticketsource.purchase.options.V12PurchaseOptionService;
import com.example.maimaibackend.ticketsource.purchase.options.model.V12PurchaseOptionsView;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 麦麦用户侧订单安全 Facade。
 * HarmonyOS 客户端统一通过 /api/ticket-source/orders/v12 访问订单能力，
 * 避免 providerOrderId/providerTicketId/providerSkuId/结算成本等内部字段进入客户端。
 */
@RestController
@RequestMapping("/api/ticket-source/orders/v12")
public class V12UserOrderController {
    private final V11OrderService orderService;
    private final V11ShipmentService shipmentService;
    private final TicketSourceWorkflowService workflowService;
    private final V12PurchaseOptionService purchaseOptionService;

    public V12UserOrderController(V11OrderService orderService,
                                  V11ShipmentService shipmentService,
                                  TicketSourceWorkflowService workflowService,
                                  V12PurchaseOptionService purchaseOptionService) {
        this.orderService = orderService;
        this.shipmentService = shipmentService;
        this.workflowService = workflowService;
        this.purchaseOptionService = purchaseOptionService;
    }

    @GetMapping("/purchase-options")
    public Result<V12PurchaseOptionsView> purchaseOptions(@RequestParam Long projectId,
                                                           @RequestParam Long sessionId,
                                                           @RequestParam Long skuId) {
        return Result.success(purchaseOptionService.get(projectId, sessionId, skuId));
    }

    @PostMapping("/quote")
    public Result<V12UserOrderQuoteView> quote(@RequestBody V11OrderQuoteRequest request) {
        purchaseOptionService.validateUserSelection(request);
        return Result.success(toUserQuote(orderService.quote(request)));
    }

    @PostMapping
    public Result<V12UserOrderCreateView> create(@RequestBody V11OrderCreateRequest request) {
        V11OrderCreateResult value = orderService.create(request);
        return Result.success(new V12UserOrderCreateView(
                value.orderId(), value.orderNo(), value.orderStatus(), value.paymentStatus(), value.fulfillmentMode(),
                value.ticketCount(), money(value.payAmount()), value.payExpireTime()));
    }

    @PostMapping("/{orderId}/pay")
    public Result<V12UserOrderActionView> pay(@PathVariable Long orderId,
                                              @RequestBody V11OrderActionRequest request) {
        return Result.success(toUserAction(orderService.pay(orderId, request)));
    }

    @PostMapping("/{orderId}/cancel")
    public Result<V12UserOrderActionView> cancel(@PathVariable Long orderId,
                                                 @RequestBody V11OrderActionRequest request) {
        return Result.success(toUserAction(orderService.cancel(orderId, request)));
    }

    @GetMapping("/{orderId}/shipment")
    public Result<V12UserShipmentView> shipment(@PathVariable Long orderId, @RequestParam Long userId) {
        return Result.success(toUserShipment(shipmentService.get(orderId, userId)));
    }

    @PostMapping("/{orderId}/shipment/sync")
    public Result<V12UserShipmentView> syncShipment(@PathVariable Long orderId,
                                                     @RequestBody V11ShipmentSyncRequest request) {
        return Result.success(toUserShipment(shipmentService.sync(orderId, request == null ? null : request.userId())));
    }

    @GetMapping("/{orderId}/tickets")
    public Result<List<V12UserTicketView>> tickets(@PathVariable Long orderId, @RequestParam Long userId) {
        List<Map<String, Object>> rows = workflowService.listTickets(orderId, userId);
        List<V12UserTicketView> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            result.add(new V12UserTicketView(
                    longValue(row.get("ticketId")), text(row.get("ticketNo")), text(row.get("ticketStatus")),
                    text(row.get("credentialType")), text(row.get("dynamicQrMode")), text(row.get("credentialVersion")),
                    localDateTime(row.get("credentialExpireTime")), integer(row.get("refreshAfterSeconds")),
                    text(row.get("seatInfo")), text(row.get("clientTicketNo")), text(row.get("holderRef")), text(row.get("holderName"))));
        }
        return Result.success(result);
    }

    @PostMapping("/{orderId}/tickets/{ticketId}/dynamic-credential:refresh")
    public Result<V12UserDynamicCredentialView> refreshDynamicCredential(@PathVariable Long orderId,
                                                                          @PathVariable Long ticketId,
                                                                          @RequestParam Long userId) {
        V12DynamicCredentialView value = workflowService.refreshDynamicCredential(orderId, ticketId, userId);
        return Result.success(new V12UserDynamicCredentialView(
                value.ticketId(), value.credentialType(), value.credentialPayload(), value.credentialVersion(),
                value.issuedAt(), value.expiresAt(), value.refreshAfterSeconds(), value.serverTime()));
    }

    @GetMapping("/{orderId}/refund-quote")
    public Result<V12UserRefundQuoteView> refundQuote(@PathVariable Long orderId, @RequestParam Long userId) {
        ProviderRefundQuote value = workflowService.quoteRefund(orderId, userId);
        return Result.success(new V12UserRefundQuoteView(
                value.refundScope().name(), value.refundable(), value.orderAmount(), value.refundableAmount(),
                value.serviceFee(), value.refundableDeliveryFee(), value.nonRefundableDeliveryFee(),
                value.promotionRollbackAmount(), value.unavailableReason(), value.quoteExpireAt()));
    }

    private static V12UserOrderQuoteView toUserQuote(V11OrderQuoteResult value) {
        V11OrderQuoteItem rawItem = value.item();
        List<V12UserOrderQuoteView.Ticket> tickets = rawItem.tickets() == null ? List.of() : rawItem.tickets().stream()
                .map(t -> new V12UserOrderQuoteView.Ticket(t.clientTicketNo(), t.audienceId()))
                .toList();
        V12UserOrderQuoteView.Item item = new V12UserOrderQuoteView.Item(
                rawItem.skuId(), rawItem.skuName(), rawItem.quantity(), money(rawItem.faceUnitPrice()),
                money(rawItem.providerSaleUnitPrice()), money(rawItem.subtotalAmount()), tickets);

        List<V12UserOrderQuoteView.Promotion> promotions = value.promotions() == null ? List.of() : value.promotions().stream()
                .map(p -> new V12UserOrderQuoteView.Promotion(p.promotionType(), p.title(), money(p.discountAmount())))
                .toList();
        return new V12UserOrderQuoteView(
                value.quoteId(), value.userId(), value.projectId(), value.sessionId(), value.purchaseMode(),
                value.ticketMode(), value.deliveryMode(), value.addressId(), item, promotions, value.totalTicketCount(),
                money(value.faceAmount()), money(value.ticketAmount()), money(value.discountAmount()),
                money(value.deliveryFeeAmount()), money(value.serviceFeeAmount()), money(value.payAmount()),
                value.expireTime(), value.warnings() == null ? List.of() : value.warnings());
    }

    private static V12UserOrderActionView toUserAction(V11OrderActionResult value) {
        return new V12UserOrderActionView(
                value.orderId(), value.orderNo(), value.orderStatus(), value.paymentStatus(), value.ticketCount(),
                value.operationTime(), value.message());
    }

    private static V12UserShipmentView toUserShipment(V11ShipmentView value) {
        return new V12UserShipmentView(
                value.orderId(), value.orderNo(), value.deliveryType(), money(value.deliveryFeeAmount()),
                value.shipmentStatus(), value.carrierCode(), value.carrierName(), value.waybillNo(), value.trackingUrl(),
                value.shippedTime(), value.signedTime(), value.lastSyncTime(), value.providerSyncSupported(),
                value.deliveryFeeRefundableHint(), value.deliveryFeeRefundHint(), value.lastSyncStatus(), value.warnings());
    }

    private static ProviderMoney money(BigDecimal value) {
        return ProviderMoney.fromMajor(value == null ? BigDecimal.ZERO : value, "CNY");
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long longValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.valueOf(String.valueOf(value));
    }

    private static Integer integer(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        return Integer.valueOf(String.valueOf(value));
    }

    private static LocalDateTime localDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime local) return local;
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toLocalDateTime();
        return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
    }
}
