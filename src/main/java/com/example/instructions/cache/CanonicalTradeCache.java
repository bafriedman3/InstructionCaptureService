package com.example.instructions.cache;

import com.example.instructions.model.CanonicalTrade;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CanonicalTradeCache {
    private final Map<String, CanonicalTrade> auditStore = Collections.synchronizedMap(
            new EvictionHashMap<>(10000));
    public CanonicalTrade get(String key) {
        return auditStore.get(key);
    }
    public void put(String key, CanonicalTrade value) {
        auditStore.put(key, value);
    }
}

class EvictionHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public EvictionHashMap(int maxSize) {
        super(maxSize, 0.75f, true); // access-order = true for LRU
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}
