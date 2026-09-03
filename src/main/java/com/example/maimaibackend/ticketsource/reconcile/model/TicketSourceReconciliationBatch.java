package com.example.maimaibackend.ticketsource.reconcile.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketSourceReconciliationBatch {
    private Long batchId;
    private String batchNo;
    private Long providerId;
    private String providerCode;
    private String batchStatus;
    private Integer totalCount;
    private Integer matchedCount;
    private Integer differenceCount;
    private Integer errorCount;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<TicketSourceReconciliationDetail> details = new ArrayList<>();

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getBatchStatus() { return batchStatus; }
    public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getMatchedCount() { return matchedCount; }
    public void setMatchedCount(Integer matchedCount) { this.matchedCount = matchedCount; }
    public Integer getDifferenceCount() { return differenceCount; }
    public void setDifferenceCount(Integer differenceCount) { this.differenceCount = differenceCount; }
    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getFinishTime() { return finishTime; }
    public void setFinishTime(LocalDateTime finishTime) { this.finishTime = finishTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<TicketSourceReconciliationDetail> getDetails() { return details; }
    public void setDetails(List<TicketSourceReconciliationDetail> details) { this.details = details == null ? new ArrayList<>() : details; }
}
