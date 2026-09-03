package com.example.maimaibackend.vo.user;

public class UpdateUserProfileResponse {
    private boolean success;
    private String message;
    private MineUserVO user;

    public UpdateUserProfileResponse() {
    }

    public UpdateUserProfileResponse(boolean success, String message, MineUserVO user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public MineUserVO getUser() { return user; }
    public void setUser(MineUserVO user) { this.user = user; }
}
