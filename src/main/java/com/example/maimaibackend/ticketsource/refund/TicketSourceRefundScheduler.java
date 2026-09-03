package com.example.maimaibackend.ticketsource.refund;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "maimai.ticket-source.refund", name = "scan-enabled", havingValue = "true", matchIfMissing = true)
public class TicketSourceRefundScheduler {
    private final TicketSourceRefundService service;

    public TicketSourceRefundScheduler(TicketSourceRefundService service) { this.service = service; }

    @Scheduled(fixedDelayString = "${maimai.ticket-source.refund.scan-delay-ms:60000}")
    public void processDueRefunds() { service.processDue(100); }
}
