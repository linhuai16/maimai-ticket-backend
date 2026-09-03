package com.example.maimaibackend.vo.audience;

public class AudienceOperateResponse {
    private boolean success;

    public AudienceOperateResponse() {
    }

    public AudienceOperateResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
