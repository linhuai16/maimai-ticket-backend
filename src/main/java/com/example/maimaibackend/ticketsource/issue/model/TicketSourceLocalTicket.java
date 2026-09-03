package com.example.maimaibackend.ticketsource.issue.model;

public class TicketSourceLocalTicket {
    private Long ticketId;
    private Integer ticketIndex;
    private String ticketStatus;

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public Integer getTicketIndex() { return ticketIndex; }
    public void setTicketIndex(Integer ticketIndex) { this.ticketIndex = ticketIndex; }
    public String getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(String ticketStatus) { this.ticketStatus = ticketStatus; }
}
