# activity-engine —— 活动引擎代码切片

配套设计稿：《活动引擎设计》（lessons/0005，九章结构，烤问会 17 问决策记录见 NOTES.md 2026-09-08 条目）。

本仓库只含**零公司代码**的算法与管线切片，可公开。

## 一天时间盒（砍单顺序与原因）

| 优先级 | 内容 | 预算 | 砍单逻辑 |
|---|---|---|---|
| P0 | 加权区间查表（O1 数组直查 / OLogN 二分，按池规模切换） | 3h | 不可砍——抽奖决策主力 |
| P0 | PRD 伪随机 + 硬保底（C 值二分反解 + P(N)=C×N） | 3h | 不可砍——寻宝稀有碎片优化叙事核心 |
| P1 | 红包二倍均值法 | 1.5h | 时间不够先砍——最简单、无项目锚点、可后补 |
| P2 | 秒杀 Redis Lua 预减片段 | 1.5h | 降级为片段——秒杀是架构不是算法，完整漏斗不进一天 |

## 铁律：每个算法必须带分布统计测试

- 加权/PRD：10 万+ 次采样，实际频率与配置分布误差在容差内
- PRD 加断：**最大连空次数 = 保底次数**（一次都不许超）
- 红包：总额守恒（分毫不差）+ 每人最小 1 分 + 期望公平

面试话术目标："我不仅实现了，还用统计测试验证过分布正确性。"

## 结构

```
src/main/java/com/activity/engine/
  decision/               决策 SPI（②b 节点）
    DecisionStrategy      决策接口
    DecisionContext       决策上下文
    AwardItem             奖品项（权重=百万分比整数，杜绝浮点）
    weighted/             P0 加权区间查表（O1 / OLogN + 规模阈值切换）
    prd/                  P0 PRD 伪随机 + 硬保底
    luckymoney/           P1 二倍均值红包
  seckill/                P2 库存预减 Lua 片段
src/main/resources/
  db/schema.sql           存储设计 DDL（第五章落地：7 表 + 条件更新/懒重置 SQL 注释）
  seckill/pre_deduct.lua  P2 Lua 脚本本体
src/test/java/.../
  DistributionAssert      统计测试工具（已就绪）
  *Test                   验收契约（@Disabled，实现一个点亮一个）
```

## 运行

```bash
./mvnw test          # 或 IntelliJ 直接跑
```

测试默认全 @Disabled——每完成一个算法，删掉对应 @Disabled，让灯变绿。
