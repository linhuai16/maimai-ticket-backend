package com.example.maimaibackend.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminRefundRuleItemVO {
    private Long refundRuleId;
    private Long projectId;
    private String projectTitle;
    private String projectStatus;
    private String refundType;
    private Integer stageCount;
    private BigDecimal minFeeRate;
    private BigDecimal maxFeeRate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getRefundRuleId() { return refundRuleId; }
    public void setRefundRuleId(Long refundRuleId) { this.refundRuleId = refundRuleId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }
    public String getRefundType() { return refundType; }
    public void setRefundType(String refundType) { this.refundType = refundType; }
    public Integer getStageCount() { return stageCount; }
    public void setStageCount(Integer stageCount) { this.stageCount = stageCount; }
    public BigDecimal getMinFeeRate() { return minFeeRate; }
    public void setMinFeeRate(BigDecimal minFeeRate) { this.minFeeRate = minFeeRate; }
    public BigDecimal getMaxFeeRate() { return maxFeeRate; }
    public void setMaxFeeRate(BigDecimal maxFeeRate) { this.maxFeeRate = maxFeeRate; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
