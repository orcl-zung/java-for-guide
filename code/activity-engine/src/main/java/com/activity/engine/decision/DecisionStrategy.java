package com.activity.engine.decision;

/**
 * 决策 SPI：管线 ②b 节点的统一接口。
 * 实现：加权区间查表（weighted）/ PRD 保底（prd）/ 必中（发券场景直接返回固定奖品，无需实现类）。
 */
public interface DecisionStrategy {

    /** 未命中（谢谢参与）的奖品哨兵值。 */
    long MISS = -1L;

    /**
     * 执行一次决策。
     *
     * @return 命中奖品 ID；未命中返回 {@link #MISS}
     */
    long decide(DecisionContext ctx);
}
