package com.activity.engine.decision.weighted;

import com.activity.engine.decision.AwardItem;

import java.util.List;

/**
 * O(log N) CDF 二分查表：预计算累积分布数组，roll 后二分定位区间。
 * <p>
 * 适用：奖品多 / 精度高、O(1) 数组内存不可接受时。内存 = 奖品数。
 * <p>
 * 对照 big-market {@code OLogNAlgorithm}；参考：chengzhaoxi《加权随机事件的二分查找》（RESOURCES 已收）。
 * <p>
 * TODO(算法日 P0)：实现构建 + 二分查找。
 */
public final class CdfLookupTable implements LookupTable {

    public CdfLookupTable(List<AwardItem> pool) {
        // TODO: 校验权重和 == AwardItem.SCALE
        // TODO: 构建 cdf 累积数组 + awardIds 平行数组
        throw new UnsupportedOperationException("算法日 P0 待实现：CDF 二分查表");
    }

    @Override
    public long pick(int roll) {
        // TODO: 在 cdf 上二分找第一个 > roll 的位置（upper_bound 语义）
        throw new UnsupportedOperationException("算法日 P0 待实现");
    }

    @Override
    public int scale() {
        return AwardItem.SCALE;
    }
}
