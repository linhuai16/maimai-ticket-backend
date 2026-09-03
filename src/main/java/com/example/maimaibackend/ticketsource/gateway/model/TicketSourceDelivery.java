package com.example.maimaibackend.ticketsource.gateway.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketSourceDelivery {
    private String providerOrderId;
    private String deliveryStatus;
    private Integer expectedTicketCount;
    private Integer issuedCount;
    private Integer failedCount;
    private LocalDateTime nextPollTime;
    private String dataVersion;
    private List<TicketSourceCredential> tickets = new ArrayList<>();

    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public Integer getExpectedTicketCount() { return expectedTicketCount; }
    public void setExpectedTicketCount(Integer expectedTicketCount) { this.expectedTicketCount = expectedTicketCount; }
    public Integer getIssuedCount() { return issuedCount; }
    public void setIssuedCount(Integer issuedCount) { this.issuedCount = issuedCount; }
    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
    public LocalDateTime getNextPollTime() { return nextPollTime; }
    public void setNextPollTime(LocalDateTime nextPollTime) { this.nextPollTime = nextPollTime; }
    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
    public List<TicketSourceCredential> getTickets() { return tickets; }
    public void setTickets(List<TicketSourceCredential> tickets) { this.tickets = tickets == null ? new ArrayList<>() : tickets; }
}
