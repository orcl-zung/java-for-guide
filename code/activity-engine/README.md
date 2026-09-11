# activity-engine —— 活动引擎代码切片

配套设计稿：《活动引擎设计》（lessons/0005，九章结构，烤问会 17 问决策记录见 NOTES.md 2026-09-08 条目）。

本仓库只含**零公司代码**的算法与管线切片，可公开。

## 一天时间盒（砍单顺序与原因）

| 优先级 | 内容 | 预算 | 砍单逻辑 |
|---|---|---|---|
| P0 | 加权区间查表（O1 数组直查 / OLogN 二分，按池规模切换）✅ 4 灯绿 | 3h | 不可砍——抽奖决策主力 |
| P0 | PRD 伪随机 + 硬保底（C 值二分反解 + P(N)=C×N）✅ 4 灯绿 | 3h | 不可砍——寻宝稀有碎片优化叙事核心 |
| P1 | 红包二倍均值法 | 1.5h | 时间不够先砍——最简单、无项目锚点、可后补 |
| P2 | 秒杀 Redis Lua 预减片段 | 1.5h | 降级为片段——秒杀是架构不是算法，完整漏斗不进一天 |

## 铁律：每个算法必须带分布统计测试

- 加权/PRD：10 万+ 次采样，实际频率与配置分布误差在容差内
- PRD 加断：**最大连空次数 = 保底次数**（一次都不许超）
- 红包：总额守恒（分毫不差）+ 每人最小 1 分 + 期望公平

面试话术目标："我不仅实现了，还用统计测试验证过分布正确性。"

## 结构

工程约定（国内主流四件套）：Service 层 `IXxxService + XxxServiceImpl` 接口拆分；注入一律 `@Resource`
（按名优先、按型兜底；需手工装配的单测组件如 QualificationChain 保留构造器注入——取舍可讲）；
SQL 一律 XML；样板代码一律 Lombok（不可变 `@Value` / 行对象 `@Data` / 多字段装配产物 `@Getter + @Builder`）。

```
src/main/java/com/activity/engine/
  ActivityEngineApplication   Spring Boot 启动类（@MapperScan infra.mapper）
  decision/                   决策 SPI（②b 节点）
    DecisionStrategy          决策接口
    DecisionContext           决策上下文
    AwardItem                 奖品项（权重=百万分比整数，杜绝浮点）
    weighted/                 P0 加权区间查表（O1 / OLogN + 规模阈值切换）✅ 已实现
    prd/                      P0 PRD 伪随机 + 硬保底 ✅ 已实现
    luckymoney/               P1 二倍均值红包
  seckill/                    P2 库存预减 Lua 片段
  assembly/                   装配：MySQL 配置表 → 运行态对象（售罄置空 + MISS 补足）
    IAssemblyService          装配服务接口（get 懒装配 / reload 重装）
    AssemblyServiceImpl       进程内缓存（生产 MQ 重装的本地降级：POST /activity/{id}/assemble）
    ActivityAssembler         读 activity + activity_award 装配（售罄置空 + MISS 补足）
    AssembledActivity         运行态对象：isLive 派生态 / 频控限额 / 兜底奖
  pipeline/                   五节点管线（切片 #2 最小闭环）
    IParticipateService       参与服务接口
    ParticipateServiceImpl    编排：占号幂等 → 资格链 → 决策 → 事务段（账户+库存+发奖+终态）
    qualification/            ① 资格链：装配态(0) → Redis Lua 频控(4)；账户(5)压尾进事务
    ruletree/                 ③ 规则树：库存条件更新(1) → 兜底奖(2)
    award/                    ④ 发奖 SPI：RecordAwardIssuer 落流水（券/实物委托为扩展位）
  infra/
    model/                    行对象（@Data 普通类；无 status 字段——派生不存储）
    mapper/                   MyBatis Mapper 接口（SQL 全在 resources/mapper/*.xml）
  api/                        ParticipateController 同步入口 + 装配查看/重装
  support/                    BizCalendar（America/Sao_Paulo 日/月边界）
src/main/resources/
  application.yml             默认连本机 docker：mysql@13306 / redis@6379（env 可覆盖）
  mapper/*.xml                SQL 与 resultMap(constructor) 显式列映射
  db/schema.sql               存储设计 DDL（第五章落地：7 表 + 条件更新/懒重置 SQL 注释）
  seckill/pre_deduct.lua      P2 Lua 脚本本体
docker-compose.yml            可选：一键起 mysql+redis（本机已有实例则不用）
src/test/java/.../
  DistributionAssert          统计测试工具
  *Test                       验收契约（@Disabled，实现一个点亮一个）
```

## 运行

```bash
./mvnw test                # 纯单测，不依赖 MySQL/Redis
./mvnw spring-boot:run     # 起服务（默认 8088；8080 被本机 rocketmq 占了）
```

默认连接本机 docker（mysql root/root@13306、redis 密码 123456@6379），不同就用环境变量覆盖：
`ACTIVITY_DB_URL / ACTIVITY_DB_USER / ACTIVITY_DB_PASSWORD / ACTIVITY_REDIS_HOST / ACTIVITY_REDIS_PORT / ACTIVITY_REDIS_PASSWORD / SERVER_PORT`

## 玩一遍最小闭环

```bash
curl -s http://localhost:8088/activity/1/pool            # 装配态奖池（售罄置空+MISS补足后的真实分布）
curl -s -X POST 'http://localhost:8088/activity/1/participate?userId=888'                  # 抽一发
curl -s -X POST 'http://localhost:8088/activity/1/participate?userId=999&clientRequestId=req-42'   # 带幂等键
curl -s -X POST 'http://localhost:8088/activity/1/participate?userId=999&clientRequestId=req-42'   # 重放：duplicated=true，结果一致
curl -s -X POST http://localhost:8088/activity/1/assemble        # 改库配置后强制重装配（生产=MQ事件）
```

已验证的演示路径：日频控 5 次第 6 发拒 `FREQ_DAY`；把 iphone 权重改 100% 连抽 4 发 = 中 2 次售罄 → 后 2 发兜底线 points10（流水记真实奖项）。
演示数据见会话记录或按 schema.sql 注释自插；flow 三表（participation/award_record/account）TRUNCATE 即重置。

## 踩坑记（都是追问弹药）

- **record + MyBatis = 构造器位置映射陷阱**：MyBatis 对 record 走构造器**位置**映射，表有列而 record 没声明就整体错位
  （crowd_type 被塞进 freq_day_limit，'ALL' 转 int 炸 DataConversionException）——最终方案：弃 record，
  不可变载体用 Lombok `@Value`、行对象用 `@Data`（setter 属性名映射，列序彻底脱敏），SQL 全入 XML。
- **固定种子的统计测试必须真跑过才算数**：加权契约原种子 42 在 ±50 容差下 2.2σ 翻车（932/1000），12 种子验证无偏后校准为 0。
- **docker exec 不传 stdin 要加 -i**：`docker exec mysql mysql < file.sql` 静默吞掉 heredoc，数据根本没进去。

加权 4 灯、PRD 4 灯已绿（C 对照表/长期命中率/硬保底上界/多用户隔离，8 种子复验无偏）；红包仍 @Disabled——每完成一个算法，删掉对应 @Disabled，让灯变绿。
