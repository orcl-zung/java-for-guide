package com.activity.engine.decision.weighted;

import com.activity.engine.decision.AwardItem;

import java.util.List;

/**
 * O(1) 数组直查表：把权重展开成长度 = SCALE 的数组，下标即随机数，值即奖品 ID。
 * <p>
 * 适用：奖品池小、精度要求不极端。内存 = 刻度长度（百万分比 → 100 万元素）。
 * <p>
 * 对照 big-market {@code O1Algorithm}。
 * <p>
 * TODO(算法日 P0)：实现构建——按权重把 [0, SCALE) 区间分段填入数组。
 */
public final class ArrayLookupTable implements LookupTable {

    public ArrayLookupTable(List<AwardItem> pool) {
        // TODO: 校验权重和 == AwardItem.SCALE（含"谢谢参与"补足项），不相等直接 IllegalArgumentException
        // TODO: 构建 long[] table，区间 [cum, cum+weight) 填 awardId
        throw new UnsupportedOperationException("算法日 P0 待实现：O(1) 数组直查表");
    }

    @Override
    public long pick(int roll) {
        // TODO: return table[roll]，O(1)
        throw new UnsupportedOperationException("算法日 P0 待实现");
    }

    @Override
    public int scale() {
        return AwardItem.SCALE;
    }
}
