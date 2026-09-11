package com.activity.engine.api;

import com.activity.engine.assembly.AssembledActivity;
import com.activity.engine.assembly.AssembledAward;
import lombok.Value;

import java.util.List;

/**
 * 装配态奖池视图：/activity/{id}/pool 的返回——让"装配"可见。
 */
@Value
public class PoolView {

    long activityId;
    String activityCode;
    String decisionType;
    int missPadding;
    Long fallbackAwardId;
    List<AssembledAward> awards;

    public static PoolView of(AssembledActivity act) {
        return new PoolView(act.getActivityId(), act.getActivityCode(), act.getDecisionType(),
                act.getMissPadding(), act.getFallbackAwardId(), act.getAwards());
    }
}
