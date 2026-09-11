package com.activity.engine.decision.prd;

import com.activity.engine.decision.DecisionContext;
import com.activity.engine.decision.DistributionAssert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PRD 验收：C 值对照表 / 长期命中率 / 硬保底上界 / 多用户计数隔离。
 */
class PrdPityDecisionTest {

    /** C 值对照表自检（来自 Dota2 公开数据）：0.25→≈0.085、0.15→≈0.032、0.50→≈0.30。 */
    @Test
    void cFromPMatchesKnownTable() {
        assertEquals(0.085, PrdConstants.cFromP(0.25), 0.005);
        assertEquals(0.032, PrdConstants.cFromP(0.15), 0.003);
        assertEquals(0.30, PrdConstants.cFromP(0.50), 0.01);
    }

    /** 20 万次采样：实际命中率 ≈ 标称 25%（±5% 相对容差 ≈ 13σ，种子 42 实测通过）。 */
    @Test
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

    /**
     * 多用户计数隔离：A 已连空 11 次（直接灌计数器，等价于连空 11 的状态），
     * 10 万个新用户各抽第一次——命中率必须仍是 C×1 ≈ 0.085（±10% 相对容差 ≈ 10σ），
     * 若计数器没按 userId 隔离，首抽概率会被 A 的 N 推高，立刻越界。
     */
    @Test
    void countersArePerUser() {
        var store = new InMemoryPityCounterStore();
        var decision = new PrdPityDecision(0.25, 7L, store, new Random(42));
        for (int i = 0; i < 11; i++) {
            store.incrementAndGet(1L, 7L);
        }

        long hits = 0;
        int total = 100_000;
        for (int u = 0; u < total; u++) {
            if (decision.decide(new DecisionContext(1L, 10_000L + u, List.of())) == 7L) {
                hits++;
            }
        }
        double rate = (double) hits / total;
        double c = PrdConstants.cFromP(0.25);
        assertTrue(Math.abs(rate - c) < c * 0.10,
                "新用户首抽命中率 " + rate + " 偏离 C=" + c + "——计数器疑似串用户");
    }
}
