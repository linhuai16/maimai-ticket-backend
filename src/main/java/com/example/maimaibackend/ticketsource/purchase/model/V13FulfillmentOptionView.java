package com.example.maimaibackend.ticketsource.purchase.model;

import java.util.List;

/** V1.3 用户侧入场/取票方式，不包含 provider* 字段；金额只用于展示。 */
public record V13FulfillmentOptionView(
        String optionCode,
        String displayName,
        String credentialType,
        String deliveryMethod,
        boolean requiresAddress,
        boolean requiresDynamicRefresh,
        boolean recommended,
        V13MoneyView deliveryFee,
        List<String> userTips
) {}
