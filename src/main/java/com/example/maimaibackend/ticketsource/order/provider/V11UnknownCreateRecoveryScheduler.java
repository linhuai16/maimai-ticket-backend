package com.example.maimaibackend.ticketsource.order.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** R5：周期补查 Provider createOrder UNKNOWN_RESULT；永远只查询，不再次创建。 */
@Component
@ConditionalOnProperty(prefix = "maimai.ticket-source.v11", name = "unknown-create-auto-recovery-enabled",
        havingValue = "true", matchIfMissing = true)
public class V11UnknownCreateRecoveryScheduler {
    private final V11OrderService orderService;

    public V11UnknownCreateRecoveryScheduler(V11OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelayString = "${maimai.ticket-source.v11.unknown-create-scan-delay-ms:300000}")
    public void recoverUnknownCreateOrders() {
        try {
            orderService.recoverUnknownCreates(50);
        } catch (RuntimeException ignored) {
            // 单轮扫描失败不应阻断调度线程；每单失败事实已写入 bridge / Gateway Log。
        }
    }
}
