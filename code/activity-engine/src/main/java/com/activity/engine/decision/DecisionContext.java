package com.activity.engine.decision;

import lombok.Value;

import java.util.List;

/**
 * 决策上下文：一次决策所需的全部输入。
 * userId 是 PRD 的 N 计数的隔离维度；awardPool 为配置装配期已冻结的奖品池。
 */
@Value
public class DecisionContext {

    long activityId;
    long userId;
    List<AwardItem> awardPool;
}
