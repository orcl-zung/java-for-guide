package com.activity.engine.decision.weighted;

import com.activity.engine.decision.AwardItem;

import java.util.List;

/**
 * O(log N) CDF 二分查表：预计算累积分布数组，roll 后二分定位区间。
 * <p>
 * 适用：奖品多 / 精度高、O(1) 数组内存不可接受时。内存 = 奖品数。
 * <p>
 * 对照 big-market {@code OLogNAlgorithm}；参考：chengzhaoxi《加权随机事件的二分查找》（RESOURCES 已收）。
 */
public final class CdfLookupTable implements LookupTable {

    /** 累积分布（右端点，升序）：第 i 项占区间 [cdf[i-1], cdf[i])。 */
    private final int[] cdf;
    private final long[] awardIds;

    public CdfLookupTable(List<AwardItem> pool) {
        PoolValidator.requireFullScale(pool);
        int n = pool.size();
        cdf = new int[n];
        awardIds = new long[n];
        int cum = 0;
        for (int i = 0; i < n; i++) {
            cum += pool.get(i).getWeightMillionths();
            cdf[i] = cum;
            awardIds[i] = pool.get(i).getAwardId();
        }
    }

    @Override
    public long pick(int roll) {
        // upper_bound 语义：第一个 cdf[i] > roll 的位置。
        // 零权重项 cdf 与前任持平（空区间），二分天然跳过。
        int lo = 0;
        int hi = cdf.length - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (cdf[mid] > roll) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return awardIds[lo];
    }

    @Override
    public int scale() {
        return AwardItem.SCALE;
    }
}
