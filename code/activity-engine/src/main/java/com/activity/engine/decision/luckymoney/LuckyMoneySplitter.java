package com.activity.engine.decision.luckymoney;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * 拼手气红包：二倍均值法（微信同款）。
 * <p>
 * 规则：剩余 M 分、剩 N 人时，本次金额 ∈ [1, (M/N)×2] 分 均匀随机，最后一人拿剩余。
 * 期望恒 = 当前剩余人均 → 先抢后抢数学公平。
 * 对比线段切分法：理论更随机但方差失控（0.01 vs 99.99），业界不采用。
 * <p>
 * 铁律：全程以"分"为单位的 long 运算，禁止浮点金额。
 * <p>
 * TODO(算法日 P1)：split 实现。
 */
public final class LuckyMoneySplitter {

    private LuckyMoneySplitter() {
    }

    /**
     * @param totalCents 总金额（分）
     * @param count      人数
     * @return 每人金额（分），长度 = count，总和必须恒等于 totalCents
     */
    public static List<Long> split(long totalCents, int count, RandomGenerator random) {
        // TODO: 校验 totalCents >= count（每人至少 1 分）
        // TODO: 循环 count-1 次：[1, 剩余人均×2] 均匀随机；最后一人 = 剩余
        throw new UnsupportedOperationException("算法日 P1 待实现：二倍均值法");
    }
}
