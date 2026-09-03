package com.example.maimaibackend.ticketsource.gateway.model;

public class TicketSourceOrderAudience {
    private String realName;
    private String certificateType;
    private String certificateNo;
    private String phone;

    public TicketSourceOrderAudience() {}

    public TicketSourceOrderAudience(String realName, String certificateType, String certificateNo, String phone) {
        this.realName = realName;
        this.certificateType = certificateType;
        this.certificateNo = certificateNo;
        this.phone = phone;
    }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getCertificateType() { return certificateType; }
    public void setCertificateType(String certificateType) { this.certificateType = certificateType; }
    public String getCertificateNo() { return certificateNo; }
    public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
