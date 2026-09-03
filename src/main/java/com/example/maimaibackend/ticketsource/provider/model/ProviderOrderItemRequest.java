package com.example.maimaibackend.ticketsource.provider.model;

import java.util.List;

/** @deprecated 核心下单已固定单票档；仅保留用于旧快照兼容读取。 */
@Deprecated
public record ProviderOrderItemRequest(
        String clientLineNo,
        String ticketProductId,
        ProviderMoney expectedUnitPrice,
        List<ProviderTicketAssignmentRequest> tickets
) {
    public ProviderOrderItemRequest {
        clientLineNo = ModelSupport.required(clientLineNo, "clientLineNo");
        ticketProductId = ModelSupport.required(ticketProductId, "ticketProductId");
        if (expectedUnitPrice == null) throw new IllegalArgumentException("expectedUnitPrice不能为空");
        tickets = ModelSupport.list(tickets);
        if (tickets.isEmpty()) throw new IllegalArgumentException("每个票档至少包含一张票");
    }
    public int quantity() { return tickets.size(); }
}
