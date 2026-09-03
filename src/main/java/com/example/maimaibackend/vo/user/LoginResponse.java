package com.example.maimaibackend.vo.user;

public class LoginResponse {
    private String token;
    private MineUserVO user;

    public LoginResponse() {
    }

    public LoginResponse(String token, MineUserVO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MineUserVO getUser() {
        return user;
    }

    public void setUser(MineUserVO user) {
        this.user = user;
    }
}
