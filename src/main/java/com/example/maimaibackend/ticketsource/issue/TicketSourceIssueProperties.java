package com.example.maimaibackend.ticketsource.issue;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "maimai.ticket-source.issue")
public class TicketSourceIssueProperties {
    private boolean scanEnabled = true;
    private long scanDelayMs = 30000;
    private int initialDelaySeconds = 120;
    private int pollIntervalSeconds = 15;
    private int retryBaseSeconds = 10;
    private int maxRetryCount = 5;
    private int processingTimeoutSeconds = 300;

    public boolean isScanEnabled() { return scanEnabled; }
    public void setScanEnabled(boolean scanEnabled) { this.scanEnabled = scanEnabled; }
    public long getScanDelayMs() { return scanDelayMs; }
    public void setScanDelayMs(long scanDelayMs) { this.scanDelayMs = scanDelayMs; }
    public int getInitialDelaySeconds() { return initialDelaySeconds; }
    public void setInitialDelaySeconds(int initialDelaySeconds) { this.initialDelaySeconds = initialDelaySeconds; }
    public int getPollIntervalSeconds() { return pollIntervalSeconds; }
    public void setPollIntervalSeconds(int pollIntervalSeconds) { this.pollIntervalSeconds = pollIntervalSeconds; }
    public int getRetryBaseSeconds() { return retryBaseSeconds; }
    public void setRetryBaseSeconds(int retryBaseSeconds) { this.retryBaseSeconds = retryBaseSeconds; }
    public int getMaxRetryCount() { return maxRetryCount; }
    public void setMaxRetryCount(int maxRetryCount) { this.maxRetryCount = maxRetryCount; }
    public int getProcessingTimeoutSeconds() { return processingTimeoutSeconds; }
    public void setProcessingTimeoutSeconds(int processingTimeoutSeconds) { this.processingTimeoutSeconds = processingTimeoutSeconds; }
}
