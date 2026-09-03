package com.example.maimaibackend.ticketsource.mock.model;

import java.time.LocalDateTime;

public class MockTicketSourceDelivery {
    private String providerOrderId;
    private String deliveryStatus;
    private String issueMode;
    private String seatMode;
    private String credentialType;
    private Integer failTicketIndex;
    private LocalDateTime availableTime;
    private String requestIdempotencyKey;
    private Integer expectedTicketCount;
    private Integer issuedCount;
    private Integer failedCount;
    private String lastErrorCode;
    private String lastErrorMessage;
    private String dataVersion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public String getIssueMode() { return issueMode; }
    public void setIssueMode(String issueMode) { this.issueMode = issueMode; }
    public String getSeatMode() { return seatMode; }
    public void setSeatMode(String seatMode) { this.seatMode = seatMode; }
    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }
    public Integer getFailTicketIndex() { return failTicketIndex; }
    public void setFailTicketIndex(Integer failTicketIndex) { this.failTicketIndex = failTicketIndex; }
    public LocalDateTime getAvailableTime() { return availableTime; }
    public void setAvailableTime(LocalDateTime availableTime) { this.availableTime = availableTime; }
    public String getRequestIdempotencyKey() { return requestIdempotencyKey; }
    public void setRequestIdempotencyKey(String requestIdempotencyKey) { this.requestIdempotencyKey = requestIdempotencyKey; }
    public Integer getExpectedTicketCount() { return expectedTicketCount; }
    public void setExpectedTicketCount(Integer expectedTicketCount) { this.expectedTicketCount = expectedTicketCount; }
    public Integer getIssuedCount() { return issuedCount; }
    public void setIssuedCount(Integer issuedCount) { this.issuedCount = issuedCount; }
    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
    public String getLastErrorCode() { return lastErrorCode; }
    public void setLastErrorCode(String lastErrorCode) { this.lastErrorCode = lastErrorCode; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
