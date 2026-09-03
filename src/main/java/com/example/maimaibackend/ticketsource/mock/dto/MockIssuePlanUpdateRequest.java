package com.example.maimaibackend.ticketsource.mock.dto;

public class MockIssuePlanUpdateRequest {
    private String issueMode;
    private Integer delaySeconds;
    private Integer failTicketIndex;
    private String seatMode;
    private String credentialType;

    public String getIssueMode() { return issueMode; }
    public void setIssueMode(String issueMode) { this.issueMode = issueMode; }
    public Integer getDelaySeconds() { return delaySeconds; }
    public void setDelaySeconds(Integer delaySeconds) { this.delaySeconds = delaySeconds; }
    public Integer getFailTicketIndex() { return failTicketIndex; }
    public void setFailTicketIndex(Integer failTicketIndex) { this.failTicketIndex = failTicketIndex; }
    public String getSeatMode() { return seatMode; }
    public void setSeatMode(String seatMode) { this.seatMode = seatMode; }
    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }
}
