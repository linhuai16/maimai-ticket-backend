package com.example.maimaibackend.ticketsource.workflow;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "maimai.ticket-source.workflow")
public class TicketSourceWorkflowProperties {
    private boolean scanEnabled = false;
    private long scanDelayMs = 30000;
    private int pollIntervalSeconds = 15;
    private int maxRetryCount = 5;
    private boolean localMockCallbackSignatureRequired = false;
    private String callbackSecret = "LOCAL_MOCK_V12_CALLBACK_TEST_SECRET";
    private int callbackWindowSeconds = 300;

    public boolean isScanEnabled() { return scanEnabled; }
    public void setScanEnabled(boolean scanEnabled) { this.scanEnabled = scanEnabled; }
    public long getScanDelayMs() { return scanDelayMs; }
    public void setScanDelayMs(long scanDelayMs) { this.scanDelayMs = scanDelayMs; }
    public int getPollIntervalSeconds() { return pollIntervalSeconds; }
    public void setPollIntervalSeconds(int pollIntervalSeconds) { this.pollIntervalSeconds = pollIntervalSeconds; }
    public int getMaxRetryCount() { return maxRetryCount; }
    public void setMaxRetryCount(int maxRetryCount) { this.maxRetryCount = maxRetryCount; }
    public boolean isLocalMockCallbackSignatureRequired() { return localMockCallbackSignatureRequired; }
    public void setLocalMockCallbackSignatureRequired(boolean localMockCallbackSignatureRequired) { this.localMockCallbackSignatureRequired = localMockCallbackSignatureRequired; }
    public String getCallbackSecret() { return callbackSecret; }
    public void setCallbackSecret(String callbackSecret) { this.callbackSecret = callbackSecret; }
    public int getCallbackWindowSeconds() { return callbackWindowSeconds; }
    public void setCallbackWindowSeconds(int callbackWindowSeconds) { this.callbackWindowSeconds = callbackWindowSeconds; }
}
