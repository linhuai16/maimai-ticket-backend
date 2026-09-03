package com.example.maimaibackend.ticketsource.purchase.options.model;

import java.util.List;

/**
 * 鸿蒙购票页面可使用的 V1.2 履约选择白名单。
 * 只暴露麦麦业务枚举和展示信息，不暴露第三方内部标识。
 */
public record V12PurchaseOptionsView(
        Long projectId,
        Long sessionId,
        Long skuId,
        boolean saleable,
        String purchaseMode,
        Integer limitPerOrder,
        Integer stockAvailable,
        boolean stockExact,
        List<Option> options,
        List<String> warnings
) {
    public record Option(
            String ticketMode,
            String deliveryMode,
            String label,
            boolean requiresAddress,
            boolean recommended
    ) {}
}
