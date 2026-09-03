package com.example.maimaibackend.ticketsource.provider.model;

import java.util.List;

public record ProviderProjectDetail(
        ProviderProjectSummary summary,
        String subtitle,
        String introduction,
        String showDetail,
        List<String> mediaUrls,
        List<String> artists,
        List<String> organizers,
        List<ProviderNotice> notices,
        List<ProviderServiceCapability> serviceCapabilities,
        ProviderRefundPolicy refundPolicy,
        Integer purchaseLimitPerOrder
) {
    public ProviderProjectDetail {
        if (summary == null) throw new IllegalArgumentException("summary不能为空");
        mediaUrls = ModelSupport.list(mediaUrls);
        artists = ModelSupport.list(artists);
        organizers = ModelSupport.list(organizers);
        notices = ModelSupport.list(notices);
        serviceCapabilities = ModelSupport.list(serviceCapabilities);
    }

    public String entranceNotice() { return notice("ENTRANCE_NOTICE", "ENTRY_NOTICE"); }
    public String realNameNotice() { return notice("REAL_NAME_NOTICE", "REAL_NAME_POLICY"); }
    public String childrenNotice() { return notice("CHILDREN_NOTICE", "CHILDREN_POLICY"); }
    public String limitNotice() { return notice("LIMIT_NOTICE", "PURCHASE_LIMIT"); }
    public String selfGetTicketNotice() { return notice("SELF_GET_TICKET_NOTICE", "PICKUP_NOTICE"); }
    public String depositInfo() { return notice("DEPOSIT_INFO", "STORAGE_NOTICE"); }
    public String prohibitedItems() { return notice("PROHIBITED_ITEMS"); }
    public String eTicketNotice() { return notice("ETICKET_NOTICE", "E_TICKET_NOTICE"); }
    public String choiceSeatNotice() { return notice("CHOICE_SEAT_NOTICE", "SEAT_SELECTION_NOTICE"); }
    public String policyOfReturn() {
        String notice = notice("POLICY_OF_RETURN", "REFUND_NOTICE");
        return notice != null ? notice : refundPolicy == null ? null : refundPolicy.sourceRuleText();
    }

    private String notice(String... codes) {
        for (String code : codes) {
            for (ProviderNotice notice : notices) {
                if (notice.noticeCode() != null && notice.noticeCode().equalsIgnoreCase(code)
                        && notice.content() != null && !notice.content().isBlank()) {
                    return notice.content();
                }
            }
        }
        return null;
    }
}
