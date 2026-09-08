package com.activity.engine.decision.weighted;

/**
 * 区间查找表：装配期构建，运行期一次随机 + 一次查表。
 * roll ∈ [0, scale)，返回奖品 ID。
 */
public interface LookupTable {

    /**
     * @param roll 随机数，取值 [0, scale)。<b>一次决策只许生成一个随机数</b>——
     *             循环内重复生成会破坏概率分布（行业踩坑点）。
     */
    long pick(int roll);

    /** 区间刻度（= 权重和，百万分比）。 */
    int scale();
}
