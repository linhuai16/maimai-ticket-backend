package com.example.maimaibackend.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "maimai.notification.push")
public class PushProperties {
    private boolean enabled;
    private boolean testMessage = true;
    private String projectId;
    private String serviceAccountFile;
    private String category = "MARKETING";
    private String action = "com.example.gomaimai.action.NOTIFICATION";
    private int ttlSeconds = 86400;
    private long scanDelayMs = 30000;
    private long deliveryDelayMs = 10000;
    private int maxRetryCount = 5;
    private String publicBaseUrl = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isTestMessage() { return testMessage; }
    public void setTestMessage(boolean testMessage) { this.testMessage = testMessage; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getServiceAccountFile() { return serviceAccountFile; }
    public void setServiceAccountFile(String serviceAccountFile) { this.serviceAccountFile = serviceAccountFile; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public int getTtlSeconds() { return ttlSeconds; }
    public void setTtlSeconds(int ttlSeconds) { this.ttlSeconds = ttlSeconds; }
    public long getScanDelayMs() { return scanDelayMs; }
    public void setScanDelayMs(long scanDelayMs) { this.scanDelayMs = scanDelayMs; }
    public long getDeliveryDelayMs() { return deliveryDelayMs; }
    public void setDeliveryDelayMs(long deliveryDelayMs) { this.deliveryDelayMs = deliveryDelayMs; }
    public int getMaxRetryCount() { return maxRetryCount; }
    public void setMaxRetryCount(int maxRetryCount) { this.maxRetryCount = maxRetryCount; }
    public String getPublicBaseUrl() { return publicBaseUrl; }
    public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
}
