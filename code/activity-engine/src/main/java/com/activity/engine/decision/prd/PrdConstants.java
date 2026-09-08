package com.activity.engine.decision.prd;

/**
 * PRD（Pseudo-Random Distribution）常数求解器。
 * <p>
 * 公式：P(N) = C × N，N = 距上次触发以来的尝试次数；触发后 N 重置。
 * 期望与标称概率相同，但方差收窄——消灭欧皇连中与酋长连空。
 * <p>
 * 参考（RESOURCES 已收）：Gamer Cat《从 Dota2 的伪随机谈开》（PfromC/CfromP 完整代码）、
 * Liquipedia: Random Distribution。
 * <p>
 * TODO(算法日 P0)：两个方向的求解。
 */
public final class PrdConstants {

    private PrdConstants() {
    }

    /**
     * 由 C 求实际期望概率 P（正算）。
     * <p>
     * 递推：pProcOnN = min(1, N*C) * (1 - pProcByN)；pProcByN 累加；期望 = 1 / Σ(N * pProcOnN)。
     */
    public static double pFromC(double c) {
        // TODO
        throw new UnsupportedOperationException("算法日 P0 待实现：pFromC");
    }

    /**
     * 由目标概率 P 反解 C（二分查找）。
     * <p>
     * 上界 = P，下界 = 0，迭代至 |pFromC(mid) - P| < 1e-10。
     * 对照表（自检用）：P=0.25 → C≈0.085；P=0.15 → C≈0.032；P=0.50 → C≈0.30。
     */
    public static double cFromP(double targetP) {
        // TODO
        throw new UnsupportedOperationException("算法日 P0 待实现：cFromP（二分）");
    }
}
