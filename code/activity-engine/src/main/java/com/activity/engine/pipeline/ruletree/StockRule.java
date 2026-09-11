package com.activity.engine.pipeline.ruletree;

import com.activity.engine.assembly.AssembledAward;
import com.activity.engine.infra.mapper.AwardMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 节点 1：库存校验。最终防线是 DB 条件更新（stock_remaining &gt; 0），
 * 不限库存奖项（stock_total=0）直接放行——⑥章双通道的同步事务通道侧。
 */
@Component
public class StockRule implements RuleNode {

    @Resource
    private AwardMapper awardMapper;

    @Override
    public int order() {
        return 1;
    }

    @Override
    public RuleOutcome apply(RuleContext ctx, RuleOutcome in) {
        if (in.isSoldOut()) {
            return in; // 已被前置判死，直穿
        }
        AssembledAward award = ctx.getActivity().award(ctx.getAwardId());
        if (award.unlimitedStock()) {
            return RuleOutcome.hit(ctx.getAwardId());
        }
        return awardMapper.deductStock(ctx.getAwardId()) == 1
                ? RuleOutcome.hit(ctx.getAwardId())
                : RuleOutcome.soldOut(ctx.getAwardId());
    }
}
