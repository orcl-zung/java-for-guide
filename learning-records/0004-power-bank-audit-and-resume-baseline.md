# power-bank 全项目亮点盘点 + 简历原文诊断（第 3 课证据档案）

2026-08-29。为简历改版做的两件事：① 通读 power-bank 全部代码与文档（12 模块 Maven 工程）；② 提取简历 PDF 原文 3 页。本档案存证据，供面试前回忆细节用。

## 一、power-bank 技术栈实勘（版本以根 pom 为准）

Java 17 / Spring Boot 3.2.2 / Spring Cloud 2023.0.1 / Spring Cloud Alibaba 2023.0.1.0 / Sa-Token 1.37（四套登录体系：Admin/Agent/Member/Advertiser）/ MyBatis-Plus 3.5.12 / MySQL 8.0 + Druid / Redis + Redisson 3.2.3 / RabbitMQ（Spring AMQP）/ Netty（netty-all）/ Nacos（config + discovery 多 namespace）/ Spring Boot Admin + Micrometer/Prometheus/Grafana/Alertmanager/ELK/Tempo + SkyWalking 9.1.0 / 阿里云 OSS + MinIO / Docker + docker-maven-plugin + 自研滚动发布回滚 shell。

**重要事实修正（面试防翻车）：**
- power-bank **没有 RocketMQ、没有 Kafka、没有 XXL-Job**（RocketMQ/XXL-Job 是嘉立创栈；power-bank 任务全用 @Scheduled + Redisson tryLock 选主）。
- **Sentinel 已弃用**（仅旧 Nacos 配置导出里有；限流走自研 Redis 滑动窗口）。
- Drive 多实例路由：方案文档完整（否决 MQ 广播/Redis Stream），**代码侧只落地了设备状态 Redis 共享缓存，HTTP 内转未上线**——简历/面试一律用"方案已定稿、一期已上线"口径。

## 二、五大王牌叙事（证据路径）

1. **多支付渠道 + 回调可靠性（最强）**：PayChannelStrategyFactory（List 注入自动注册，5 实现：Stripe/MercadoPago/Phiz/GlobalPay/余额）；webhook 快收慢办（验签→insertPayNotifyFlow→MQ→200）；StripePayCallbackConsumer:264-282 消费端 CAS 条件更新幂等；PayCallbackRabbitConfig:96-207（10 队列、24h TTL、统一 DLQ、手动 ACK、并发 3-5/prefetch 5）；Stripe 预授权 auth→capture 两段式（:305-365）；openpay 开放支付平台（AES+验签+防重放+超时关单）。
2. **广告计费引擎**：AdAdvertBillServiceImpl(1378 行)；钱包+授信双资金池、日结 00:20/月结 00:30/小时资金扫描；FOR UPDATE + Redisson 锁 60s；失败账单顺序补扣；余额不足自动暂停+充值自动恢复；授信预警阈值 40%、短信 24h 去重；OpenAPI 四要素鉴权（nonce 300s 防重放）+ 展示点击上报 Redis 累积、10 分钟合并、ZSET 延迟重试。
3. **Netty 设备网关 + 多实例路由**：HgzhServer（端口 6102、帧上限 8192、SO_BACKLOG 1024、读空闲 180s）；长度前缀+JSON+\r 帧尾、DelimiterBasedFrameDecoder + IdleStateHandler；多实例方案文档 doc/Drive 多实例方案.md。
4. **优惠券渐进式重构**：30-40 人天；特征测试→增量 DDL（升级/校验/回滚三件套）→新代码兼容读→新写入→回填→停旧写；22 项定向验证（tasks/coupon-refactor/ 四文档 + a-sql/coupon-refactor/ 12 脚本）；CouponLifecycleCompensationJob（每分钟批 500、锁 15 分钟、计数器 10 分钟对账、Redisson tryLock 选主）。
5. **可观测从 0 到 1**：AlertmanagerWebhookService 桥接（token 鉴权、分级 @人、自动拼 Grafana/Kibana/Tempo 三链接推企微）；PortalMetricsService(367 行) 自定义指标；设备在线率 30s 采集、离线快照 7 天；3 节点生产拓扑（10.224.0.10 监控+MQ / .14 业务+Redis / .4 drive+ELK）。

通用组件（common）：@DistributeLock 注解切面（SpEL、waitTime/expireTime 可配）、Redisson 延迟队列框架、滑动窗口限流、Caffeine+Redis 多级缓存（堆内存 10% 自适应）、敏感数据加解密 AOP、通用 WebSocket。i18n 三语（zh/en/pt，Language header），时区固定 America/Sao_Paulo。发票：NFe.io + LinsGeek 双供应商、CPF/CNPJ、延迟队列重试。

## 三、简历原文诊断（3 页，提取于 2026-08-29）

六大硬伤：① Ingoo（2025.11-2026.08）完全缺席；② 技能栈无 Netty/RabbitMQ/Boot3/Java17/SkyWalking/支付/出海，JD 关键词半数零命中；③ 嘉立创"架构师职责"模板化零实锤；④ 嘉立创段零数字（品沃反而有：1000 台/数万单/TPS500）；⑤ 笔误（Redission ×2、"积分口见"、"后段"）；⑥ 3 页。原文亮点保留：品沃两项目细节扎实、设计模式表述与支付策略模式实锤吻合。

**Implications:** 第 3 课《简历改版实战》交付：新简历全文模板（Ingoo 主打 6 条 + 嘉立创压缩 4 条 + 品沃合并）、JD 对标表、防拷打对照表、三条诚实红线、8 项数字填空（设备量/日订单/新渠道人日/广告主/0 资损时长/故障发现时间/慢SQL P99/嘉立创活动量级）、投递战术（8/30 晚 8 点 3-5 家练手）。docs/（含手机号邮箱的简历 PDF）已加入 .gitignore 不公开。AI 加分位等 W3 Demo 后再加，防现在被拷打。
