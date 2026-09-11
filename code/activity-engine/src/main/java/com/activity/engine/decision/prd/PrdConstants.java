package com.activity.engine.decision.prd;

/**
 * PRD（Pseudo-Random Distribution）常数求解器。
 * <p>
 * 公式：P(N) = C × N，N = 距上次触发以来的尝试次数；触发后 N 重置。
 * 期望与标称概率相同，但方差收窄——消灭欧皇连中与酋长连空。
 * <p>
 * 参考（RESOURCES 已收）：Gamer Cat《从 Dota2 的伪随机谈开》（PfromC/CfromP 完整代码）、
 * Liquipedia: Random Distribution。
 */
public final class PrdConstants {

    private PrdConstants() {
    }

    /**
     * 由 C 求实际期望概率 P（正算）。
     * <p>
     * 递推：第 N 次才触发的概率 = 前 N-1 次都没触发 × 第 N 次触发：
     * pProcOnN = (1 - pProcByN) × min(1, N×C)，pProcByN 累加。
     * N 到 ⌈1/C⌉ 时 min(1, N×C)=1，剩余概率全部落在这一次，级数恰好收敛到 1。
     * 期望概率 = 1 / E[N] = 1 / Σ(N × pProcOnN)。
     */
    public static double pFromC(double c) {
        if (c <= 0) {
            return 0;
        }
        double pProcByN = 0;
        double sumNpProcOnN = 0;
        int maxPity = (int) Math.ceil(1.0 / c);
        for (int n = 1; n <= maxPity; n++) {
            double pProcOnN = Math.min(1.0, n * c) * (1 - pProcByN);
            pProcByN += pProcOnN;
            sumNpProcOnN += n * pProcOnN;
        }
        return 1.0 / sumNpProcOnN;
    }

    /**
     * 由目标概率 P 反解 C（二分查找：pFromC 单调递增，C 必落在 (0, P)）。
     * 迭代至 |pFromC(mid) - P| &lt; 1e-10。
     * 对照表（自检用）：P=0.25 → C≈0.085；P=0.15 → C≈0.032；P=0.50 → C≈0.30。
     */
    public static double cFromP(double targetP) {
        if (targetP <= 0) {
            return 0;
        }
        if (targetP >= 1) {
            return 1;
        }
        double lo = 0, hi = targetP;
        double mid = lo;
        while (true) {
            mid = (lo + hi) / 2;
            double p = pFromC(mid);
            if (Math.abs(p - targetP) < 1e-10) {
                break;
            }
            if (p > targetP) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        return mid;
    }
}
