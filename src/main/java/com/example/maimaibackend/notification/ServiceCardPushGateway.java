package com.example.maimaibackend.notification;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServiceCardPushGateway {
    private final PushProperties properties;
    private final ServiceAccountJwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public ServiceCardPushGateway(PushProperties properties, ServiceAccountJwtProvider jwtProvider,
                                  ObjectMapper objectMapper) {
        this.properties = properties;
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
    }

    public HuaweiPushGateway.PushResult sendCardRefresh(String token, String formId, String moduleName,
                                                         String abilityName, String formName, long version,
                                                         Map<String, Object> formData) {
        if (!isConfigured()) {
            return new HuaweiPushGateway.PushResult(false, false, "CONFIG_MISSING", "Push配置未启用", null);
        }
        try {
            Map<String, Object> requestBody = buildRequestBody(token, formId, moduleName, abilityName, formName,
                    version, formData);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://push-api.cloud.huawei.com/v3/" + properties.getProjectId() + "/messages:send"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwtProvider.getToken())
                    .header("push-type", "1")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode body = response.body() == null || response.body().isBlank()
                    ? objectMapper.createObjectNode() : objectMapper.readTree(response.body());
            String code = body.path("code").asText("HTTP_" + response.statusCode());
            String message = body.path("msg").asText("卡片Push服务响应异常");
            String requestId = body.path("requestId").asText(null);
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300 && "80000000".equals(code);
            boolean invalid = "80300007".equals(code) || "80300028".equals(code);
            return new HuaweiPushGateway.PushResult(success, invalid, code, message, requestId);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return new HuaweiPushGateway.PushResult(false, false, "INTERRUPTED", "卡片Push请求已中断", null);
        } catch (Exception ex) {
            return new HuaweiPushGateway.PushResult(false, false, "NETWORK_ERROR", safe(ex.getMessage()), null);
        }
    }

    Map<String, Object> buildRequestBody(String token, String formId, String moduleName, String abilityName,
                                         String formName, long version, Map<String, Object> formData) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("moduleName", moduleName);
        payload.put("abilityName", abilityName);
        payload.put("formName", formName);
        payload.put("formId", new BigInteger(formId));
        payload.put("version", version);
        payload.put("formData", formData);
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("payload", payload);
        requestBody.put("target", Map.of("token", List.of(token)));
        requestBody.put("pushOptions", Map.of("testMessage", properties.isTestMessage()));
        return requestBody;
    }

    private boolean isConfigured() {
        return properties.isEnabled() && present(properties.getProjectId()) && present(properties.getServiceAccountFile());
    }

    private boolean present(String value) {
        return value != null && !value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "卡片Push请求失败" : value.substring(0, Math.min(450, value.length()));
    }
}
