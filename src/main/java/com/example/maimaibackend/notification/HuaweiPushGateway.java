package com.example.maimaibackend.notification;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class HuaweiPushGateway {
    private final PushProperties properties;
    private final ServiceAccountJwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public HuaweiPushGateway(PushProperties properties, ServiceAccountJwtProvider jwtProvider, ObjectMapper objectMapper) {
        this.properties = properties;
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return properties.isEnabled() && present(properties.getProjectId()) && present(properties.getServiceAccountFile());
    }

    public PushResult send(String token, String title, String content, Map<String, Object> data) {
        if (!isConfigured()) return new PushResult(false, false, "CONFIG_MISSING", "Push配置未启用", null);
        try {
            Map<String, Object> clickAction = new LinkedHashMap<>();
            clickAction.put("actionType", 1);
            clickAction.put("action", properties.getAction());
            clickAction.put("data", data);
            Map<String, Object> notification = new LinkedHashMap<>();
            notification.put("category", properties.getCategory());
            notification.put("title", title);
            notification.put("body", content);
            notification.put("clickAction", clickAction);
            notification.put("foregroundShow", true);
            Map<String, Object> payload = Map.of("notification", notification);
            Map<String, Object> target = Map.of("token", List.of(token));
            Map<String, Object> options = new LinkedHashMap<>();
            options.put("testMessage", properties.isTestMessage());
            options.put("ttl", Math.max(60, properties.getTtlSeconds()));
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("payload", payload);
            requestBody.put("target", target);
            requestBody.put("pushOptions", options);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://push-api.cloud.huawei.com/v3/" + properties.getProjectId() + "/messages:send"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwtProvider.getToken())
                    .header("push-type", "0")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode body = response.body() == null || response.body().isBlank()
                    ? objectMapper.createObjectNode() : objectMapper.readTree(response.body());
            String code = body.path("code").asText("HTTP_" + response.statusCode());
            String message = body.path("msg").asText("Push服务响应异常");
            String requestId = body.path("requestId").asText(null);
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300 && "80000000".equals(code);
            boolean invalid = "80300007".equals(code) || "80300028".equals(code)
                    || ("80100000".equals(code) && message.contains(token));
            return new PushResult(success, invalid, code, message, requestId);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return new PushResult(false, false, "INTERRUPTED", "Push请求已中断", null);
        } catch (Exception ex) {
            return new PushResult(false, false, "NETWORK_ERROR", safe(ex.getMessage()), null);
        }
    }

    private boolean present(String value) { return value != null && !value.isBlank(); }
    private String safe(String value) { return value == null ? "Push请求失败" : value.substring(0, Math.min(450, value.length())); }

    public record PushResult(boolean success, boolean invalidToken, String code, String message, String requestId) {}
}
