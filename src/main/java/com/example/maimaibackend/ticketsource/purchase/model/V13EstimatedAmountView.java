package com.example.maimaibackend.ticketsource.purchase.model;

public record V13EstimatedAmountView(
        String priceStatus,
        V13MoneyView ticketAmount,
        V13MoneyView deliveryFee,
        V13MoneyView payAmount,
        String displayText
) {}
