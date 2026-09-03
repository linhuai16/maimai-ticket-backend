package com.example.maimaibackend.ticketsource.order;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "maimai.ticket-source.order",
        name = "expire-scan-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class TicketSourceOrderExpireScheduler {
    private final TicketSourceOrderBridgeService bridgeService;

    public TicketSourceOrderExpireScheduler(TicketSourceOrderBridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    @Scheduled(fixedDelayString = "${maimai.ticket-source.order.expire-scan-delay-ms:60000}")
    public void expireDueOrders() {
        bridgeService.expireDue(100);
    }
}
