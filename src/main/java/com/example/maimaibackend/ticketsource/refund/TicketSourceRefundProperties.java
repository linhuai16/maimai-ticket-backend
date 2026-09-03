package com.example.maimaibackend.ticketsource.refund;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "maimai.ticket-source.refund")
public class TicketSourceRefundProperties {
    private boolean scanEnabled = true;
    private long scanDelayMs = 60000;
    private int pollIntervalSeconds = 15;
    private int retryBaseSeconds = 10;
    private int maxRetryCount = 5;

    public boolean isScanEnabled() { return scanEnabled; }
    public void setScanEnabled(boolean scanEnabled) { this.scanEnabled = scanEnabled; }
    public long getScanDelayMs() { return scanDelayMs; }
    public void setScanDelayMs(long scanDelayMs) { this.scanDelayMs = scanDelayMs; }
    public int getPollIntervalSeconds() { return pollIntervalSeconds; }
    public void setPollIntervalSeconds(int pollIntervalSeconds) { this.pollIntervalSeconds = pollIntervalSeconds; }
    public int getRetryBaseSeconds() { return retryBaseSeconds; }
    public void setRetryBaseSeconds(int retryBaseSeconds) { this.retryBaseSeconds = retryBaseSeconds; }
    public int getMaxRetryCount() { return maxRetryCount; }
    public void setMaxRetryCount(int maxRetryCount) { this.maxRetryCount = maxRetryCount; }
}
