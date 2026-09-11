package com.activity.engine.decision.weighted;

import com.activity.engine.decision.AwardItem;

import java.util.List;

/**
 * 奖池装配校验：整池权重和必须 = 百万分比刻度（"谢谢参与"作为一个奖项补足）。
 * 概率和校验是装配期职责——运行期不再检查，查表永不出界。
 */
final class PoolValidator {

    private PoolValidator() {
    }

    static void requireFullScale(List<AwardItem> pool) {
        long sum = 0;
        for (AwardItem item : pool) {
            if (item.getWeightMillionths() < 0) {
                throw new IllegalArgumentException("负权重: " + item);
            }
            sum += item.getWeightMillionths();
        }
        if (sum != AwardItem.SCALE) {
            throw new IllegalArgumentException(
                    "权重和必须 = " + AwardItem.SCALE + "（谢谢参与补足），实际 = " + sum);
        }
    }
}
