package com.activity.engine.decision.weighted;

import com.activity.engine.decision.AwardItem;
import com.activity.engine.decision.DecisionContext;
import com.activity.engine.decision.DecisionStrategy;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * 加权区间查表决策（抽奖主力实现）。
 * <p>
 * 装配期按池规模选择查找表：小池 O(1) 数组直查，大池/高精度 O(log N) CDF 二分。
 * 运行期：一次随机 + 一次查表。
 * <p>
 * 不选 Alias 的理由（面试口径）：Alias 采样 O(1) 但构建 O(N) 且概率变更需全量重建两张表；
 * 营销抽奖的库存实时消耗会频繁触发概率归一化，区间查表改边界即可，Alias 得整体重排。
 * <p>
 * 库存联动：③规则树扣库存失败 → 该奖品区间置空 + 剩余权重归一化（重建本表）。
 */
public final class WeightedTableDecision implements DecisionStrategy {

    /** O(1)/O(log N) 切换阈值：奖品数 ≤ 此值用数组直查。 */
    static final int ARRAY_THRESHOLD = 64;

    private final LookupTable table;
    private final RandomGenerator random;

    private WeightedTableDecision(LookupTable table, RandomGenerator random) {
        this.table = table;
        this.random = random;
    }

    /** 装配：按奖品池规模选表（阈值切换在装配期一次完成，运行期无分支）。 */
    public static WeightedTableDecision assemble(List<AwardItem> pool, RandomGenerator random) {
        LookupTable table = pool.size() <= ARRAY_THRESHOLD
                ? new ArrayLookupTable(pool)
                : new CdfLookupTable(pool);
        return new WeightedTableDecision(table, random);
    }

    @Override
    public long decide(DecisionContext ctx) {
        // 一次决策只生成一个随机数——循环内重复生成会破坏概率分布（行业踩坑点）
        int roll = random.nextInt(table.scale());
        return table.pick(roll);
    }

    /** 测试探针：装配选了哪种表（阈值切换的验收口）。 */
    LookupTable table() {
        return table;
    }
}
