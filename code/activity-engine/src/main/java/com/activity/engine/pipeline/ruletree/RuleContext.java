package com.activity.engine.pipeline.ruletree;

import com.activity.engine.assembly.AssembledActivity;
import lombok.Value;

/**
 * 规则树上下文（③规则树）：一次决策结果在树上流动时携带的全部输入。
 * awardId 为 ②b 决策选中的奖品（MISS=-1 时根本不会走到规则树）。
 */
@Value
public class RuleContext {

    AssembledActivity activity;
    long userId;
    long awardId;
}
