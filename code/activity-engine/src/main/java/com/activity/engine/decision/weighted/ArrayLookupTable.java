package com.activity.engine.decision.weighted;

import com.activity.engine.decision.AwardItem;

import java.util.Arrays;
import java.util.List;

/**
 * O(1) 数组直查表：把权重展开成长度 = SCALE 的数组，下标即随机数，值即奖品 ID。
 * <p>
 * 适用：奖品池小、精度要求不极端。内存 = 刻度长度（百万分比 → 100 万元素）。
 * <p>
 * 对照 big-market {@code O1Algorithm}。
 */
public final class ArrayLookupTable implements LookupTable {

    private final long[] table;

    public ArrayLookupTable(List<AwardItem> pool) {
        PoolValidator.requireFullScale(pool);
        long[] t = new long[AwardItem.SCALE];
        int cum = 0;
        for (AwardItem item : pool) {
            // 区间 [cum, cum+weight) 填 awardId；零权重项区间为空，天然不占刻度
            Arrays.fill(t, cum, cum + item.getWeightMillionths(), item.getAwardId());
            cum += item.getWeightMillionths();
        }
        this.table = t;
    }

    @Override
    public long pick(int roll) {
        return table[roll];
    }

    @Override
    public int scale() {
        return AwardItem.SCALE;
    }
}
