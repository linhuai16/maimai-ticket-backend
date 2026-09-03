package com.example.maimaibackend.ticketsource.fulfillment.shipment.model;

import com.example.maimaibackend.ticketsource.provider.enums.ShipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record V11ShipmentView(
        Long orderId,
        String orderNo,
        String deliveryType,
        BigDecimal deliveryFeeAmount,
        ShipmentStatus shipmentStatus,
        String carrierCode,
        String carrierName,
        String waybillNo,
        String trackingUrl,
        LocalDateTime shippedTime,
        LocalDateTime signedTime,
        LocalDateTime lastSyncTime,
        boolean providerSyncSupported,
        boolean deliveryFeeRefundableHint,
        String deliveryFeeRefundHint,
        String lastSyncStatus,
        List<String> warnings
) {
    public V11ShipmentView {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
