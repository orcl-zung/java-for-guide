package com.activity.engine.assembly;

import com.activity.engine.decision.AwardItem;
import com.activity.engine.decision.DecisionStrategy;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 装配产物：一个活动的运行态对象——配置流的终点（设计稿第五章"装配"的代码形态）。
 * 驻进程内存（多实例各装一份）；失效/重装生产上走 MQ 事件，本地切片用管理端接口手动触发。
 * <p>
 * pool 为决策查表用的权重池（含 MISS 补足项）；missPadding 是装配期补足的
 * "谢谢参与"权重（百万分比），0 = DB 行已配满；fallbackAwardId 为兜底奖
 * （is_fallback=1 的第一行），null = 未配置。
 * <p>
 * 字段多且有可选域（fallbackAwardId），构造走 @Builder——比 14 个位置参数的
 * 构造器调用可读，加字段也不用改所有调用点。
 */
@Getter
@Builder
public class AssembledActivity {

    private final long activityId;
    private final String activityCode;
    private final String activityType;

    /** MUST_HIT/WEIGHTED/PRD_PITY/FIRST_COME——装配期已据此选定 decision，运行期不再分支。 */
    private final String decisionType;

    /** 0 下架 / 1 上架——isLive 的一票否决项。 */
    private final int onOff;

    /** UTC，左闭。 */
    private final LocalDateTime startTime;

    /** UTC，右开。 */
    private final LocalDateTime endTime;

    /** 每日参与上限，0=不限——FrequencyNode 消费。 */
    private final int freqDayLimit;

    /** 总参与上限，0=不限——账户条件更新的限额参数。 */
    private final int freqTotalLimit;

    /** 奖项业务视图：规则树判库存、发奖取 benefitType 走这里。 */
    private final List<AssembledAward> awards;

    /** 决策查表专用权重池，含 MISS 补足项；与 awards 同源不同形。 */
    private final List<AwardItem> pool;

    /** 装配期补足的"谢谢参与"权重（百万分比），0 = DB 行已配满。 */
    private final int missPadding;

    /** 兜底奖（is_fallback=1 的第一行），null = 未配置。 */
    private final Long fallbackAwardId;

    /** 装配期选定的决策策略实现（当前为加权查表，阈值内 O(1) 数组/超阈值 O(logN) CDF）。 */
    private final DecisionStrategy decision;

    /**
     * 运行态派生：status 不落库，由 (on_off, 起止时间, now) 现算——
     * 旧寻宝"存储+惰性"双轨两套真相的根治点。now 传 UTC（与 DB DATETIME 同基准）。
     */
    public boolean isLive(LocalDateTime nowUtc) {
        return onOff == 1 && !nowUtc.isBefore(startTime) && nowUtc.isBefore(endTime);
    }

    /** 线性查找（奖池 ≤ 阈值规模，不值得建 Map）。 */
    public AssembledAward award(long awardId) {
        return awards.stream().filter(a -> a.getAwardId() == awardId).findFirst()
                .orElseThrow(() -> new IllegalStateException("装配态里不存在奖项: " + awardId));
    }
}
