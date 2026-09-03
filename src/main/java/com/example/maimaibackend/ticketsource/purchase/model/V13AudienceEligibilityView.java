package com.example.maimaibackend.ticketsource.purchase.model;

/** 提交订单页观演人可选状态；不包含证件号明文。 */
public record V13AudienceEligibilityView(
        Long audienceId,
        boolean canSelect,
        String disabledReason
) {}
