package com.example.maimaibackend.ticketsource.status;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * V1.3.3 本地展示状态重算服务。
 *
 * <p>第三方同步负责更新 ticket_source_*_mapping 的源状态和库存快照；本服务只根据已经落库的
 * 第三方状态快照、本地当前时间、本地库存和映射关系重算场次/票档用户侧展示状态。performance_project.project_status 由麦麦运营维护，本服务不再改写。</p>
 *
 * <p>这不是提交订单页的可购性门禁；提交页最终仍由 V11OrderService 在 quote/create 时向第三方
 * 确认价格和库存。本服务的职责是让首页、详情页、票档页先呈现正确按钮状态。</p>
 */
@Service
public class V13LocalStatusRecalculateService {
    private final JdbcTemplate jdbcTemplate;

    public V13LocalStatusRecalculateService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public V13LocalStatusRecalculateResult recalculateAll() {
        int expiredSessions = markExpiredSessions();
        int expiredSessionSkus = markSkusOfEndedSessions();
        int unmappedOrLocalCompatSkus = hideUnmappedOrLocalCompatSkus();
        int mappedSkus = refreshMappedSkuStatus();
        int sessionsAggregated = aggregateSessionStatus();
        expireEndedSessionTickets();
        finishEndedOrders();
        return new V13LocalStatusRecalculateResult(LocalDateTime.now(), expiredSessions, expiredSessionSkus,
                unmappedOrLocalCompatSkus, mappedSkus, sessionsAggregated, 0);
    }

    /** 已过结束时间的场次直接落库为 ENDED。 */
    private int markExpiredSessions() {
        return jdbcTemplate.update("""
                UPDATE performance_session
                SET session_status = 'ENDED', update_time = NOW()
                WHERE end_time IS NOT NULL
                  AND end_time <= NOW()
                  AND session_status <> 'ENDED'
                  AND session_status <> 'OFFLINE'
                """);
    }

    /** 场次已结束时，所属票档不可再购买。ticket_sku 暂无 ENDED 展示态，统一落 SOLD_OUT。 */
    private int markSkusOfEndedSessions() {
        return jdbcTemplate.update("""
                UPDATE ticket_sku sku
                JOIN performance_session s ON s.session_id = sku.session_id
                SET sku.sku_status = 'SOLD_OUT', sku.update_time = NOW()
                WHERE s.end_time IS NOT NULL
                  AND s.end_time <= NOW()
                  AND sku.sku_status IN ('ON_SALE','PRESALE')
                """);
    }

    /**
     * 旧 LOCAL_COMPAT 或未绑定第三方票品映射的票档不进入 V1.3 用户购票列表。
     * 历史订单仍依赖原项目/场次/票档快照，不删除数据，只将展示状态转为 OFFLINE。
     */
    private int hideUnmappedOrLocalCompatSkus() {
        return jdbcTemplate.update("""
                UPDATE ticket_sku sku
                JOIN performance_session s ON s.session_id = sku.session_id
                LEFT JOIN ticket_source_sku_mapping skum
                       ON skum.sku_id = sku.sku_id AND skum.mapping_status = 'BOUND'
                LEFT JOIN ticket_source_session_mapping smap
                       ON smap.mapping_id = skum.session_mapping_id AND smap.mapping_status = 'BOUND'
                LEFT JOIN ticket_source_project_mapping pm
                       ON pm.mapping_id = smap.project_mapping_id AND pm.mapping_status = 'BOUND'
                LEFT JOIN ticket_source_provider pvd
                       ON pvd.provider_id = skum.provider_id AND pvd.provider_status = 'ENABLED'
                SET sku.sku_status = 'OFFLINE', sku.update_time = NOW()
                WHERE (s.end_time IS NULL OR s.end_time > NOW())
                  AND sku.sku_status <> 'OFFLINE'
                  AND (
                        sku.inventory_authority = 'LOCAL_COMPAT'
                     OR skum.mapping_id IS NULL
                     OR smap.mapping_id IS NULL
                     OR pm.mapping_id IS NULL
                     OR pvd.provider_id IS NULL
                  )
                """);
    }

