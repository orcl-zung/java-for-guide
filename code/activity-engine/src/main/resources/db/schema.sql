-- ============================================================================
-- 活动引擎 · 存储设计 V1（设计稿 lessons/0005 第五章落地）
--
-- 命名规约（为什么不是 act_*）：
--   - 弃缩写 act_：Java 生态 ACT_* 是 Activiti 工作流引擎的保留前缀
--     （ACT_RE_*/ACT_RU_*），撞认知；全单词可读性也更好
--   - 域前缀全单词 activity_，三层语法，看表名知层：
--       配置层  activity / activity_task / activity_award  裸名词：运营写，装配缓存读
--       账户层  activity_account / activity_progress       用户×活动累计态，条件更新
--       流水层  *_record                                   append-only 事实，唯一键幂等
--   - 主表不带冗余角色词；从表 = 主表名 + 角色
--
-- 四条原则（每条都有旧寻宝缺陷做反面教材）：
--   1. 维度即表：配置列拒绝 JSON 大字段——旧寻宝 startTaskParam JSON 实存裸数字
--      （parseInviteRequired 的 try-catch 是事故现场）
--   2. 状态派生不存储：activity 无 status 列，运行态 = f(on_off, start, end, now)
--      ——旧寻宝"存储+惰性"双轨两套真相是缺陷审计①头号
--   3. 幂等靠唯一索引：所有写入口以 (activity_id, user_id, biz_key) 撞键兜底
--   4. 写路径全部条件更新（UPDATE ... WHERE 存量/额度校验），
--      不 SELECT ... FOR UPDATE——最终防线是 DB 条件更新（第六章双通道共识）
--
-- 全局约定：
--   - InnoDB / utf8mb4 / utf8mb4_bin（bizKey 精确匹配，不吃大小写折叠）
--   - 金额一律分（巴西 BRL centavos），杜绝浮点
--   - DATETIME 存 UTC；"日/月"边界由应用层按 America/Sao_Paulo 换算成
--     day_key/month_key 标记列——旧寻宝用 JVM 默认时区，巴西错 3h
--   - 主键自增（教学从简；量产后换雪花不影响任何设计点）
-- ============================================================================

SET NAMES utf8mb4;

-- ============================== 配置层 ==============================

