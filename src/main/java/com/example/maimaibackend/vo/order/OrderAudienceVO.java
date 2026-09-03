package com.example.maimaibackend.vo.order;

public class OrderAudienceVO {

    private Long orderAudienceId;
    private Long orderId;
    private String realName;
    private String certificateType;
    private String certificateNo;
    private String phone;

    public Long getOrderAudienceId() {
        return orderAudienceId;
    }

    public void setOrderAudienceId(Long orderAudienceId) {
        this.orderAudienceId = orderAudienceId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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
