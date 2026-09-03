package com.example.maimaibackend.vo.search;

public class DeleteSearchHistoryResponse {

    private boolean success;

    public DeleteSearchHistoryResponse() {
    }

    public DeleteSearchHistoryResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
