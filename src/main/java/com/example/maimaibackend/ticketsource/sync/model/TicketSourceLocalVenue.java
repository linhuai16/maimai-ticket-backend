package com.example.maimaibackend.ticketsource.sync.model;

public class TicketSourceLocalVenue {
    private Long venueId;
    private String venueName;
    private String cityName;
    private String address;

    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
