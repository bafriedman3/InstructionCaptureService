package com.example.instructions.util;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.PlatformTrade;
import com.example.instructions.model.TradeType;

// TradeTransformer.java
public class TradeTransformer {
    private static final String PLATFORM_ID = "PLAT123";

    public static PlatformTrade createPlatformTrade(CanonicalTrade t) {
        return new PlatformTrade(
                PLATFORM_ID,
                new PlatformTrade.Trade(
                        maskAccount(t.accountNumber()),
                        t.securityId(),
                        TradeType.convert(t.tradeType()),
                        t.quantity(),
                        t.timestamp()
                )
        );
    }
    private static String maskAccount(String accountNo) {
        String masked ="";
        if(accountNo.length()>4) {
            masked = "*".repeat(accountNo.length() - 4)
                    + accountNo.substring(accountNo.length() - 4);
        }
        else {
            masked = accountNo;
        }
        return masked;
    }
}

