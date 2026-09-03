package com.example.maimaibackend.vo.search;

public class ClearSearchHistoryResponse {

    private boolean success;

    public ClearSearchHistoryResponse() {
    }

    public ClearSearchHistoryResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
