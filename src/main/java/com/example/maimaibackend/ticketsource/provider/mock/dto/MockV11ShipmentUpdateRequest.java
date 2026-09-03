package com.example.maimaibackend.ticketsource.provider.mock.dto;

import com.example.maimaibackend.ticketsource.provider.enums.ShipmentStatus;
import java.time.OffsetDateTime;

public record MockV11ShipmentUpdateRequest(
        ShipmentStatus shipmentStatus,
        String carrierCode,
        String carrierName,
        String waybillNo,
        OffsetDateTime shippedAt,
        OffsetDateTime signedAt,
        String trackingUrl
) {}
