package com.activity.engine.infra.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * activity_award 奖品行（配置层）。权重百万分比整数杜绝浮点；
 * 装配期校验 Σweight ≤ 1_000_000，差额由"谢谢参与"补足。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AwardRow {

    private long id;

    private long activityId;

    private String awardName;

    /** COUPON/FRAGMENT/PHYSICAL/POINTS/CASH——决定④哪个 AwardIssuer 接单。 */
    private String benefitType;

    /** 权益引用：券模板ID等；FRAGMENT 时即碎片 item_key；无引用=0（占位不 NULL）。 */
    private long benefitRefId;

    /** 权重（百万分比）。MUST_HIT 玩法只配兜底奖一行=1_000_000。 */
    private int weightMillion;

    /** PRD 的 C 值（百万分比），0=非 PRD；硬保底 N=⌈1_000_000/C⌉ 装配期推导，不落库。 */
    private int pityCMillion;

    /** 总库存，0=不限（谢谢参与/兜底奖常配 0）。 */
    private int stockTotal;

    /** 剩余库存——最终防线是条件更新 stock_remaining>0，不是先查后改。 */
    private int stockRemaining;

    /** 1=兜底奖：决策选中奖无库存时的降级出口（③规则树）。int 而不用 boolean，与 TINYINT 直对。 */
    private int isFallback;
}
