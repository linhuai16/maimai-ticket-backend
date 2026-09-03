package com.example.maimaibackend.dto.admin;

import java.math.BigDecimal;

/** R4: 麦麦平台售价策略。Provider 原始价/结算价不可由本接口修改。 */
public class AdminUpdatePlatformPriceRequest {
    private String priceMode;
    private BigDecimal platformPrice;

    public String getPriceMode() { return priceMode; }
    public void setPriceMode(String priceMode) { this.priceMode = priceMode; }
    public BigDecimal getPlatformPrice() { return platformPrice; }
    public void setPlatformPrice(BigDecimal platformPrice) { this.platformPrice = platformPrice; }
}
