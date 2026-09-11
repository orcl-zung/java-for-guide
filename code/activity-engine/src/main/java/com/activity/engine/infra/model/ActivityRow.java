package com.activity.engine.infra.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * activity 主表行（配置层）。注意没有 status 字段——运行态派生不存储。
 * 普通类 + setter：MyBatis 按属性名映射，列位置彻底脱敏。
 * 列语义详见 db/schema.sql，此处只标 Java 侧消费者关心的单位与哨兵值。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRow {

    private long id;

    /** 业务编码（uk_activity_code），运营可读。 */
    private String activityCode;

    private String activityName;

    /** 玩法：LOTTERY/TREASURE/COUPON_SEND/SIGN_IN/SECKILL。 */
    private String activityType;

    /** ACTIVE/EVENT/SCHEDULE/GEO——决定走同步入口还是 MQ 异步入口。 */
    private String triggerType;

    /** triggerType=EVENT 时匹配的行为（REGISTER/FIRST_ORDER/LOGIN/PAY）；其余空串占位。 */
    private String eventType;

    /** MUST_HIT/WEIGHTED/PRD_PITY/FIRST_COME——装配期据此选 DecisionStrategy 实现。 */
    private String decisionType;

    /** ALL/NEW_USER/OLD_USER/WHITELIST——资格链人群节点。 */
    private String crowdType;

    /** 每日参与上限，0=不限。 */
    private int freqDayLimit;

    /** 总参与上限，0=不限。 */
    private int freqTotalLimit;

    /** 预算上限（BRL 分），0=不限。 */
    private long budgetTotal;

    /** 已用预算（BRL 分），发放前条件更新扣减。 */
    private long budgetUsed;

    /** 0 下架 / 1 上架——运行态派生的一票否决项。 */
    private int onOff;

    /** UTC，左闭。 */
    private LocalDateTime startTime;

    /** UTC，右开。 */
    private LocalDateTime endTime;
}
