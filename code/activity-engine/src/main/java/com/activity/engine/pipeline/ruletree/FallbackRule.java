package com.activity.engine.pipeline.ruletree;

import com.activity.engine.assembly.AssembledAward;
import com.activity.engine.infra.mapper.AwardMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 节点 2：兜底奖。决策选中奖售罄 → 降级到 is_fallback=1 的奖项
 * （业内同款：big-market RuleLuckAward 的"未中不退次数走兜底"）。
 * 兜底奖自身也是有限库存且也售罄 → MISS（兜底不能再兜兜底）。
 */
@Component
public class FallbackRule implements RuleNode {

    @Resource
    private AwardMapper awardMapper;

    @Override
    public int order() {
        return 2;
    }

    @Override
    public RuleOutcome apply(RuleContext ctx, RuleOutcome in) {
        if (!in.isSoldOut()) {
            return in;
        }
        Long fallbackId = ctx.getActivity().getFallbackAwardId();
        if (fallbackId == null) {
            return RuleOutcome.miss();
        }
        AssembledAward fallback = ctx.getActivity().award(fallbackId);
        boolean ok = fallback.unlimitedStock() || awardMapper.deductStock(fallbackId) == 1;
        return ok ? RuleOutcome.fallback(fallbackId) : RuleOutcome.miss();
    }
}
