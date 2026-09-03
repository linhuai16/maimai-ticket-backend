package com.example.maimaibackend.ticketsource.mock.model;

import java.time.LocalDateTime;

public class MockTicketSourceCredential {
    private String providerTicketId;
    private String providerOrderId;
    private Integer ticketIndex;
    private String ticketStatus;
    private String credentialType;
    private String credentialPayload;
    private String credentialVersion;
    private String seatZone;
    private String seatRow;
    private String seatNumber;
    private String entranceInfo;
    private LocalDateTime issueTime;
    private LocalDateTime expireTime;
    private String errorCode;
    private String errorMessage;
    private String dataVersion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getProviderTicketId() { return providerTicketId; }
    public void setProviderTicketId(String providerTicketId) { this.providerTicketId = providerTicketId; }
    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public Integer getTicketIndex() { return ticketIndex; }
    public void setTicketIndex(Integer ticketIndex) { this.ticketIndex = ticketIndex; }
    public String getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(String ticketStatus) { this.ticketStatus = ticketStatus; }
    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }
    public String getCredentialPayload() { return credentialPayload; }
    public void setCredentialPayload(String credentialPayload) { this.credentialPayload = credentialPayload; }
    public String getCredentialVersion() { return credentialVersion; }
    public void setCredentialVersion(String credentialVersion) { this.credentialVersion = credentialVersion; }
    public String getSeatZone() { return seatZone; }
    public void setSeatZone(String seatZone) { this.seatZone = seatZone; }
    public String getSeatRow() { return seatRow; }
    public void setSeatRow(String seatRow) { this.seatRow = seatRow; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getEntranceInfo() { return entranceInfo; }
    public void setEntranceInfo(String entranceInfo) { this.entranceInfo = entranceInfo; }
    public LocalDateTime getIssueTime() { return issueTime; }
    public void setIssueTime(LocalDateTime issueTime) { this.issueTime = issueTime; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
