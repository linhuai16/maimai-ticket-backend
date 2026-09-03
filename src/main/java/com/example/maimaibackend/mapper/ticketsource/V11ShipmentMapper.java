package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.fulfillment.shipment.model.V11ShipmentContext;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.model.V11ShipmentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface V11ShipmentMapper {
    V11ShipmentContext selectShipmentContext(@Param("orderId") Long orderId,
                                              @Param("userId") Long userId);

    V11ShipmentContext selectShipmentContextAdmin(@Param("orderId") Long orderId);

    V11ShipmentRecord selectShipmentByBridgeId(@Param("bridgeId") Long bridgeId);

    int insertWaitShipment(@Param("bridgeId") Long bridgeId);

    int markShipmentNotRequired(@Param("bridgeId") Long bridgeId,
                                @Param("syncTime") LocalDateTime syncTime);

    int upsertProviderShipment(@Param("bridgeId") Long bridgeId,
                               @Param("shipmentStatus") String shipmentStatus,
                               @Param("carrierCode") String carrierCode,
                               @Param("carrierName") String carrierName,
                               @Param("waybillNo") String waybillNo,
                               @Param("trackingUrl") String trackingUrl,
                               @Param("shippedTime") LocalDateTime shippedTime,
                               @Param("signedTime") LocalDateTime signedTime,
                               @Param("lastSyncTime") LocalDateTime lastSyncTime,
                               @Param("providerVersion") String providerVersion);

    int markSyncFailure(@Param("bridgeId") Long bridgeId,
                        @Param("errorCode") String errorCode,
                        @Param("errorMessage") String errorMessage,
                        @Param("syncTime") LocalDateTime syncTime);

    List<V11ShipmentContext> selectPendingShipmentContexts(@Param("limit") int limit,
                                                            @Param("staleBefore") LocalDateTime staleBefore,
                                                            @Param("deliveredStaleBefore") LocalDateTime deliveredStaleBefore);
}
