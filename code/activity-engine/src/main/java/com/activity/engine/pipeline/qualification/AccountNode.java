package com.activity.engine.pipeline.qualification;

import com.activity.engine.assembly.AssembledActivity;
import com.activity.engine.infra.mapper.AccountMapper;
import com.activity.engine.support.BizCalendar;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * 次数账户扣减——逻辑上是资格链节点 5（DB 写，全链最贵，压尾）；
 * 物理上<b>不占链位</b>，由编排器在事务段内调用：扣减必须与扣库存/发奖流水同事务，
 * 系统异常整体回滚，次数自然退还（第六章"补偿三段"①）。
 * <p>
 * v1 配额语义：total_quota=0 不限（普通活动限量由 Redis 频控扛）；
 * quota&gt;0 强卡（邀请得次数模式，随 ②a 任务充值链路启用）。
 */
@Component
public class AccountNode {

    @Resource
    private AccountMapper accountMapper;

    /**
     * 扣减一次参与额度。先条件更新（已有账户的fast path）；
     * 影响 0 行时分两种：账户不存在 → 开户（并发开户撞唯一键 → 重走扣减）；账户在但配额尽 → 拒绝。
     */
    public boolean tryDeduct(AssembledActivity activity, long userId) {
        long activityId = activity.getActivityId();
        String dayKey = BizCalendar.dayKey();
        String monthKey = BizCalendar.monthKey();
        if (accountMapper.deduct(activityId, userId, dayKey, monthKey) == 1) {
            return true;
        }
        try {
            accountMapper.openAccount(activityId, userId, dayKey, monthKey);
            return true;
        } catch (DuplicateKeyException e) {
            return accountMapper.deduct(activityId, userId, dayKey, monthKey) == 1;
        }
    }
}
