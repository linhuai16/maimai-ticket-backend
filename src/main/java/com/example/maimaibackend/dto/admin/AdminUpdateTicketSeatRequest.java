package com.example.maimaibackend.dto.admin;

public class AdminUpdateTicketSeatRequest {
    private String seatInfo;

    public AdminUpdateTicketSeatRequest() {
    }

    public String getSeatInfo() {
        return seatInfo;
    }

    public void setSeatInfo(String seatInfo) {
        this.seatInfo = seatInfo;
    }
}
