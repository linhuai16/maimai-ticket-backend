package com.example.maimaibackend.ticketsource.issue;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "maimai.ticket-source.issue", name = "scan-enabled", havingValue = "true", matchIfMissing = true)
public class TicketSourceIssueScheduler {
    private final TicketSourceIssueService service;

    public TicketSourceIssueScheduler(TicketSourceIssueService service) { this.service = service; }

    @Scheduled(fixedDelayString = "${maimai.ticket-source.issue.scan-delay-ms:30000}")
    public void processDueTasks() { service.processDue(100); }
}
