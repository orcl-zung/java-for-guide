package com.activity.engine.pipeline.qualification;

import com.activity.engine.assembly.AssembledActivity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 节点 0：装配态（上架开关 + 起止时间派生活动是否进行中）。
 * 内存读，O(1)——全链最便宜的节点，永远第一个。
 * DB DATETIME 存 UTC，now 也用 UTC 对齐基准。
 */
@Component
public class AssembledStateNode implements QualificationNode {

    @Override
    public int order() {
        return 0;
    }

    @Override
    public QualificationResult check(AssembledActivity activity, long userId) {
        return activity.isLive(LocalDateTime.now(ZoneOffset.UTC))
                ? QualificationResult.pass()
                : QualificationResult.reject("ASSEMBLED_STATE");
    }
}
