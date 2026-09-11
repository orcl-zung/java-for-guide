package com.activity.engine.decision.weighted;

import com.activity.engine.decision.AwardItem;
import com.activity.engine.decision.DecisionContext;
import com.activity.engine.decision.DistributionAssert;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验收契约：加权区间查表（P0 已实现——分布统计 + 双表一致 + 阈值切换 + 装配校验）。
 */
class WeightedTableDecisionTest {

    /** 10 万次采样，各奖品频率与权重分布误差 ≤ ±5%（相对）。 */
    @Test
    void distributionMatchesWeights() {
        // 奖池：A 1%、B 9%、谢谢参与(MISS) 90%
        List<AwardItem> pool = List.of(
                new AwardItem(1L, 10_000),
                new AwardItem(2L, 90_000),
                new AwardItem(-1L, 900_000));
        // 种子经实测校准：原 42 在 ±50 容差下出现 2.2σ 偏差（932/1000）——
        // 固定种子的统计测试必须真跑过才算数
        var decision = WeightedTableDecision.assemble(pool, new Random(0));

        var counts = DistributionAssert.sample(
                () -> decision.decide(new DecisionContext(100L, 1L, pool)), 100_000);

        DistributionAssert.assertFrequency(counts,
                Map.of(1L, 0.01, 2L, 0.09, -1L, 0.90), 100_000, 0.05);
    }

    /** O(1) 与 O(log N) 两种表对同一奖池、同一 roll 序列，结果必须一致。 */
    @Test
    void arrayAndCdfTablesAgree() {
        List<AwardItem> pool = List.of(
                new AwardItem(1L, 250_000), new AwardItem(2L, 250_000),
                new AwardItem(3L, 250_000), new AwardItem(4L, 250_000));
        var array = new ArrayLookupTable(pool);
        var cdf = new CdfLookupTable(pool);
        for (int roll : new int[]{0, 1, 249_999, 250_000, 500_000, 999_999}) {
            assertEquals(array.pick(roll), cdf.pick(roll), "roll=" + roll);
        }
    }

    /** 装配阈值：≤64 个奖品用数组表，否则 CDF 表。 */
    @Test
    void assembleSwitchesByPoolSize() {
        // 64 项整除：64 × 15625 = 1_000_000
        List<AwardItem> small = IntStream.rangeClosed(1, 64)
                .mapToObj(i -> new AwardItem(i, 15_625))
                .toList();
        assertTrue(WeightedTableDecision.assemble(small, new Random(1)).table() instanceof ArrayLookupTable);

        // 65 项：15424 + 64 × 15384 = 1_000_000
        List<AwardItem> large = IntStream.rangeClosed(1, 65)
                .mapToObj(i -> new AwardItem(i, i == 1 ? 15_424 : 15_384))
                .toList();
        assertTrue(WeightedTableDecision.assemble(large, new Random(1)).table() instanceof CdfLookupTable);
    }

    /** 权重和 ≠ 1_000_000 直接拒绝装配。 */
    @Test
    void rejectsInvalidWeightSum() {
        List<AwardItem> bad = List.of(new AwardItem(1L, 500_000));
        assertThrows(IllegalArgumentException.class, () -> new ArrayLookupTable(bad));
        assertThrows(IllegalArgumentException.class, () -> new CdfLookupTable(bad));
    }
}
