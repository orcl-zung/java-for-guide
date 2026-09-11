package com.activity.engine.pipeline.award;

import com.activity.engine.assembly.AssembledActivity;
import com.activity.engine.assembly.AssembledAward;
import lombok.Value;

/**
 * 发奖上下文（④统一发奖中心）。
 */
@Value
public class IssueContext {

    AssembledActivity activity;
    long userId;
    String bizKey;
    AssembledAward award;
}
