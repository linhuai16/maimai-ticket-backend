package com.example.maimaibackend.ticketsource.reconcile.dto;

import java.util.ArrayList;
import java.util.List;

public class TicketSourceReconciliationRequest {
    private String providerCode;
    private List<Long> orderIds = new ArrayList<>();

    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public List<Long> getOrderIds() { return orderIds; }
    public void setOrderIds(List<Long> orderIds) { this.orderIds = orderIds == null ? new ArrayList<>() : orderIds; }
}
