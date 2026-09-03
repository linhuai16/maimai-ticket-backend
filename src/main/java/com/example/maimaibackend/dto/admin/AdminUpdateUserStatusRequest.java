package com.example.maimaibackend.dto.admin;

public class AdminUpdateUserStatusRequest {
    private String accountStatus;

    public AdminUpdateUserStatusRequest() {
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}