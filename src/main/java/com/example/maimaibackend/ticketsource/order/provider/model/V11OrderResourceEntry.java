package com.example.maimaibackend.ticketsource.order.provider.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** R4: Provider 与本地资源的可观测映射明细。 */
public class V11OrderResourceEntry {
    private Long projectId; private Long sessionId; private Long skuId; private String skuName;
    private BigDecimal localPrice; private String priceMode; private Integer localStock; private String localSkuStatus;
    private String deliveryType; private String providerCode; private String providerProjectId; private String projectMappingStatus;
    private String providerSessionId; private String sessionMappingStatus; private String providerSkuId; private String providerSkuName;
    private String skuMappingStatus; private String sourceSaleStatus; private String inventoryMode; private Integer availableStockSnapshot;
    private BigDecimal providerFacePrice; private BigDecimal providerSalePrice; private BigDecimal settlementPrice;
    private String lastSyncStatus; private LocalDateTime lastSyncTime;
    public Long getProjectId(){return projectId;} public void setProjectId(Long v){projectId=v;}
    public Long getSessionId(){return sessionId;} public void setSessionId(Long v){sessionId=v;}
    public Long getSkuId(){return skuId;} public void setSkuId(Long v){skuId=v;}
    public String getSkuName(){return skuName;} public void setSkuName(String v){skuName=v;}
    public BigDecimal getLocalPrice(){return localPrice;} public void setLocalPrice(BigDecimal v){localPrice=v;}
    public String getPriceMode(){return priceMode;} public void setPriceMode(String v){priceMode=v;}
    public Integer getLocalStock(){return localStock;} public void setLocalStock(Integer v){localStock=v;}
    public String getLocalSkuStatus(){return localSkuStatus;} public void setLocalSkuStatus(String v){localSkuStatus=v;}
    public String getDeliveryType(){return deliveryType;} public void setDeliveryType(String v){deliveryType=v;}
    public String getProviderCode(){return providerCode;} public void setProviderCode(String v){providerCode=v;}
    public String getProviderProjectId(){return providerProjectId;} public void setProviderProjectId(String v){providerProjectId=v;}
    public String getProjectMappingStatus(){return projectMappingStatus;} public void setProjectMappingStatus(String v){projectMappingStatus=v;}
    public String getProviderSessionId(){return providerSessionId;} public void setProviderSessionId(String v){providerSessionId=v;}
    public String getSessionMappingStatus(){return sessionMappingStatus;} public void setSessionMappingStatus(String v){sessionMappingStatus=v;}
    public String getProviderSkuId(){return providerSkuId;} public void setProviderSkuId(String v){providerSkuId=v;}
    public String getProviderSkuName(){return providerSkuName;} public void setProviderSkuName(String v){providerSkuName=v;}
    public String getSkuMappingStatus(){return skuMappingStatus;} public void setSkuMappingStatus(String v){skuMappingStatus=v;}
    public String getSourceSaleStatus(){return sourceSaleStatus;} public void setSourceSaleStatus(String v){sourceSaleStatus=v;}
    public String getInventoryMode(){return inventoryMode;} public void setInventoryMode(String v){inventoryMode=v;}
    public Integer getAvailableStockSnapshot(){return availableStockSnapshot;} public void setAvailableStockSnapshot(Integer v){availableStockSnapshot=v;}
    public BigDecimal getProviderFacePrice(){return providerFacePrice;} public void setProviderFacePrice(BigDecimal v){providerFacePrice=v;}
    public BigDecimal getProviderSalePrice(){return providerSalePrice;} public void setProviderSalePrice(BigDecimal v){providerSalePrice=v;}
    public BigDecimal getSettlementPrice(){return settlementPrice;} public void setSettlementPrice(BigDecimal v){settlementPrice=v;}
    public String getLastSyncStatus(){return lastSyncStatus;} public void setLastSyncStatus(String v){lastSyncStatus=v;}
    public LocalDateTime getLastSyncTime(){return lastSyncTime;} public void setLastSyncTime(LocalDateTime v){lastSyncTime=v;}
}
