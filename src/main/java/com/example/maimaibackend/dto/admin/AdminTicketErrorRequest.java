package com.example.maimaibackend.dto.admin;

public class AdminTicketErrorRequest {
    private String abnormalReason;

    public AdminTicketErrorRequest() {
    }

    public String getAbnormalReason() {
        return abnormalReason;
    }

    public void setAbnormalReason(String abnormalReason) {
        this.abnormalReason = abnormalReason;
    }
}
