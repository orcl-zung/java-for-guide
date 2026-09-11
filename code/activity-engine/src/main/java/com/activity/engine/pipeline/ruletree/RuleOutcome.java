package com.activity.engine.pipeline.ruletree;

import com.activity.engine.decision.DecisionStrategy;
import lombok.Value;

/**
 * 规则树节点的产出。awardId 为 MISS 表示最终未发奖。
 * fallbackApplied = 是否走了兜底降级（可追溯：流水记的是兜底奖的真实 awardId）。
 */
@Value
public class RuleOutcome {

    long awardId;
    boolean soldOut;
    boolean fallbackApplied;

    public static RuleOutcome hit(long awardId) {
        return new RuleOutcome(awardId, false, false);
    }

    public static RuleOutcome soldOut(long awardId) {
        return new RuleOutcome(awardId, true, false);
    }

    public static RuleOutcome fallback(long awardId) {
        return new RuleOutcome(awardId, false, true);
    }

    public static RuleOutcome miss() {
        return new RuleOutcome(DecisionStrategy.MISS, true, false);
    }
}
