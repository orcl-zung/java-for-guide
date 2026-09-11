package com.activity.engine.infra.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 次数账户（①资格链尾节点，全链最贵的写）。SQL 在 mapper/AccountMapper.xml。
 * 日/月计数懒重置：day_key/month_key 标记过期视同归零，不跑定时任务。
 */
public interface AccountMapper {

    /**
     * 单语句原子扣减（拒绝先查后改）。
     * v1 配额语义：total_quota=0 视为不限（普通活动靠 Redis 频控扛限量）；
     * quota&gt;0 才强卡（邀请得次数模式，②a 任务充值链路上了再启用）。
     *
     * @return 1=扣减成功；0=配额耗尽或账户不存在
     */
    int deduct(@Param("activityId") long activityId, @Param("userId") long userId,
               @Param("dayKey") String dayKey, @Param("monthKey") String monthKey);

    /** 开户：首次参与落行。唯一键冲突由调用方重试 deduct 吸收。 */
    int openAccount(@Param("activityId") long activityId, @Param("userId") long userId,
                    @Param("dayKey") String dayKey, @Param("monthKey") String monthKey);
}
