package com.example.maimaibackend.ticketsource.user.model;

import com.example.maimaibackend.ticketsource.provider.enums.ShipmentStatus;
import com.example.maimaibackend.ticketsource.provider.model.ProviderMoney;
import java.time.LocalDateTime;
import java.util.List;

public record V12UserShipmentView(
        Long orderId,
        String orderNo,
        String deliveryType,
        ProviderMoney deliveryFee,
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
) {}
