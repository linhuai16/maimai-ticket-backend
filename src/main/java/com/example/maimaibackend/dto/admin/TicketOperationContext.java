package com.example.maimaibackend.dto.admin;

public class TicketOperationContext {
    private String operatorType;
    private Long operatorId;
    private String operatorName;
    private String sourceIp;

    public TicketOperationContext() {
    }

    public static TicketOperationContext admin(Long operatorId, String operatorName, String sourceIp) {
        TicketOperationContext context = new TicketOperationContext();
        context.setOperatorType("ADMIN");
        context.setOperatorId(operatorId);
        context.setOperatorName(operatorName);
        context.setSourceIp(sourceIp);
        return context;
    }

    public static TicketOperationContext system(String operatorName) {
        TicketOperationContext context = new TicketOperationContext();
        context.setOperatorType("SYSTEM");
        context.setOperatorName(operatorName == null || operatorName.trim().isEmpty() ? "SYSTEM" : operatorName.trim());
        return context;
    }

    public String getOperatorType() { return operatorType; }
    public void setOperatorType(String operatorType) { this.operatorType = operatorType; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }
}
