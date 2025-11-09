package com.example.instructions.model;

// CanonicalTrade.java
public record CanonicalTrade(
        String tradeId,
        String accountNumber,
        String securityId,
        String tradeType,
        int quantity,
        String timestamp
) {}

