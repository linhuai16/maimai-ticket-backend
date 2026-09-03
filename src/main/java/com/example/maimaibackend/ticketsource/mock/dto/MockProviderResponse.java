package com.example.maimaibackend.ticketsource.mock.dto;

import java.time.LocalDateTime;

/**
 * 本地模拟器暴露的“外部系统响应”，刻意不复用平台 Result。
 */
public class MockProviderResponse<T> {
    private String code;
    private String message;
    private String requestId;
    private T data;
    private LocalDateTime responseTime;

    public static <T> MockProviderResponse<T> success(String requestId, T data) {
        MockProviderResponse<T> response = new MockProviderResponse<>();
        response.code = "MOCK_OK";
        response.message = "success";
        response.requestId = requestId;
        response.data = data;
        response.responseTime = LocalDateTime.now();
        return response;
    }

    public static <T> MockProviderResponse<T> failure(String requestId, String code, String message) {
        MockProviderResponse<T> response = new MockProviderResponse<>();
        response.code = code;
        response.message = message;
        response.requestId = requestId;
        response.responseTime = LocalDateTime.now();
        return response;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public LocalDateTime getResponseTime() { return responseTime; }
    public void setResponseTime(LocalDateTime responseTime) { this.responseTime = responseTime; }
}
