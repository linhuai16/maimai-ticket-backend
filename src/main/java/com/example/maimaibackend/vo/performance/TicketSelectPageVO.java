package com.example.maimaibackend.vo.performance;

import java.util.List;

public class TicketSelectPageVO {
    private ProjectDetailVO project;
    private SessionItemVO session;
    private VenueVO venue;
    private List<TicketSkuVO> ticketSkus;

    public ProjectDetailVO getProject() { return project; }
    public void setProject(ProjectDetailVO project) { this.project = project; }
    public SessionItemVO getSession() { return session; }
    public void setSession(SessionItemVO session) { this.session = session; }
    public VenueVO getVenue() { return venue; }
    public void setVenue(VenueVO venue) { this.venue = venue; }
    public List<TicketSkuVO> getTicketSkus() { return ticketSkus; }
    public void setTicketSkus(List<TicketSkuVO> ticketSkus) { this.ticketSkus = ticketSkus; }
}
