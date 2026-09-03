package com.example.maimaibackend.vo.want;

public class WantActionResponse {
    private boolean success;
    private Boolean isWanted;

    public WantActionResponse() {
    }

    public WantActionResponse(boolean success, Boolean isWanted) {
        this.success = success;
        this.isWanted = isWanted;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public Boolean getIsWanted() { return isWanted; }
    public void setIsWanted(Boolean isWanted) { this.isWanted = isWanted; }
}
