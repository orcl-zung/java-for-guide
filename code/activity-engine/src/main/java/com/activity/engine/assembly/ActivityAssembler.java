package com.activity.engine.assembly;

import com.activity.engine.decision.AwardItem;
import com.activity.engine.decision.DecisionStrategy;
import com.activity.engine.decision.weighted.WeightedTableDecision;
import com.activity.engine.infra.mapper.ActivityMapper;
import com.activity.engine.infra.mapper.AwardMapper;
import com.activity.engine.infra.model.ActivityRow;
import com.activity.engine.infra.model.AwardRow;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * 装配器：从 MySQL 配置表读出运行态对象（第五章"配置流"的落地）。
 * <p>
 * 装配期三件事，每件对应一条设计原则：
 * <ol>
 *   <li>读 {@code activity} + {@code activity_award}——维度即表，无 JSON 解析；</li>
 *   <li>过滤库存耗尽的奖项（stock_total &gt; 0 且 stock_remaining = 0）——
 *       ③库存联动"区间置空"的静态版：被置空的权重并入"谢谢参与"，其余奖项概率不动
 *       （不做归一化放大——售罄不该让剩下的大奖变便宜）；</li>
 *   <li>权重和补足到 1_000_000：差额由装配器补 MISS，DB 不必存填充行；
 *       溢出（&gt; SCALE）直接拒绝装配——配置错误宁早勿晚。</li>
 * </ol>
 * 表结构见 {@code src/main/resources/db/schema.sql}。
 */
@Component
public class ActivityAssembler {

    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private AwardMapper awardMapper;

    private final RandomGenerator random = RandomGenerator.getDefault();

    /**
     * 装配加权抽奖：读库 → 售罄置空 → MISS 补足 → 阈值切表。
     */
    public AssembledActivity assemble(long activityId) {
        ActivityRow act = activityMapper.findById(activityId);
        if (act == null) {
            throw new IllegalArgumentException("活动不存在: id=" + activityId);
        }

        List<AssembledAward> awards = new ArrayList<>();
        List<AwardItem> pool = new ArrayList<>();
        long sum = 0;
        Long fallbackId = null;
        for (AwardRow row : awardMapper.listByActivity(activityId)) {
            if (row.getStockTotal() > 0 && row.getStockRemaining() <= 0) {
                continue; // 售罄置空：权重留给 MISS 补足，其他奖概率不动
            }
            AssembledAward award = new AssembledAward(row.getId(), row.getAwardName(), row.getBenefitType(), row.getWeightMillion(), row.getStockTotal(), row.getIsFallback() == 1);
            awards.add(award);
            pool.add(new AwardItem(award.getAwardId(), award.getWeightMillionths()));
            sum += award.getWeightMillionths();
            if (award.isFallback() && fallbackId == null) {
                fallbackId = award.getAwardId();
            }
        }

        if (pool.isEmpty()) {
            throw new IllegalStateException("奖池为空或全部售罄: activityId=" + activityId);
        }
        if (sum > AwardItem.SCALE) {
            throw new IllegalStateException("权重和溢出: " + sum + " > " + AwardItem.SCALE
                    + "，配置错误宁早勿晚，拒绝装配");
        }
        int padding = (int) (AwardItem.SCALE - sum);
        if (padding > 0) {
            pool.add(new AwardItem(DecisionStrategy.MISS, padding));
        }

        return AssembledActivity.builder()
                .activityId(act.getId())
                .activityCode(act.getActivityCode())
                .activityType(act.getActivityType())
                .decisionType(act.getDecisionType())
                .onOff(act.getOnOff())
                .startTime(act.getStartTime())
                .endTime(act.getEndTime())
                .freqDayLimit(act.getFreqDayLimit())
                .freqTotalLimit(act.getFreqTotalLimit())
                .awards(List.copyOf(awards))
                .pool(List.copyOf(pool))
                .missPadding(padding)
                .fallbackAwardId(fallbackId)
                .decision(WeightedTableDecision.assemble(pool, random))
                .build();
    }
}
