package com.example.maimaibackend.ticketsource.provider.model;

import com.example.maimaibackend.ticketsource.provider.enums.ShipmentStatus;
import java.time.OffsetDateTime;

public record ProviderShipment(
        ShipmentStatus shipmentStatus,
        String carrierCode,
        String carrierName,
        String waybillNo,
        OffsetDateTime shippedAt,
        OffsetDateTime signedAt,
        String trackingUrl,
        OffsetDateTime lastSyncAt,
        String version
) {
    public ProviderShipment {
        if (shipmentStatus == null) throw new IllegalArgumentException("shipmentStatus不能为空");
    }
}
