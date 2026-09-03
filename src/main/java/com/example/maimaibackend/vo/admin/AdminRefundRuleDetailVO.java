package com.example.maimaibackend.vo.admin;

import java.time.LocalDateTime;
import java.util.List;

public class AdminRefundRuleDetailVO {
    private Long refundRuleId;
    private Long projectId;
    private String projectTitle;
    private String projectStatus;
    private String refundType;
    private Long sourceProviderId;
    private String providerRuleId;
    private String ruleDescription;
    private Boolean consumerEntryEnabled;
    private Boolean deliveryFeeRefundable;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<AdminRefundRuleStageVO> stages;

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
    public Long getSourceProviderId() { return sourceProviderId; }
    public void setSourceProviderId(Long sourceProviderId) { this.sourceProviderId = sourceProviderId; }
    public String getProviderRuleId() { return providerRuleId; }
    public void setProviderRuleId(String providerRuleId) { this.providerRuleId = providerRuleId; }
    public String getRuleDescription() { return ruleDescription; }
    public void setRuleDescription(String ruleDescription) { this.ruleDescription = ruleDescription; }
    public Boolean getConsumerEntryEnabled() { return consumerEntryEnabled; }
    public void setConsumerEntryEnabled(Boolean consumerEntryEnabled) { this.consumerEntryEnabled = consumerEntryEnabled; }
    public Boolean getDeliveryFeeRefundable() { return deliveryFeeRefundable; }
    public void setDeliveryFeeRefundable(Boolean deliveryFeeRefundable) { this.deliveryFeeRefundable = deliveryFeeRefundable; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<AdminRefundRuleStageVO> getStages() { return stages; }
    public void setStages(List<AdminRefundRuleStageVO> stages) { this.stages = stages; }
}
