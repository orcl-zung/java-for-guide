package com.activity.engine.decision.prd;

import com.activity.engine.decision.DecisionContext;
import com.activity.engine.decision.DecisionStrategy;

import java.util.random.RandomGenerator;

/**
 * PRD 伪随机 + 硬保底决策。
 * <p>
 * 单次决策：N = 计数器递增；P(N) = min(1, C × N)——N 达到 ⌈1/C⌉ 时 P=1 必然命中（硬保底是 PRD 的自然推论）；
 * roll < P(N) → 命中并重置计数；否则未中。
 * <p>
 * 本切片对单奖品判定（中/不中）；多奖品场景 = 先 PRD 判定大奖是否出，不出再走加权查表。
 * <p>
 * TODO(算法日 P0)：decide 主流程。
 */
public final class PrdPityDecision implements DecisionStrategy {

    private final double c;
    private final long targetAwardId;
    private final PityCounterStore counterStore;
    private final RandomGenerator random;

    /**
     * @param nominalProbability 标称概率（如 0.25）
     * @param targetAwardId      被 PRD 控制的奖品（稀有碎片/大奖）
     */
    public PrdPityDecision(double nominalProbability, long targetAwardId,
                           PityCounterStore counterStore, RandomGenerator random) {
        this.c = PrdConstants.cFromP(nominalProbability);
        this.targetAwardId = targetAwardId;
        this.counterStore = counterStore;
        this.random = random;
    }

    @Override
    public long decide(DecisionContext ctx) {
        // TODO:
        // int n = counterStore.incrementAndGet(ctx.userId(), targetAwardId);
        // double p = Math.min(1.0, c * n);
        // if (random.nextDouble() < p) { counterStore.reset(...); return targetAwardId; }
        // return MISS;
        throw new UnsupportedOperationException("算法日 P0 待实现：PRD 判定");
    }
}
