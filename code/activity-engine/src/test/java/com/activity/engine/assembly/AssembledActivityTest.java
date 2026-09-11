package com.activity.engine.assembly;

import com.activity.engine.decision.AwardItem;
import com.activity.engine.decision.DecisionStrategy;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 运行态派生（isLive）边界：status 不落库，由 (on_off, 起止, now) 现算——
 * 旧寻宝"状态机双轨"缺陷的根治点，边界值必须钉死。
 */
class AssembledActivityTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 9, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 1, 0, 0);

    private static AssembledActivity act(int onOff) {
        return AssembledActivity.builder()
                .activityId(1L).activityCode("T").activityType("LOTTERY").decisionType("WEIGHTED")
                .onOff(onOff).startTime(START).endTime(END)
                .freqDayLimit(0).freqTotalLimit(0)
                .awards(List.of())
                .pool(List.of(new AwardItem(DecisionStrategy.MISS, AwardItem.SCALE)))
                .missPadding(0)
                .build();
    }

    @Test
    void liveOnlyWhenOnShelfAndWithinWindow() {
        assertTrue(act(1).isLive(START), "开始瞬间即进行中（左闭）");
        assertTrue(act(1).isLive(END.minusNanos(1)));
        assertFalse(act(1).isLive(END), "结束瞬间即结束（右开）");
        assertFalse(act(1).isLive(START.minusNanos(1)), "未开始");
        assertFalse(act(0).isLive(START), "下架一票否决");
    }
}
