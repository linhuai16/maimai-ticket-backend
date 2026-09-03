package com.example.maimaibackend.ticketsource.provider.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/** 金额统一使用最小货币单位；人民币 amountMinor 的单位是分。 */
public record ProviderMoney(long amountMinor, String currency) {
    public ProviderMoney {
        if (amountMinor < 0) throw new IllegalArgumentException("amountMinor不能为负数");
        currency = currency == null || currency.isBlank() ? "CNY" : currency.trim().toUpperCase(Locale.ROOT);
    }

    public static ProviderMoney cny(long fen) { return new ProviderMoney(fen, "CNY"); }

    public static ProviderMoney fromMajor(BigDecimal amount, String currency) {
        if (amount == null) return null;
        long minor = amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        return new ProviderMoney(minor, currency);
    }

    public BigDecimal toMajor() {
        return BigDecimal.valueOf(amountMinor, 2);
    }

    public ProviderMoney plus(ProviderMoney other) {
        requireSameCurrency(other);
        return new ProviderMoney(Math.addExact(amountMinor, other.amountMinor), currency);
    }

    private void requireSameCurrency(ProviderMoney other) {
        if (other == null || !currency.equals(other.currency)) {
            throw new IllegalArgumentException("金额币种不一致");
        }
    }
}
