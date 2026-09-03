package com.example.maimaibackend.vo.audience;

public class AudienceItemVO {
    private Long audienceId;
    private String realName;
    private String certificateType;
    private String maskedCertificateNo;
    private String maskedPhone;
    private Boolean isDefault;

    public Long getAudienceId() { return audienceId; }
    public void setAudienceId(Long audienceId) { this.audienceId = audienceId; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getCertificateType() { return certificateType; }
    public void setCertificateType(String certificateType) { this.certificateType = certificateType; }
    public String getMaskedCertificateNo() { return maskedCertificateNo; }
    public void setMaskedCertificateNo(String maskedCertificateNo) { this.maskedCertificateNo = maskedCertificateNo; }
    public String getMaskedPhone() { return maskedPhone; }
    public void setMaskedPhone(String maskedPhone) { this.maskedPhone = maskedPhone; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
