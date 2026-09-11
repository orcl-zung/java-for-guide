package com.activity.engine.pipeline.ruletree;

/**
 * 规则树节点 SPI（③规则树）。v1 两个节点：库存校验 → 兜底奖；
 * 扩展位：风控拦截（旧寻宝防作弊四层的归位点之一）。
 * <p>
 * 每个节点读入上一个节点的产出，决定放行、改造或终结——
 * 与资格链的区别：资格链只会"拒"，规则树会"换"（兜底降级）。
 */
public interface RuleNode {

    int order();

    RuleOutcome apply(RuleContext ctx, RuleOutcome in);
}
