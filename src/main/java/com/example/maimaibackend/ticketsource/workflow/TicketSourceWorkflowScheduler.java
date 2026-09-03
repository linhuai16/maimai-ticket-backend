package com.example.maimaibackend.ticketsource.workflow;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 默认关闭；生产环境可按部署策略显式开启。 */
@Component
public class TicketSourceWorkflowScheduler {
    private final TicketSourceWorkflowService service;
    private final TicketSourceWorkflowProperties properties;
    public TicketSourceWorkflowScheduler(TicketSourceWorkflowService service, TicketSourceWorkflowProperties properties) {
        this.service = service; this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${maimai.ticket-source.workflow.scan-delay-ms:30000}")
    public void scan() {
        if (!properties.isScanEnabled()) return;
        service.processDueFulfillment(50);
        service.syncPendingRefunds(50);
        service.processPendingCallbacks(50);
    }
}
