package com.example.maimaibackend.vo.user;

public class SendSmsCodeResponse {
    private boolean success;

    public SendSmsCodeResponse() {
    }

    public SendSmsCodeResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
