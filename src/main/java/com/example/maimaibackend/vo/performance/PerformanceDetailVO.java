package com.example.maimaibackend.vo.performance;

import java.util.List;

public class PerformanceDetailVO {
    private ProjectDetailVO project;
    private List<SessionItemVO> sessions;
    private SessionItemVO selectedSession;
    private VenueVO venue;
    private List<ServiceTagVO> serviceTags;
    private List<NoticeItemVO> noticeItems;

    public ProjectDetailVO getProject() { return project; }
    public void setProject(ProjectDetailVO project) { this.project = project; }
    public List<SessionItemVO> getSessions() { return sessions; }
    public void setSessions(List<SessionItemVO> sessions) { this.sessions = sessions; }
    public SessionItemVO getSelectedSession() { return selectedSession; }
    public void setSelectedSession(SessionItemVO selectedSession) { this.selectedSession = selectedSession; }
    public VenueVO getVenue() { return venue; }
    public void setVenue(VenueVO venue) { this.venue = venue; }
    public List<ServiceTagVO> getServiceTags() { return serviceTags; }
    public void setServiceTags(List<ServiceTagVO> serviceTags) { this.serviceTags = serviceTags; }
    public List<NoticeItemVO> getNoticeItems() { return noticeItems; }
    public void setNoticeItems(List<NoticeItemVO> noticeItems) { this.noticeItems = noticeItems; }
}
