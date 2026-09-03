package com.example.maimaibackend.ai.infrastructure.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "maimai.ai")
public class AiProperties {
    private boolean enabled;
    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey;
    private String model;
    private int connectTimeoutSeconds = 10;
    private int requestTimeoutSeconds = 120;
    private int maxToolRounds = 4;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getConnectTimeoutSeconds() { return connectTimeoutSeconds; }
    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) { this.connectTimeoutSeconds = connectTimeoutSeconds; }
    public int getRequestTimeoutSeconds() { return requestTimeoutSeconds; }
    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) { this.requestTimeoutSeconds = requestTimeoutSeconds; }
    public int getMaxToolRounds() { return maxToolRounds; }
    public void setMaxToolRounds(int maxToolRounds) { this.maxToolRounds = maxToolRounds; }

    public void requireConfigured() {
        if (!enabled || blank(baseUrl) || blank(apiKey) || blank(model)) {
            throw new IllegalStateException("麦麦AI尚未配置，请设置 MAIMAI_AI_ENABLED、MAIMAI_AI_BASE_URL、MAIMAI_AI_API_KEY 和 MAIMAI_AI_MODEL");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
