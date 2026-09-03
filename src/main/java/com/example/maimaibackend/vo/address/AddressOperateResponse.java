package com.example.maimaibackend.vo.address;

public class AddressOperateResponse {
    private boolean success;

    public AddressOperateResponse() {
    }

    public AddressOperateResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
