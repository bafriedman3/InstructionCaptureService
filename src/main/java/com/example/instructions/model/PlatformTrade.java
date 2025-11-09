package com.example.instructions.model;

// PlatformTrade.java
public record PlatformTrade(String platform_id, Trade trade) {
    public record Trade(String account,
                        String security,
                        String type,
                        int amount,
                        String timestamp) {}
}