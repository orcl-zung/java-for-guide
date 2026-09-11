package com.activity.engine.pipeline;

import com.activity.engine.assembly.AssembledAward;
import lombok.Value;

/**
 * 参与结果（同步入口的返回）。
 * status：HIT 中奖 / MISS 未中（含兜底兜不住）/ REJECT 被拒 / PROCESSING 重放时原请求仍在途；
 * duplicated = true 表示本次是幂等重放，返回的是已落库的结果。
 */
@Value
public class ParticipateResult {

    public static final String HIT = "HIT";
    public static final String MISS = "MISS";
    public static final String REJECT = "REJECT";
    public static final String PROCESSING = "PROCESSING";

    String status;
    Long awardId;
    String awardName;
    String rejectNode;
    boolean duplicated;
    String bizKey;

    public static ParticipateResult hit(AssembledAward award, boolean duplicated, String bizKey) {
        return new ParticipateResult(HIT, award.getAwardId(), award.getAwardName(), "", duplicated, bizKey);
    }

    public static ParticipateResult miss(String bizKey, boolean duplicated) {
        return new ParticipateResult(MISS, null, null, "", duplicated, bizKey);
    }

    public static ParticipateResult reject(String node, String bizKey, boolean duplicated) {
        return new ParticipateResult(REJECT, null, null, node, duplicated, bizKey);
    }

    public static ParticipateResult processing(String bizKey) {
        return new ParticipateResult(PROCESSING, null, null, "", true, bizKey);
    }
}
