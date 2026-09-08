package com.activity.engine.decision.prd;

import com.activity.engine.decision.DecisionContext;
import com.activity.engine.decision.DecisionStrategy;
import com.activity.engine.decision.DistributionAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验收契约：实现完成后删除 @Disabled。
 */
class PrdPityDecisionTest {

    /** C 值对照表自检（来自 Dota2 公开数据）：0.25→≈0.085、0.15→≈0.032、0.50→≈0.30。 */
    @Test
    @Disabled("算法日 P0 待实现")
    void cFromPMatchesKnownTable() {
        assertEquals(0.085, PrdConstants.cFromP(0.25), 0.005);
        assertEquals(0.032, PrdConstants.cFromP(0.15), 0.003);
        assertEquals(0.30, PrdConstants.cFromP(0.50), 0.01);
    }

    /** 20 万次采样：实际命中率 ≈ 标称 25%（±5% 相对容差）。 */
    @Test
    @Disabled("算法日 P0 待实现")
    void longRunRateMatchesNominal() {
        var store = new InMemoryPityCounterStore();
        var decision = new PrdPityDecision(0.25, 7L, store, new Random(42));

        long hits = 0;
        int total = 200_000;
        for (int i = 0; i < total; i++) {
            if (decision.decide(new DecisionContext(1L, 1L, List.of())) == 7L) {
                hits++;
            }
        }
        double rate = (double) hits / total;
        assertTrue(Math.abs(rate - 0.25) < 0.25 * 0.05,
                "命中率 " + rate + " 偏离标称 0.25");
    }

    /** 硬保底：最大连空次数 ≤ ⌈1/C⌉（0.25 标称 → 12）。一次都不许超。 */
    @Test
    @Disabled("算法日 P0 待实现")
    void neverExceedsPity() {
        var store = new InMemoryPityCounterStore();
        var decision = new PrdPityDecision(0.25, 7L, store, new Random(42));

        List<Boolean> hits = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            hits.add(decision.decide(new DecisionContext(1L, 1L, List.of())) == 7L);
        }
        int maxDry = DistributionAssert.maxDryStreak(hits);
        assertTrue(maxDry <= 12, "最大连空 " + maxDry + " 超过保底 12");
    }

    /** 多用户计数隔离：A 的连空不推高 B 的概率。 */
    @Test
    @Disabled("算法日 P0 待实现")
    void countersArePerUser() {
        // TODO: userA 连空 11 次后，userB 第一次抽的命中概率仍应 ≈ C（统计显著性宽松断言）
    }
}
