package com.example.maimaibackend.ai.infrastructure.llm;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;

@Component
public class OpenAiCompatibleChatProvider implements AiChatProvider {
    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiCompatibleChatProvider(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(1, properties.getConnectTimeoutSeconds())))
                .build();
    }

    @Override
    public JsonNode complete(ArrayNode messages, ArrayNode tools) {
        properties.requireConfigured();
        ObjectNode body = baseBody(messages);
        body.set("tools", tools);
        body.put("tool_choice", "auto");
        body.put("stream", false);
        JsonNode root = sendJson(body);
        JsonNode message = root.path("choices").path(0).path("message");
        if (message.isMissingNode() || message.isNull()) {
            throw new IllegalStateException(providerMessage(root, "LLM未返回有效消息"));
        }
        ObjectNode sanitized = (ObjectNode) message.deepCopy();
        sanitized.remove("reasoning_content");
        JsonNode content = sanitized.path("content");
        if (content.isTextual()) sanitized.put("content", stripThinkBlocks(content.asText()));
        return sanitized;
    }

    @Override
    public void stream(ArrayNode messages, Consumer<String> textConsumer) {
        properties.requireConfigured();
        ObjectNode body = baseBody(messages);
        body.put("stream", true);
        HttpRequest request = requestBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(write(body), StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new IllegalStateException("LLM请求失败，HTTP " + response.statusCode() + ": " + safe(errorBody));
            }
            ThinkFilter filter = new ThinkFilter(textConsumer);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) continue;
                    String data = line.substring(5).trim();
                    if (data.isEmpty() || "[DONE]".equals(data)) continue;
                    JsonNode chunk = objectMapper.readTree(data);
                    JsonNode delta = chunk.path("choices").path(0).path("delta");
                    JsonNode content = delta.path("content");
                    if (content.isTextual() && !content.asText().isEmpty()) {
                        filter.accept(content.asText());
                    }
                }
            }
            filter.finish();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM请求已中断");
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException stateException) throw stateException;
            throw new IllegalStateException("LLM流式请求失败: " + safe(ex.getMessage()));
        }
    }

    private JsonNode sendJson(ObjectNode body) {
        HttpRequest request = requestBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(write(body), StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode root = response.body() == null || response.body().isBlank()
                    ? objectMapper.createObjectNode() : objectMapper.readTree(response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("LLM请求失败，HTTP " + response.statusCode() + ": " + providerMessage(root, safe(response.body())));
            }
            return root;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM请求已中断");
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException stateException) throw stateException;
            throw new IllegalStateException("LLM请求失败: " + safe(ex.getMessage()));
        }
    }

    private ObjectNode baseBody(ArrayNode messages) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getModel().trim());
        body.set("messages", messages);
        body.put("temperature", 0.1);
        body.put("top_p", 0.8);
        body.put("max_tokens", 384);
        if (isDashScopeCompatible()) body.put("enable_thinking", false);
        return body;
    }

    private boolean isDashScopeCompatible() {
        String baseUrl = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().toLowerCase();
        return baseUrl.contains("dashscope.aliyuncs.com") || baseUrl.contains("maas.aliyuncs.com");
    }

    private HttpRequest.Builder requestBuilder() {
        return HttpRequest.newBuilder()
                .uri(URI.create(endpoint()))
                .timeout(Duration.ofSeconds(Math.max(10, properties.getRequestTimeoutSeconds())))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("Authorization", "Bearer " + properties.getApiKey().trim());
    }

    private String endpoint() {
        String base = properties.getBaseUrl().trim();
        if (base.endsWith("/chat/completions")) return base;
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/chat/completions";
    }

    private String stripThinkBlocks(String value) {
        if (value == null || value.isEmpty()) return "";
        return value.replaceAll("(?is)<think>.*?</think>", "")
                .replace("<think>", "")
                .replace("</think>", "");
    }

    private String write(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            throw new IllegalStateException("LLM请求序列化失败");
        }
    }

    private String providerMessage(JsonNode root, String fallback) {
        String message = root.path("error").path("message").asText("");
        return message.isBlank() ? fallback : message;
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) return "未知错误";
        return value.substring(0, Math.min(500, value.length()));
    }

    private static final class ThinkFilter {
        private static final String OPEN = "<think>";
        private static final String CLOSE = "</think>";
        private final Consumer<String> consumer;
        private String carry = "";
        private boolean inThink = false;

        private ThinkFilter(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        private void accept(String chunk) {
            String value = carry + chunk;
            carry = "";
            while (!value.isEmpty()) {
                if (inThink) {
                    int closeIndex = value.toLowerCase().indexOf(CLOSE);
                    if (closeIndex >= 0) {
                        value = value.substring(closeIndex + CLOSE.length());
                        inThink = false;
                        continue;
                    }
                    carry = suffixPrefix(value, CLOSE);
                    return;
                }
                int openIndex = value.toLowerCase().indexOf(OPEN);
                if (openIndex >= 0) {
                    emit(value.substring(0, openIndex));
                    value = value.substring(openIndex + OPEN.length());
                    inThink = true;
                    continue;
                }
                String suffix = suffixPrefix(value, OPEN);
                int emitLength = value.length() - suffix.length();
                emit(value.substring(0, emitLength));
                carry = suffix;
                return;
            }
        }

        private void finish() {
            if (!inThink) emit(carry.replace("<think>", "").replace("</think>", ""));
            carry = "";
        }

        private void emit(String value) {
            if (value != null && !value.isEmpty()) consumer.accept(value);
        }

        private static String suffixPrefix(String value, String marker) {
            String lowerValue = value.toLowerCase();
            for (int length = Math.min(marker.length() - 1, value.length()); length > 0; length--) {
                if (marker.startsWith(lowerValue.substring(value.length() - length))) {
                    return value.substring(value.length() - length);
                }
            }
            return "";
        }
    }
}
