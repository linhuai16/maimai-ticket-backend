package com.example.maimaibackend.ticketsource.sync.model;

import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSku;

import java.util.ArrayList;
import java.util.List;

public class TicketSourceProjectBundle {
    private TicketSourceProject project;
    private List<SessionBundle> sessions = new ArrayList<>();

    public TicketSourceProject getProject() { return project; }
    public void setProject(TicketSourceProject project) { this.project = project; }
    public List<SessionBundle> getSessions() { return sessions; }
    public void setSessions(List<SessionBundle> sessions) {
        this.sessions = sessions == null ? new ArrayList<>() : sessions;
    }

    public int sessionCount() {
        return sessions == null ? 0 : sessions.size();
    }

    public int skuCount() {
        if (sessions == null) {
            return 0;
        }
        return sessions.stream().mapToInt(item -> item.getSkus() == null ? 0 : item.getSkus().size()).sum();
    }

    public static class SessionBundle {
        private TicketSourceSession session;
        private List<TicketSourceSku> skus = new ArrayList<>();

        public TicketSourceSession getSession() { return session; }
        public void setSession(TicketSourceSession session) { this.session = session; }
        public List<TicketSourceSku> getSkus() { return skus; }
        public void setSkus(List<TicketSourceSku> skus) {
            this.skus = skus == null ? new ArrayList<>() : skus;
        }
    }
}
