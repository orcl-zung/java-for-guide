package com.activity.engine.infra.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * activity_participation_record 行（流水层）——重放投影，只取终态两列。
 * 完整列（幂等键/触发通道/时间）见 db/schema.sql；重放语义见
 * pipeline.ParticipateServiceImpl#replay。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRow {

    /** HIT/MISS/REJECT；PENDING 表在途，Service 层翻成 PROCESSING 不回放。 */
    private String result;

    /** 被拒于哪个节点：RISK/BLACKLIST/CROWD/FREQ/ACCOUNT/STOCK/SYSTEM；非拒绝对填空串。 */
    private String rejectNode;
}
