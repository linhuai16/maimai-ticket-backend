package com.example.maimaibackend.ticketsource.mock.model;

import java.time.LocalDateTime;

public class MockTicketSourceBehavior {
    private String operationCode;
    private Boolean enabled;
    private Integer delayMs;
    private String forcedErrorCode;
    private String forcedErrorMessage;
    private LocalDateTime updateTime;

    public String getOperationCode() { return operationCode; }
    public void setOperationCode(String operationCode) { this.operationCode = operationCode; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getDelayMs() { return delayMs; }
    public void setDelayMs(Integer delayMs) { this.delayMs = delayMs; }
    public String getForcedErrorCode() { return forcedErrorCode; }
    public void setForcedErrorCode(String forcedErrorCode) { this.forcedErrorCode = forcedErrorCode; }
    public String getForcedErrorMessage() { return forcedErrorMessage; }
    public void setForcedErrorMessage(String forcedErrorMessage) { this.forcedErrorMessage = forcedErrorMessage; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
