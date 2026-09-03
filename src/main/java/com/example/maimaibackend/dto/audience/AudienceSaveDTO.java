package com.example.maimaibackend.dto.audience;

public class AudienceSaveDTO {
    private Long audienceId;
    private Long userId;
    private String realName;
    private String certificateType;
    private String certificateNo;
    private String certificateNoHash;
    private String phone;
    private Boolean isDefault;

    public Long getAudienceId() { return audienceId; }
    public void setAudienceId(Long audienceId) { this.audienceId = audienceId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getCertificateType() { return certificateType; }
    public void setCertificateType(String certificateType) { this.certificateType = certificateType; }
    public String getCertificateNo() { return certificateNo; }
    public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
    public String getCertificateNoHash() { return certificateNoHash; }
    public void setCertificateNoHash(String certificateNoHash) { this.certificateNoHash = certificateNoHash; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
