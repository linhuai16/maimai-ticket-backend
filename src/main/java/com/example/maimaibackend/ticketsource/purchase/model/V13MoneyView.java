package com.example.maimaibackend.ticketsource.purchase.model;

/** 用户侧金额VO：只暴露最小货币单位，不暴露第三方结算价。 */
public record V13MoneyView(long amountMinor, String currency) {
    public V13MoneyView {
        currency = currency == null || currency.isBlank() ? "CNY" : currency.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
