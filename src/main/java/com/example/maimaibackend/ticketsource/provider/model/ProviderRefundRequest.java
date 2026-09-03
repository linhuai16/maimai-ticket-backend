package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.RefundScope;

/** V1.2 整单退款请求。禁止 providerTicketIds 等逐票退款字段。 */
public record ProviderRefundRequest(
        String clientRefundNo,
        RefundScope refundScope,
        String reasonCode,
        String reason,
        ProviderMoney quotedRefundAmount,
        String quoteId,
        String idempotencyKey
) {
    public ProviderRefundRequest {
        clientRefundNo = ModelSupport.required(clientRefundNo, "clientRefundNo");
        if (refundScope != RefundScope.FULL_ORDER) throw new IllegalArgumentException("V1.2当前只支持FULL_ORDER整单退款");
        reasonCode = ModelSupport.required(reasonCode, "reasonCode");
        if (quotedRefundAmount == null) throw new IllegalArgumentException("quotedRefundAmount不能为空");
        quoteId = ModelSupport.required(quoteId, "quoteId");
        idempotencyKey = ModelSupport.required(idempotencyKey, "idempotencyKey");
    }

    /** 旧映射代码兼容访问器，后续删除旧链路时一并移除。 */
    public ProviderMoney expectedRefundAmount() { return quotedRefundAmount; }
    public String refundQuoteId() { return quoteId; }
}
