package com.example.maimaibackend.ticketsource.provider.adapter;

/**
 * V1.1 Adapter 统一异常。
 *
 * <p>R5 增加 resultUnknown：仅用于“请求可能已经在 Provider 生效，但响应未可靠返回”的场景。
 * 这类异常绝不能按普通明确失败直接取消本地订单或允许用同一业务重新 create；
 * 必须先按商户订单号/幂等键补查。</p>
 */
public class V11AdapterException extends RuntimeException {
    private final V11ErrorCode errorCode;
    private final String sourceErrorCode;
    private final boolean retryable;
    private final boolean resultUnknown;

    public V11AdapterException(V11ErrorCode errorCode, String sourceErrorCode, String message, boolean retryable) {
        this(errorCode, sourceErrorCode, message, retryable, false);
    }

    public V11AdapterException(V11ErrorCode errorCode, String sourceErrorCode, String message,
                               boolean retryable, boolean resultUnknown) {
        super(message);
        this.errorCode = errorCode;
        this.sourceErrorCode = sourceErrorCode;
        this.retryable = retryable;
        this.resultUnknown = resultUnknown;
    }

    public V11ErrorCode getErrorCode() { return errorCode; }
    public String getSourceErrorCode() { return sourceErrorCode; }
    public boolean isRetryable() { return retryable; }
    public boolean isResultUnknown() { return resultUnknown; }
}
