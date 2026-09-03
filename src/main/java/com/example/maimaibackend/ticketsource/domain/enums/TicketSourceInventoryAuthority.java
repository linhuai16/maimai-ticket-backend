package com.example.maimaibackend.ticketsource.domain.enums;

/**
 * 本地票档库存字段的权威来源。
 */
public enum TicketSourceInventoryAuthority {
    /** 兼容旧架构，库存仍由本地管理员维护。 */
    LOCAL_COMPAT,
    /** 本地库存是第三方最近一次同步的快照。 */
    PROVIDER_SNAPSHOT,
    /** 下单前必须向第三方实时查询，快照只用于展示。 */
    PROVIDER_REALTIME
}
