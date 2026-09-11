package com.activity.engine.pipeline.award;

import com.activity.engine.infra.mapper.RecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 记录型发奖实现（v1 全兜）：任何权益类型都先落发奖流水。
 * 券/实物等真实发放动作是委托各自领域的扩展位——
 * 旧寻宝 ActivityRewardService.issueReward + RewardIssueStrategy 是三个 Issuer 的直接前身。
 */
@Component
public class RecordAwardIssuer implements AwardIssuer {

    @Resource
    private RecordMapper recordMapper;

    @Override
    public boolean supports(String benefitType) {
        return true;
    }

    @Override
    public void issue(IssueContext ctx) {
        recordMapper.insertAwardRecord(ctx.getActivity().getActivityId(), ctx.getUserId(),
                ctx.getBizKey(), ctx.getAward().getAwardId(), ctx.getAward().getBenefitType());
    }
}
