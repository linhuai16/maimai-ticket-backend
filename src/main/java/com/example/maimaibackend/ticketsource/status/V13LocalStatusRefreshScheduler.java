package com.example.maimaibackend.ticketsource.status;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 周期性刷新本地展示状态。 */
@Component
public class V13LocalStatusRefreshScheduler {
    private final V13LocalStatusRecalculateService service;

    public V13LocalStatusRefreshScheduler(V13LocalStatusRecalculateService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${maimai.ticket-source.v13.status-refresh-delay-ms:300000}")
    public void run() {
        service.recalculateAll();
    }
}
