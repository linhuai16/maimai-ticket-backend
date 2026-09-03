package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;

public class AdminRefundRuleStageVO {
    private Long stageId;
    private Long refundRuleId;
    private Integer minBeforeStartMinutes;
    private Integer maxBeforeStartMinutes;
    private String stageResult;
    private BigDecimal feeRate;
    private BigDecimal fixedFeeAmount;
    private Integer sortOrder;

    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public Long getRefundRuleId() { return refundRuleId; }
    public void setRefundRuleId(Long refundRuleId) { this.refundRuleId = refundRuleId; }
    public Integer getMinBeforeStartMinutes() { return minBeforeStartMinutes; }
    public void setMinBeforeStartMinutes(Integer minBeforeStartMinutes) { this.minBeforeStartMinutes = minBeforeStartMinutes; }
    public Integer getMaxBeforeStartMinutes() { return maxBeforeStartMinutes; }
    public void setMaxBeforeStartMinutes(Integer maxBeforeStartMinutes) { this.maxBeforeStartMinutes = maxBeforeStartMinutes; }
    public String getStageResult() { return stageResult; }
    public void setStageResult(String stageResult) { this.stageResult = stageResult; }
    public BigDecimal getFeeRate() { return feeRate; }
    public void setFeeRate(BigDecimal feeRate) { this.feeRate = feeRate; }
    public BigDecimal getFixedFeeAmount() { return fixedFeeAmount; }
    public void setFixedFeeAmount(BigDecimal fixedFeeAmount) { this.fixedFeeAmount = fixedFeeAmount; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
