package com.example.maimaibackend.vo.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 用户侧电子票安全 VO：不包含 providerOrderId/providerTicketId/原始证件号。 */
public class ElectronicTicketVO {
    private Long ticketId;
    private String ticketNo;
    private Long orderId;
    private Long orderItemId;
    private Long orderAudienceId;
    private String ticketStatus;
    private String credentialType;
    private String dynamicQrMode;
    private String credentialVersion;
    private LocalDateTime credentialExpireTime;
    private Integer refreshAfterSeconds;
    /** 静态凭证的用户可见值；DYNAMIC_QR 永远为 null。 */
    private String credentialDisplayValue;
    private String qrCodeValue;
    private String seatInfo;
    private String entranceInfo;
    private LocalDateTime generateTime;
    private LocalDateTime checkTime;
    private LocalDateTime expireTime;
    private String abnormalReason;
    private Long skuId;
    private String skuName;
    private BigDecimal unitPrice;
    private String realName;
    private String certificateType;
    private String maskedCertificateNo;

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public String getTicketNo() { return ticketNo; }
    public void setTicketNo(String ticketNo) { this.ticketNo = ticketNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public Long getOrderAudienceId() { return orderAudienceId; }
    public void setOrderAudienceId(Long orderAudienceId) { this.orderAudienceId = orderAudienceId; }
    public String getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(String ticketStatus) { this.ticketStatus = ticketStatus; }
    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }
    public String getDynamicQrMode() { return dynamicQrMode; }
    public void setDynamicQrMode(String dynamicQrMode) { this.dynamicQrMode = dynamicQrMode; }
    public String getCredentialVersion() { return credentialVersion; }
    public void setCredentialVersion(String credentialVersion) { this.credentialVersion = credentialVersion; }
    public LocalDateTime getCredentialExpireTime() { return credentialExpireTime; }
    public void setCredentialExpireTime(LocalDateTime credentialExpireTime) { this.credentialExpireTime = credentialExpireTime; }
    public Integer getRefreshAfterSeconds() { return refreshAfterSeconds; }
    public void setRefreshAfterSeconds(Integer refreshAfterSeconds) { this.refreshAfterSeconds = refreshAfterSeconds; }
    public String getCredentialDisplayValue() { return credentialDisplayValue; }
    public void setCredentialDisplayValue(String credentialDisplayValue) { this.credentialDisplayValue = credentialDisplayValue; }
    public String getQrCodeValue() { return qrCodeValue; }
    public void setQrCodeValue(String qrCodeValue) { this.qrCodeValue = qrCodeValue; }
    public String getSeatInfo() { return seatInfo; }
    public void setSeatInfo(String seatInfo) { this.seatInfo = seatInfo; }
    public String getEntranceInfo() { return entranceInfo; }
    public void setEntranceInfo(String entranceInfo) { this.entranceInfo = entranceInfo; }
    public LocalDateTime getGenerateTime() { return generateTime; }
    public void setGenerateTime(LocalDateTime generateTime) { this.generateTime = generateTime; }
    public LocalDateTime getCheckTime() { return checkTime; }
    public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public String getAbnormalReason() { return abnormalReason; }
    public void setAbnormalReason(String abnormalReason) { this.abnormalReason = abnormalReason; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getCertificateType() { return certificateType; }
    public void setCertificateType(String certificateType) { this.certificateType = certificateType; }
    public String getMaskedCertificateNo() { return maskedCertificateNo; }
    public void setMaskedCertificateNo(String maskedCertificateNo) { this.maskedCertificateNo = maskedCertificateNo; }
}
