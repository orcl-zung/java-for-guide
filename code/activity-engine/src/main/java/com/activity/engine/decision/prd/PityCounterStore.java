package com.activity.engine.decision.prd;

/**
 * 保底计数器存储：记录 (userId, awardId) 距上次命中以来的尝试次数 N。
 * 生产实现 = Redis（INCR/DEL）；切片用内存实现跑统计测试。
 */
public interface PityCounterStore {

    /** N + 1 并返回新值（本次尝试前递增）。 */
    int incrementAndGet(long userId, long awardId);

    /** 命中后清零。 */
    void reset(long userId, long awardId);
}
