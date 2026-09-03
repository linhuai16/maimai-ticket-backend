package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.CoordinateSystem;
import java.time.OffsetDateTime;
import java.util.List;

public record ProviderVenue(
        String venueId,
        String venueName,
        String countryCode,
        String provinceCode,
        String cityCode,
        String districtCode,
        String address,
        String longitude,
        String latitude,
        CoordinateSystem coordinateSystem,
        String navigationName,
        List<String> entrances,
        String trafficNotice,
        String parkingNotice,
        String version,
        OffsetDateTime updatedAt
) {
    public ProviderVenue {
        venueId = ModelSupport.required(venueId, "venueId");
        venueName = ModelSupport.required(venueName, "venueName");
        coordinateSystem = coordinateSystem == null ? CoordinateSystem.UNKNOWN : coordinateSystem;
        entrances = ModelSupport.list(entrances);
    }
}
