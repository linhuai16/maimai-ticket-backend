package com.example.maimaibackend.ticketsource.fulfillment.shipment.model;

import java.time.LocalDateTime;

public class V11ShipmentRecord {
    private Long shipmentId;
    private Long orderBridgeId;
    private String providerShipmentId;
    private String shipmentStatus;
    private String carrierCode;
    private String carrierName;
    private String waybillNo;
    private String trackingUrl;
    private LocalDateTime shippedTime;
    private LocalDateTime signedTime;
    private LocalDateTime lastSyncTime;
    private String providerVersion;
    private String lastSyncStatus;
    private String lastErrorCode;
    private String lastErrorMessage;

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Long getOrderBridgeId() { return orderBridgeId; }
    public void setOrderBridgeId(Long orderBridgeId) { this.orderBridgeId = orderBridgeId; }
    public String getProviderShipmentId() { return providerShipmentId; }
    public void setProviderShipmentId(String providerShipmentId) { this.providerShipmentId = providerShipmentId; }
    public String getShipmentStatus() { return shipmentStatus; }
    public void setShipmentStatus(String shipmentStatus) { this.shipmentStatus = shipmentStatus; }
    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
    public String getWaybillNo() { return waybillNo; }
    public void setWaybillNo(String waybillNo) { this.waybillNo = waybillNo; }
    public String getTrackingUrl() { return trackingUrl; }
    public void setTrackingUrl(String trackingUrl) { this.trackingUrl = trackingUrl; }
    public LocalDateTime getShippedTime() { return shippedTime; }
    public void setShippedTime(LocalDateTime shippedTime) { this.shippedTime = shippedTime; }
    public LocalDateTime getSignedTime() { return signedTime; }
    public void setSignedTime(LocalDateTime signedTime) { this.signedTime = signedTime; }
    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    public String getProviderVersion() { return providerVersion; }
    public void setProviderVersion(String providerVersion) { this.providerVersion = providerVersion; }
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    public String getLastErrorCode() { return lastErrorCode; }
    public void setLastErrorCode(String lastErrorCode) { this.lastErrorCode = lastErrorCode; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
}
