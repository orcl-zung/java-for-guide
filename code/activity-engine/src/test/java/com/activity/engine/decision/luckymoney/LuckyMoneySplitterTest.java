package com.activity.engine.decision.luckymoney;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验收契约：实现完成后删除 @Disabled。
 */
class LuckyMoneySplitterTest {

    /** 总额守恒：1 万次随机分，每次总和分毫不差。 */
    @Test
    @Disabled("算法日 P1 待实现")
    void totalConserved() {
        var random = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            long total = 1 + random.nextLong(1_000_000);
            int count = 1 + random.nextInt(50);
            if (total < count) {
                continue;
            }
            List<Long> parts = LuckyMoneySplitter.split(total, count, random);
            assertEquals(count, parts.size());
            assertEquals(total, parts.stream().mapToLong(Long::longValue).sum(), "总额必须守恒");
            parts.forEach(p -> assertTrue(p >= 1, "每人至少 1 分"));
        }
    }

    /** 期望公平：100 元分 10 人，重复 10 万次，各位置期望 ≈ 10 元（±5%）——先抢后抢数学公平。 */
    @Test
    @Disabled("算法日 P1 待实现")
    void expectationIsFairAcrossPositions() {
        // TODO: 固定 total=10000 分、count=10，采样 10 万次，按位置累加求均值，断言各位置均值 ∈ [9.5, 10.5] 元
    }

    /** 总额不够每人 1 分时直接拒绝。 */
    @Test
    @Disabled("算法日 P1 待实现")
    void rejectsTotalSmallerThanCount() {
        assertThrows(IllegalArgumentException.class,
                () -> LuckyMoneySplitter.split(5, 10, new Random(1)));
    }
}
