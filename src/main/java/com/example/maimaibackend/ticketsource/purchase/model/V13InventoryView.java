package com.example.maimaibackend.ticketsource.purchase.model;

public record V13InventoryView(
        String stockState,
        Integer availableStock,
        boolean exact,
        String displayText,
        boolean canContinue
) {}