-- ----------------------------------------------------------------------------
-- 1. activity 活动主表（五维公式"一行"的持久化）
-- ----------------------------------------------------------------------------
CREATE TABLE activity (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    activity_code    VARCHAR(64)   NOT NULL COMMENT '业务编码，运营可读',
    activity_name    VARCHAR(128)  NOT NULL COMMENT '活动名',
    activity_type    VARCHAR(32)   NOT NULL COMMENT '玩法：LOTTERY抽奖/TREASURE寻宝/COUPON_SEND发券/SIGN_IN签到/SECKILL秒杀…',
    trigger_type     VARCHAR(16)   NOT NULL COMMENT '触发方式：ACTIVE主动/EVENT行为事件/SCHEDULE定时/GEO地理位置——决定走同步入口还是MQ异步入口',
    event_type       VARCHAR(32)   NOT NULL DEFAULT '' COMMENT 'trigger_type=EVENT 时匹配的行为：REGISTER/FIRST_ORDER/LOGIN/PAY；其余为空串',
    decision_type    VARCHAR(32)   NOT NULL COMMENT '决策模式：MUST_HIT/WEIGHTED/PRD_PITY/FIRST_COME——装配期据此选 DecisionStrategy 实现',
    crowd_type       VARCHAR(32)   NOT NULL DEFAULT 'ALL' COMMENT '人群约束：ALL/NEW_USER/OLD_USER/WHITELIST（名单进Redis集合）——资格链节点3',
    freq_day_limit   INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '频控：每日参与上限，0=不限。运行计数在Redis INCR——资格链节点4',
    freq_total_limit INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '频控：总参与上限，0=不限',
    budget_total     BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '活动预算（分），0=不限——缺陷审计②新增：旧寻宝无预算控制',
    budget_used      BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已用预算（分）。发放前校验+条件更新：UPDATE ... SET budget_used=budget_used+#{amt} WHERE id=#{} AND (budget_total=0 OR budget_used+#{amt}<=budget_total)',
    on_off           TINYINT       NOT NULL DEFAULT 0 COMMENT '上架开关：0下架/1上架。与起止时间共同派生运行态，不设status列',
    start_time       DATETIME      NOT NULL COMMENT '开始时间（UTC）',
    end_time         DATETIME      NOT NULL COMMENT '结束时间（UTC）',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_code (activity_code),
    KEY idx_shelf_window (on_off, start_time, end_time) COMMENT '运营后台列表用；运行态读装配缓存，不回表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='活动主表';

-- ----------------------------------------------------------------------------
-- 2. activity_task 任务配置（1对N）——五维之"任务进度"
--    无任务玩法零行，②a 空任务直穿
-- ----------------------------------------------------------------------------
CREATE TABLE activity_task (
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    activity_id        BIGINT UNSIGNED NOT NULL COMMENT '所属活动',
    task_type          VARCHAR(32)   NOT NULL COMMENT 'CHECKIN打卡/INVITE邀请/ORDER支付/COLLECT集卡…',
    progress_semantics VARCHAR(16)   NOT NULL COMMENT '进度语义：COUNTER累加/COLLECTION收集(带item_key)/STREAK连续——三种语义覆盖累积家族',
    target_value       INT UNSIGNED  NOT NULL COMMENT '目标值：N人/N次/N张卡（COLLECTION=去重item_key数）',
    streak_reset_policy VARCHAR(16)  NOT NULL DEFAULT 'NONE' COMMENT '断签策略：NONE/RESET归零，仅STREAK有意义',
    recharge_count     INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '达成后反向充值次数账户的额度（邀请得抽奖次数），0=不充值——账户闭环',
    created_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务配置表';

-- ----------------------------------------------------------------------------
-- 3. activity_award 奖品定义（1对N）——五维之"权益类型" + "约束·库存"
--    权重百万分比整数杜绝浮点；装配期校验 Σweight=1_000_000（谢谢参与补足）
-- ----------------------------------------------------------------------------
CREATE TABLE activity_award (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    activity_id     BIGINT UNSIGNED NOT NULL COMMENT '所属活动',
    award_name      VARCHAR(128)   NOT NULL COMMENT '奖品名',
    benefit_type    VARCHAR(16)    NOT NULL COMMENT '权益类型：COUPON券/FRAGMENT碎片/PHYSICAL实物/POINTS积分/CASH红包——决定④哪个AwardIssuer接单',
    benefit_ref_id  BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '权益引用：券模板ID等；FRAGMENT时即碎片item_key',
    weight_million  INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '权重（百万分比）。MUST_HIT玩法只配兜底奖一行=1_000_000',
    pity_c_million  INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT 'PRD的C值（百万分比），0=非PRD；硬保底N=⌈1_000_000/C⌉由装配期推导，不落库',
    stock_total     INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '总库存，0=不限（谢谢参与/兜底奖常配0）',
    stock_remaining INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '剩余库存——最终防线：UPDATE ... SET stock_remaining=stock_remaining-1 WHERE id=#{} AND stock_remaining>0（stock_total=0跳过校验）',
    is_fallback     TINYINT        NOT NULL DEFAULT 0 COMMENT '是否兜底奖：1=决策选中奖无库存时的降级出口（③规则树库存节点）',
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='奖品定义表';

-- ============================== 流水层 ==============================

-- ----------------------------------------------------------------------------
-- 4. activity_award_record 发奖流水（幂等防线）
--    biz_key 三段语义：抽奖=第N次序号 / 行为事件=behaviorKey / 手动领取=clientRequestId
--    挡的是"同一逻辑请求的重复投递"（MQ重投/客户端带clientRequestId重试）；
--    纯双击=两次参与，参与即扣不退（第六章次数语义）
-- ----------------------------------------------------------------------------
CREATE TABLE activity_award_record (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    activity_id   BIGINT UNSIGNED NOT NULL,
    user_id       BIGINT UNSIGNED NOT NULL,
    biz_key       VARCHAR(64)     NOT NULL COMMENT '幂等键第三段，语义见表头',
    award_id      BIGINT UNSIGNED NOT NULL COMMENT '实际发出的奖（兜底降级也记真实奖项，可追溯）',
    benefit_type  VARCHAR(16)     NOT NULL DEFAULT '' COMMENT '发放时快照——防奖品配置事后被改导致对账失真',
    issue_status  VARCHAR(16)     NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAILED/PENDING——异步通道(FirstCome)先PENDING落库后由MQ兜底翻转',
    fail_reason   VARCHAR(128)    NOT NULL DEFAULT '' COMMENT '失败原因（issuer域异常等），补偿依据',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '仅异步通道状态翻转会动',
    PRIMARY KEY (id),
    UNIQUE KEY uk_idempotent (activity_id, user_id, biz_key) COMMENT '重复请求/MQ重投/客户端重试全被这一个索引挡住',
    KEY idx_user_awards (user_id, activity_id, created_at) COMMENT '我的奖品列表',
    KEY idx_activity_recon (activity_id, created_at) COMMENT '对账：发放计数 vs 流水（第六章）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='发奖流水表';

-- ----------------------------------------------------------------------------
-- 5. activity_participation_record 参与流水（未中奖也落库：审计/风控/漏斗）
--    与发奖流水职责分工：参与=请求事实（含被拒），发奖=权益事实
-- ----------------------------------------------------------------------------
CREATE TABLE activity_participation_record (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    activity_id     BIGINT UNSIGNED NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    biz_key         VARCHAR(64)     NOT NULL COMMENT '与发奖流水同源——一次参与0..1次发奖，两表按此键对得上',
    trigger_channel VARCHAR(8)      NOT NULL COMMENT '双入口来源：SYNC同步API/MQ行为事件',
    result          VARCHAR(16)     NOT NULL COMMENT 'HIT中奖/MISS未中/REJECT被拒',
    reject_node     VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '被拒于哪个管线节点：RISK/BLACKLIST/CROWD/FREQ/ACCOUNT/STOCK——漏斗分析定位列',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_idempotent (activity_id, user_id, biz_key) COMMENT '参与侧同键兜底；拒绝也占号，重放直接读已落库结果',
    KEY idx_activity_result (activity_id, result, created_at) COMMENT '运营漏斗粗查；明细分析走数仓，不在线库加宽索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='参与流水表';

-- ============================== 账户层 ==============================

-- ----------------------------------------------------------------------------
-- 6. activity_account 次数账户（①资格链尾节点——全链最贵的写，压尾）
--    日/月计数用"日期标记懒重置"：不跑定时任务，条件更新里比对标记，
--    过期标记视同归零（与"状态派生不存储"同一哲学：能推导就不存中间态）
-- ----------------------------------------------------------------------------
CREATE TABLE activity_account (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    activity_id BIGINT UNSIGNED NOT NULL,
    user_id     BIGINT UNSIGNED NOT NULL,
    total_quota INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '总额度——任务达成可充值↑（activity_task.recharge_count）',
    total_used  INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '总已用',
    day_used    INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '当日已用（day_key过期视为0）',
    day_key     CHAR(8)       NOT NULL DEFAULT '' COMMENT 'yyyyMMdd（America/Sao_Paulo），空=从未用过',
    month_used  INT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '当月已用（month_key过期视为0）',
    month_key   CHAR(6)       NOT NULL DEFAULT '' COMMENT 'yyyyMM',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_user (activity_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='次数账户表';

-- 扣减（单语句原子，拒绝先查后改；限额来自装配配置 activity.freq_*，作参数传入）：
--   UPDATE activity_account
--     SET total_used = total_used + 1,
--         day_used   = IF(day_key   = #{dayKey}, day_used + 1, 1),
--         month_used = IF(month_key = #{monthKey}, month_used + 1, 1),
--         day_key = #{dayKey}, month_key = #{monthKey}
--   WHERE activity_id = #{activityId} AND user_id = #{userId}
--     AND (#{totalLimit} = 0 OR total_used < #{totalLimit})
--     AND (#{dayLimit}   = 0 OR day_key   <> #{dayKey}   OR day_used   < #{dayLimit})
--     AND (#{monthLimit} = 0 OR month_key <> #{monthKey} OR month_used < #{monthLimit});
-- 影响行数=0 → 资格链尾节点拒绝。账户行不存在 → 先 INSERT ... ON DUPLICATE KEY UPDATE 开户。
-- 充值（任务达成反向）：UPDATE ... SET total_quota = total_quota + #{n} WHERE ...

-- ----------------------------------------------------------------------------
-- 7. activity_progress 进度账户（②a 任务进度 + PRD 的 N——五维模型自洽点）
--    不叫 activity_task_progress：PRD_N 行本就不是任务进度，名字去掉 task 更准
--    ★ 唯一索引陷阱：MySQL 唯一索引允许多个 NULL 并存，task_id/item_key
--      若可空则唯一约束形同虚设——一律用 0 / '' 占位，不用 NULL
-- ----------------------------------------------------------------------------
CREATE TABLE activity_progress (
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    activity_id        BIGINT UNSIGNED NOT NULL,
    user_id            BIGINT UNSIGNED NOT NULL,
    progress_type      VARCHAR(16)     NOT NULL COMMENT 'COUNTER/COLLECTION/STREAK/PRD_N',
    task_id            BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '任务配置ID；PRD_N行=0（距上次中奖与任务无关）',
    item_key           VARCHAR(64)     NOT NULL DEFAULT '' COMMENT 'COLLECTION=卡面/碎片key；PRD_N=奖品ID；其余空串',
    counter_value      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '计数（PRD_N行=距上次命中次数N，命中重置）',
    current_streak     INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '当前连击，仅STREAK用',
    last_progress_date CHAR(8)         NOT NULL DEFAULT '' COMMENT 'yyyyMMdd：STREAK断签判定（昨→连击+1，今→已打卡，更早→按streak_reset_policy）',
    achieved_count     INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '已兑现次数：达成判定 counter_value >= (achieved_count+1)*target——可重复任务免清零',
    created_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_progress (activity_id, user_id, progress_type, task_id, item_key) COMMENT 'COLLECTION集齐判定=该键前缀COUNT(*)>=target，索引白送'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='进度账户表';

-- ----------------------------------------------------------------------------
-- 量级与演进（不提前建设，只留判断依据）：
--   - 两张流水表是增长极：日均万级营销量 → 月百万行，单表无压力；
--     千万级再谈按月归档/分区，分库分表不作
--   - 私有存储判据（设计稿第五章）：宝箱实例/团/箱=共享实体才配私有表；
--     GEO/ZSET/秒杀预减 key 是 Redis 结构，不占 DB 名额
--   - 本表集合的检验标准：新增签到/砍价/拼团玩法，默认零新表
-- ----------------------------------------------------------------------------
