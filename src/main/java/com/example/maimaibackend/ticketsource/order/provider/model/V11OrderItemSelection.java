package com.example.maimaibackend.ticketsource.order.provider.model;

import java.util.List;

/** @deprecated 下单请求已改为 skuId + tickets；仅保留旧JSON快照兼容。 */
@Deprecated
public record V11OrderItemSelection(Long skuId, List<V11TicketSelection> tickets) {}
