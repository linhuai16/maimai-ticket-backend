package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.CredentialType;
import com.example.maimaibackend.ticketsource.provider.enums.DeliveryMode;
import com.example.maimaibackend.ticketsource.provider.enums.PurchaseMode;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * V1.2 第三方统一下单请求。
 *
 * 业务约束固定为：一个订单只包含一个场次下的一个票档，可购买多张票，
 * 每张票必须绑定一个不同观演人。第三方平台即使支持多票档订单，Adapter
 * 也只提交一个 ticketProductId，避免核心业务出现客户端无法操作的多票档流程。
 */
public record ProviderOrderCreateRequest(
        String clientOrderNo,
        String projectId,
        String sessionId,
        String ticketProductId,
        ProviderMoney expectedUnitPrice,
        List<ProviderTicketAssignmentRequest> tickets,
        Map<String, ProviderPerson> holders,
        ProviderPerson buyer,
        ProviderContact contact,
        PurchaseMode purchaseMode,
        CredentialType ticketMode,
        DeliveryMode deliveryMode,
        ProviderAddress address,
        ProviderMoney expectedTicketAmount,
        ProviderMoney expectedDeliveryFee,
        ProviderMoney expectedPayAmount,
        String deliveryQuoteId,
        OffsetDateTime reservationExpireAt,
        String idempotencyKey
) {
    public ProviderOrderCreateRequest {
        clientOrderNo = ModelSupport.required(clientOrderNo, "clientOrderNo");
        projectId = ModelSupport.required(projectId, "projectId");
        sessionId = ModelSupport.required(sessionId, "sessionId");
        ticketProductId = ModelSupport.required(ticketProductId, "ticketProductId");
        if (expectedUnitPrice == null) throw new IllegalArgumentException("expectedUnitPrice不能为空");
        tickets = ModelSupport.list(tickets);
        holders = ModelSupport.map(holders);
        if (tickets.isEmpty()) throw new IllegalArgumentException("订单至少包含一张票");
        if (purchaseMode == null || ticketMode == null || deliveryMode == null) {
            throw new IllegalArgumentException("购买/票型/配送模式不能为空");
        }
        idempotencyKey = ModelSupport.required(idempotencyKey, "idempotencyKey");
        if (deliveryMode == DeliveryMode.EXPRESS && address == null) {
            throw new IllegalArgumentException("快递票必须提供收货地址");
        }
        if (deliveryMode == DeliveryMode.EXPRESS && contact == null) {
            throw new IllegalArgumentException("快递票必须提供收件联系人");
        }
        validateAssignments(tickets, holders);
    }

    public int quantity() {
        return tickets.size();
    }

    public int totalTicketCount() {
        return quantity();
    }

    private static void validateAssignments(List<ProviderTicketAssignmentRequest> tickets,
                                            Map<String, ProviderPerson> holders) {
        Set<String> ticketNos = new HashSet<>();
        Set<String> holderRefs = new HashSet<>();
        for (ProviderTicketAssignmentRequest ticket : tickets) {
            if (!ticketNos.add(ticket.clientTicketNo())) {
                throw new IllegalArgumentException("clientTicketNo重复: " + ticket.clientTicketNo());
            }
            if (!holderRefs.add(ticket.holderRef())) {
                throw new IllegalArgumentException("一个观演人在同一订单中只能购买一张票: " + ticket.holderRef());
            }
            if (!holders.containsKey(ticket.holderRef())) {
                throw new IllegalArgumentException("未找到观演人: " + ticket.holderRef());
            }
        }
    }
}
