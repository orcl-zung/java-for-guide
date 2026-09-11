package com.activity.engine.pipeline.award;

/**
 * 发奖 SPI（④统一发奖中心）：只管"给谁发、发什么、发没发成"（幂等、落账）。
 * 券怎么用（锁券/核销/满减）留在 coupon 域——引擎编排 + 领域自治。
 * 新增权益类型 = 新增实现类，实现接口即接入。
 */
public interface AwardIssuer {

    /** 能否承接该权益类型（COUPON/FRAGMENT/PHYSICAL/POINTS/CASH…）。 */
    boolean supports(String benefitType);

    void issue(IssueContext ctx);
}
