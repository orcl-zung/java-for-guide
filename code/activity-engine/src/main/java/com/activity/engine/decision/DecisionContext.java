package com.activity.engine.decision;

import java.util.List;

/**
 * 决策上下文：一次决策所需的全部输入。
 *
 * @param activityId 活动 ID
 * @param userId     用户 ID（PRD 的 N 计数按它隔离）
 * @param awardPool  当前活动的奖品池（配置装配期已冻结）
 */
public record DecisionContext(long activityId, long userId, List<AwardItem> awardPool) {
}
