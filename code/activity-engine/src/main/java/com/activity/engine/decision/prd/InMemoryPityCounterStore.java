package com.activity.engine.decision.prd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内存版保底计数器（测试/切片用，生产换 Redis 实现）。
 */
public final class InMemoryPityCounterStore implements PityCounterStore {

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    @Override
    public int incrementAndGet(long userId, long awardId) {
        return counters.computeIfAbsent(key(userId, awardId), k -> new AtomicInteger()).incrementAndGet();
    }

    @Override
    public void reset(long userId, long awardId) {
        counters.remove(key(userId, awardId));
    }

    private String key(long userId, long awardId) {
        return userId + ":" + awardId;
    }
}
