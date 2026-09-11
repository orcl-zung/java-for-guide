package com.activity.engine.assembly;

import lombok.Value;

/**
 * 装配态奖品：配置行 + 运行期所需的全部字段（比 decision.AwardItem 多业务维度）。
 */
@Value
public class AssembledAward {

    long awardId;
    String awardName;

    /** 权益类型——④发奖时据此挑 AwardIssuer。 */
    String benefitType;

    /** 百万分比权重，装配期决策查表直接消费。 */
    int weightMillionths;

    /** 0 = 不限库存（谢谢参与/兜底奖常配）——规则树跳过条件更新直接放行。 */
    int stockTotal;

    /** true = 决策选中奖无库存时的降级出口（③规则树兜底节点）。 */
    boolean fallback;

    /** 不限库存？true → 规则树跳过条件更新直接放行。 */
    public boolean unlimitedStock() {
        return stockTotal == 0;
    }
}
