package com.example.maimaibackend.ticketsource.mock.dto;

public class MockBehaviorUpdateRequest {
    private Boolean enabled;
    private Integer delayMs;
    private String forcedErrorCode;
    private String forcedErrorMessage;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getDelayMs() { return delayMs; }
    public void setDelayMs(Integer delayMs) { this.delayMs = delayMs; }
    public String getForcedErrorCode() { return forcedErrorCode; }
    public void setForcedErrorCode(String forcedErrorCode) { this.forcedErrorCode = forcedErrorCode; }
    public String getForcedErrorMessage() { return forcedErrorMessage; }
    public void setForcedErrorMessage(String forcedErrorMessage) { this.forcedErrorMessage = forcedErrorMessage; }
}
