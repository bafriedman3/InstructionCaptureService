package com.example.instructions.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum TradeType {
    B("Buy"),
    S("Sell");
    private final String type;
    TradeType(String type) {
        this.type = type;
    }
    private static final Map<String, TradeType> typeConvert =
            Arrays.stream(values())
                    .collect(Collectors.toMap(
                            e -> e.type.toLowerCase(), e -> e
                    ));
    public static String convert(String type) {
        TradeType pType = typeConvert.get(type.toLowerCase());
        if(pType==null) {
            throw new IllegalArgumentException("Incorrect trade type: " + type);
        }
        return typeConvert.get(type.toLowerCase()).name();
    }

    public static boolean validType(String type) {
        return typeConvert.containsKey(type.toLowerCase());
    }
}