    /** 按第三方票品状态/库存快照刷新已绑定票档。库存 NULL 不等于售罄。 */
    private int refreshMappedSkuStatus() {
        return jdbcTemplate.update("""
                UPDATE ticket_sku sku
                JOIN performance_session s ON s.session_id = sku.session_id
                JOIN ticket_source_sku_mapping skum ON skum.sku_id = sku.sku_id AND skum.mapping_status = 'BOUND'
                JOIN ticket_source_session_mapping smap ON smap.mapping_id = skum.session_mapping_id AND smap.mapping_status = 'BOUND'
                JOIN ticket_source_project_mapping pm ON pm.mapping_id = smap.project_mapping_id AND pm.mapping_status = 'BOUND'
                JOIN ticket_source_provider pvd ON pvd.provider_id = skum.provider_id AND pvd.provider_status = 'ENABLED'
                SET sku.sku_status = CASE
                        WHEN skum.source_sale_status IN ('SOLD_OUT') THEN 'SOLD_OUT'
                        WHEN skum.available_stock_snapshot IS NOT NULL AND skum.available_stock_snapshot <= 0 THEN 'SOLD_OUT'
                        WHEN skum.source_sale_status = 'PRESALE' THEN 'PRESALE'
                        WHEN skum.source_sale_status = 'ON_SALE' THEN 'ON_SALE'
                        WHEN skum.source_sale_status IN ('ENDED','OFF_SHELF','SUSPENDED','UNKNOWN') THEN 'OFFLINE'
                        ELSE sku.sku_status
                    END,
                    sku.stock_available = CASE
                        WHEN skum.available_stock_snapshot IS NULL THEN sku.stock_available
                        WHEN skum.available_stock_snapshot <= sku.stock_locked THEN 0
                        ELSE skum.available_stock_snapshot - sku.stock_locked
                    END,
                    sku.update_time = NOW()
                WHERE (s.end_time IS NULL OR s.end_time > NOW())
                  AND sku.inventory_authority <> 'LOCAL_COMPAT'
                """);
    }

    /** 场次状态由时间优先，其次由票档状态聚合。 */
    private int aggregateSessionStatus() {
        return jdbcTemplate.update("""
                UPDATE performance_session s
                LEFT JOIN (
                    SELECT session_id,
                           MAX(CASE WHEN sku_status = 'ON_SALE' THEN 1 ELSE 0 END) AS has_on_sale,
                           MAX(CASE WHEN sku_status = 'PRESALE' THEN 1 ELSE 0 END) AS has_presale,
                           MAX(CASE WHEN sku_status = 'SOLD_OUT' THEN 1 ELSE 0 END) AS has_sold_out,
                           MAX(CASE WHEN sku_status <> 'OFFLINE' THEN 1 ELSE 0 END) AS has_visible
                    FROM ticket_sku
                    GROUP BY session_id
                ) sku_state ON sku_state.session_id = s.session_id
                SET s.session_status = CASE
                        WHEN s.end_time IS NOT NULL AND s.end_time <= NOW() THEN 'ENDED'
                        WHEN COALESCE(sku_state.has_on_sale, 0) = 1 THEN 'ON_SALE'
                        WHEN COALESCE(sku_state.has_presale, 0) = 1 THEN 'PRESALE'
                        WHEN COALESCE(sku_state.has_sold_out, 0) = 1 THEN 'SOLD_OUT'
                        WHEN COALESCE(sku_state.has_visible, 0) = 0 THEN 'OFFLINE'
                        ELSE s.session_status
                    END,
                    s.update_time = NOW()
                WHERE s.session_status <> CASE
                        WHEN s.end_time IS NOT NULL AND s.end_time <= NOW() THEN 'ENDED'
                        WHEN COALESCE(sku_state.has_on_sale, 0) = 1 THEN 'ON_SALE'
                        WHEN COALESCE(sku_state.has_presale, 0) = 1 THEN 'PRESALE'
                        WHEN COALESCE(sku_state.has_sold_out, 0) = 1 THEN 'SOLD_OUT'
                        WHEN COALESCE(sku_state.has_visible, 0) = 0 THEN 'OFFLINE'
                        ELSE s.session_status
                    END
                """);
    }

    private int expireEndedSessionTickets() {
        return jdbcTemplate.update("""
                UPDATE electronic_ticket et
                JOIN ticket_order o ON o.order_id = et.order_id
                JOIN performance_session s ON s.session_id = o.session_id
                SET et.ticket_status = 'EXPIRED',
                    et.credential_payload = NULL,
                    et.qr_code_value = NULL,
                    et.credential_expire_time = NULL,
                    et.expire_time = COALESCE(et.expire_time, NOW()),
                    et.update_time = NOW()
                WHERE s.end_time IS NOT NULL
                  AND s.end_time <= NOW()
                  AND et.ticket_status = 'UNUSED'
                """);
    }

    private int finishEndedOrders() {
        return jdbcTemplate.update("""
                UPDATE ticket_order o
                JOIN performance_session s ON s.session_id = o.session_id
                SET o.order_status = 'FINISHED',
                    o.finish_time = NOW(),
                    o.update_time = NOW()
                WHERE o.order_status = 'WAIT_USE'
                  AND s.end_time IS NOT NULL
                  AND s.end_time <= NOW()
                  AND (
                        o.delivery_type = 'PAPER_TICKET'
                     OR (
                            EXISTS (
                                SELECT 1
                                FROM electronic_ticket et_exists
                                WHERE et_exists.order_id = o.order_id
                            )
                        AND NOT EXISTS (
                                SELECT 1
                                FROM electronic_ticket et_open
                                WHERE et_open.order_id = o.order_id
                                  AND et_open.ticket_status NOT IN ('CHECKED', 'EXPIRED')
                            )
                        )
                  )
                """);
    }

}
