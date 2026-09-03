package com.example.maimaibackend.ticketsource.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "maimai.ticket-source.gateway")
public class TicketSourceGatewayProperties {
    private int workerCount = 8;
    private int queueCapacity = 100;
    private int hardTimeoutMs = 10000;

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getHardTimeoutMs() {
        return hardTimeoutMs;
    }

    public void setHardTimeoutMs(int hardTimeoutMs) {
        this.hardTimeoutMs = hardTimeoutMs;
    }
}
