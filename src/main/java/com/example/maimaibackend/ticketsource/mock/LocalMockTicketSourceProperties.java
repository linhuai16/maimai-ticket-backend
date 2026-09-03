package com.example.maimaibackend.ticketsource.mock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "maimai.ticket-source.mock")
public class LocalMockTicketSourceProperties {
    private boolean apiEnabled = true;

    public boolean isApiEnabled() {
        return apiEnabled;
    }

    public void setApiEnabled(boolean apiEnabled) {
        this.apiEnabled = apiEnabled;
    }
}
