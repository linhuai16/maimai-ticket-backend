package com.example.maimaibackend.vo.search;

public class SaveSearchHistoryResponse {

    private boolean success;

    public SaveSearchHistoryResponse() {
    }

    public SaveSearchHistoryResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
