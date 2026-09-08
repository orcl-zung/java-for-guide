package com.activity.engine.decision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分布统计测试工具——"用统计测试验证分布正确性"的载体。
 */
public final class DistributionAssert {

    private DistributionAssert() {
    }

    /** 采样 times 次，统计每个结果出现次数。 */
    public static <T> Map<T, Long> sample(Supplier<T> draw, int times) {
        Map<T, Long> counts = new HashMap<>();
        for (int i = 0; i < times; i++) {
            counts.merge(draw.get(), 1L, Long::sum);
        }
        return counts;
    }

    /**
     * 断言实际频率与期望概率的相对误差在容差内。
     *
     * @param counts        采样计数
     * @param expected      期望概率（key → 0..1）
     * @param total         采样总数
     * @param relTolerance  相对容差（如 0.05 = ±5%）
     */
    public static void assertFrequency(Map<Long, Long> counts, Map<Long, Double> expected,
                                       int total, double relTolerance) {
        expected.forEach((key, p) -> {
            long actual = counts.getOrDefault(key, 0L);
            double expectedCount = p * total;
            double tolerance = Math.max(expectedCount * relTolerance, total * 0.0005);
            assertTrue(Math.abs(actual - expectedCount) <= tolerance,
                    () -> "key=%s 期望≈%.0f 实际=%d 超出容差±%.0f".formatted(key, expectedCount, actual, tolerance));
        });
    }

    /** 最长连空（连续 false）次数——PRD 硬保底的验收断言用它。 */
    public static int maxDryStreak(List<Boolean> hits) {
        int max = 0;
        int cur = 0;
        for (boolean hit : hits) {
            cur = hit ? 0 : cur + 1;
            max = Math.max(max, cur);
        }
        return max;
    }
}
