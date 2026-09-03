package com.example.maimaibackend.vo.admin;

public class AdminOrderAudienceVO {
    private Long orderAudienceId;
    private String realName;
    private String certificateType;
    private String certificateNo;
    private String phone;

    public AdminOrderAudienceVO() {
    }

    public Long getOrderAudienceId() {
        return orderAudienceId;
    }

    public void setOrderAudienceId(Long orderAudienceId) {
        this.orderAudienceId = orderAudienceId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}