package com.activity.engine.decision;

/**
 * 奖品项。
 *
 * @param awardId          奖品 ID
 * @param weightMillionths 权重（百万分比整数）。整池权重和必须 = 1_000_000，
 *                         "谢谢参与"作为一个奖项补足——概率和校验是装配期职责。
 *                         用整数区间杜绝浮点误差（0.1 + 0.2 != 0.3）。
 */
public record AwardItem(long awardId, int weightMillionths) {

    /** 权重刻度：百万分比。 */
    public static final int SCALE = 1_000_000;
}
