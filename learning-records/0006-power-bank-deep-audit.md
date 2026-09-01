# power-bank 深挖审计 · 三报告总装（第 3 课证据档案 v2）

2026-09-01。学员点名要挖透：设备网关指令交互、订单全链路、优惠券+营销。三路并行扫描（设备指令 63 次工具调用 / 订单链路 76 次 / 遗漏亮点+优惠券 81 次），本档案存结论，完整追问脚本已入 reference/0002。

## 一、亮点清单 v2（五大王牌 → 十二张牌）

| # | 牌 | 星级 | 状态 |
|---|---|---|---|
| 1 | 多支付渠道策略 + webhook 快收慢办 + CAS 幂等 | ★★★★★ | 原王牌 |
| 2 | openpay 开放平台（HMAC 验签 + AES 全加密 + IP 白名单 + nonce 防重放 + 幂等回放 + 超时关单） | ★★★★★ | **升级**：原塞在 #1 里，独立成题（OpenPayServiceImpl 1293 行） |
| 3 | **设备指令交互全时序**（不对称协议 + flowId 半异步 + 四元组回扫） | ★★★★★ | **新增**（本档案二） |
| 4 | **指令可靠性取舍**（协议层零重试 + 业务层三重兜底 + 掉宝"以物为锚"补偿） | ★★★★★ | **新增**（本档案二） |
| 5 | **订单状态机与链路**（二维正交 + 三状态机分离 + is_finish 闸门 + 归还四来源收敛） | ★★★★★ | **新增**（本档案三） |
| 6 | **计价引擎 + 分账**（纯函数+快照 + 两级分润 + Redisson 延迟队列 T+n） | ★★★★ | **新增**（本档案三） |
| 7 | 广告计费引擎（双资金池 + 三级任务 + 双锁） | ★★★★ | 原王牌 |
| 8 | **营销活动体系**（LBS 寻宝 + 防作弊四层 + 碎片合成 + 抽奖空壳扩展位） | ★★★★ | **新增**（本档案四，big-market 糅合落点） |
| 9 | 优惠券渐进式重构（特征测试 + 三件套 SQL + ot/ops 双轨现状证据） | ★★★★ | 原王牌，补充：双轨并行是"现状混乱"的直接代码证据 |
| 10 | Netty 网关 + 多实例路由方案 | ★★★★ | 原王牌，**帧格式纠错见下** |
| 11 | 可观测体系从 0 到 1 | ★★★★ | 原王牌 |
| 12 | 自研滚动发布/回滚（Nacos 预检→健康门禁 180s→Nacos 注册确认→链式版本指针留 5 版） | ★★★ | 低频高质，运维向岗位用 |

次级弹药：代理商四层 B 端（H5 店员 ThreadLocal 租户上下文 + 数据权限拦截器）、NFe 发票双供应商（异常分级 + 指数退避 + CPF/CNPJ/CEP 回填）、钱包提现（多 channel 多币种 + 日限额）、Sa-Token 四登录（Redis hash 权限热更新 + Google OAuth 双模式/Apple sub 防误合并）、OSS+MinIO 双策略（DB 配置驱动动态切换）、WebSocket 组件（场景单一并入广告）、i18n（枚举内嵌三语——只当取舍题）。

## 二、设备指令交互（报告 1 精要）

- **协议不对称**（已纠 reference/0002 旧错）：上行 = JSON + `\n` 切帧（DelimiterBasedFrameDecoder(8192, \n)，超长帧断连）；下行 = `%06d` 六位十进制长度前缀 + JSON + `\r`。字段单字母 key（R 指令码/D 设备/G 流水号/L 电池列表，电池项 B1-B5 编码/插槽/电量/电线故障/电池故障——B4/B5 有 0/1 语义翻转适配）。
- **指令集**：上行 I 长心跳 / T 开机首包 / F 短心跳（只续 Redis 省流量）/ B 租赁回执 / H 归还上报 / J 检修回执 / Q 强制心跳回 / D 日志回执；下行 B 弹宝 / J 检修弹出 / R 重启整机 / P 重启程序 / Q 强制心跳 / D 日志开关 / L 拉日志（设备 3-5 分钟后 POST zip 回传）/ N+T=W 广告刷新。Z 注册/R 重启回执注释有 case 无——静默丢弃，注册实际靠首条心跳。
- **flowId 半异步**（无 Future）：雪花 flowId 随 G 字段下发，HTTP 同步返回 flowId（语义="指令已写进 socket"），设备回执带回 G，drive 用它回扫借出电量快照（rentElectricityCache，唯一应答式资源，注释诚实承认断连残留边界），portal 用 bankNo+deviceNo+channel+driveFlowId 四元组定位订单 CAS 置确认。
- **连接管理**：五张本地 ConcurrentHashMap；sendMessage 三重防御（连接存在→isActive→180s 内活跃）；channelInactive 清表删 Redis 发断连 MQ，比对 clientId 防旧连接误删新映射；B4/B5 布尔语义翻转两处出口统一。
- **在线状态**：Redis key `device:HGZH:{deviceNo}` TTL 180s 与 IdleStateHandler 同源；在线率采集 = 单线程 ScheduledExecutor 每 30s 快照 → Micrometer MultiGauge（6 个 gauge 按 channel/device_no 打 tag），离线保留 7 天；旧 @Scheduled 清理 job 整体注释废弃（演进痕迹）。设备心跳节奏可由服务端 E="3" 字段控制（3 短 1 长）。
- **EventLoop 保护**：重业务（DB/MQ）全丢 taskExecutor（5 核心/8 最大/1024 队列）。
- **Code review 谈资**：①readTimeoutCountMap 是 static（理论串扰风险）②RabbitMqConfig 的 BANK_INFO_CHANGE_EXCHANGE Bean 误绑 DEVICE exchange 名（功能未受影响）③租借前置校验"查不到设备跳过拦截"（宁可漏拦不可误杀的取舍）。

## 三、订单全链路（报告 2 精要）

- **状态机**：`ORDER_STATUS` 二维正交（十位=是否已支付 1/2，个位=设备结果 0 弹宝失败/1 未归还/2 已归还 → 10/11/12/20/21/22，初始 21）；押金 `DEPOSIT_TYPE`、分账 `SETTLE_STATUS`、售后 `AfterSalesStatusEnum` 三套独立；完成 = 独立布尔 `is_finish`，`update ... set is_finish=1 where is_finish!=1` 乐观闸门 + @DistributeLock(订单号)。无状态机框架，纯枚举+条件 UPDATE。
- **下单**：扫码直租（非选柜选孔）：用户锁（一人一单）→ Feign 读 drive 内存实时态校验（电量阈值 MIN_RENT_POWER 参数化）→ 押金/预授权校验 + payNo 查重 → 设备级分布式锁 + REQUIRES_NEW + 3 次重试 CAS 占孔（`set is_rent=true where is_rent!=true`；5 分钟内未归还宝跳过防误分配）→ 锁券 → 三表落库（oms_order / oms_order_pay 含计价快照 JSON 列 settlement_po / oms_order_device 含 driveFlowId）→ 事务提交后异步弹宝（先落单后弹宝）。
- **计价**：纯函数 + 快照（OrderUtil 静态无 DB 依赖可单测）；三种模式 = 普通（自然日切分逐天 ceil 计费 + 日封顶）/ 阶梯（前 N 单位递增价）/ 时段（秒级累加、跨段边界 <30s 豁免）；费率按商户 DB 配置（FeeSetDetailPO）；券三策略（免费分钟/金额/满减，OrderCouponCalculator 分发）；币种 BRL。
- **归还**：四来源收敛一个幂等入口 orderBankReturnMqDeal——DEVICE_NOTIFY 设备推送（主）/ USER_SEARCH 用户查询触发 / JOB_SYN 3 分钟 job 按 DB 宝归属反推（伪造消息走同一 MQ 入口）/ DEVICE_HEART_SYN 心跳 J/H 变化推出（30s 防抖）；条件 UPDATE is_return 防重；resolveBillingTime 机器时间优先 + 时钟漂移校验回退服务器时间。
- **结算支付**：归还即结算（先享后付）。双支付模型一个字段分流：preAuthNo 有值走信用卡预授权 capture（金额>0 发 MQ 异步扣款，**扣款成功回调再进 finishOrder 才真正完成——return 是资金安全闸门**；金额=0 直接取消授权）；余额单走冻结多退少补（SQL 原子 bal = bal + (freeze - consume)）→ 押金 RETURNING → 20-50 分钟 job 退回。充值成功回调自动下单弹仓（10 分钟窗口防迟到重复回调——巴西"先扫码支付再弹宝"典型场景）。
- **分账**：分润即记账、结算即入账两阶段。利润 = 消费额 − 渠道手续费；代理商佣金 = 利润 × 网点业务 commission；商户 = 代理商佣金 × 商户 profitShareRatio（两层 DB 配置）；金额 setScale(4, DOWN) 防超分；渠道结算回调 + Redisson 延迟队列 T+n（wlt_billing_cycle 账期配置，商户再晚 60s）到期入账（DistributeLock + SQL 原子加余额 + 变动流水）；退款发生把明细置 INVALID 拦截入账。
- **异常单**：①弹宝失败（驱动错误码）→ 翻转失败态 + 0 元完成 + 全额退；机器错误不退款等人工。②**掉宝补偿（最精巧）**：同宝被下一单分配到 → 反推上一单"未完成未归还超 30 分钟"异常 → afterSalesStatus 条件 UPDATE 抢占 CAS（and (null or NONE or REFUND_FAIL)）→ 0 元完成 + 退押金，失败 REFUND_FAIL + 微信告警。③卡宝/超时：5 分钟 job 按快照重算 needFinish（押金耗尽）自动封顶完成。④客诉退款（admin）：校验链（消费 0 不退/未完成不退/累计≤消费/信用卡只全退/**已过渠道分账禁在线退**）→ afterCommit Feign 回调 portal 原路退 → 退满自动恢复券 → 30 分钟~7 天结果同步 job 收敛。
- **对账边界（诚实口径）**：无传统三方文件对账 job；用四件套替代——状态链串联（is_finish→settle_state→pay_flow→income_details 每环条件 UPDATE）+ 支付结果主动收敛（payResultSynJob 3 分钟查渠道补 webhook 丢失）+ 渠道结算同步（stripe/mercadoPagoSettlementSynJob 按结算时间拉单）+ 异常量阈值告警。被追问改进方向：渠道账单下载逐笔 diff、payout 批次对总账。

## 四、营销活动体系 + big-market 糅合裁决（报告 3 核心结论）

**项目里真实存在完整 ops 运营活动模块（LBS 寻宝）**：Redis GEO 宝箱点位 + 宝箱等级 + 四类任务（到店打卡/拍照打卡/邀请好友/每日签到）+ 奖励 = 优惠券（CouponRewardIssueStrategy：原子扣库存防超发 + 6 位核销码冲突重试 5 次）或实物大奖碎片合成（GrandPrizeRewardIssueStrategy：碎片原子扣减 + find-or-create + 唯一键 DuplicateKeyException 兜底并发 + 会员快照隔离配置变更）+ 商家 H5 / admin 双端核销 + 大奖 CPF 实名 + 防作弊四层（服务端 Haversine 距离校验不信前端坐标 / 时间窗+nonce+HMAC 防重放 / 600s 失败 10 次封禁 1h / 每日限额+点位冷却）+ 四组策略接口族（ActivityStrategy/RewardStrategy/RewardIssueStrategy/ClaimTask+OpenTask）+ 工厂。

**抽奖 = 真实空壳**：SPIN 任务类型仅枚举占位、RandomDrawRewardStrategy 是 TODO。**糅合裁决**：不编"做过抽奖"，讲"预留扩展位 + 我的设计"——big-market 的模板方法编排/规则树前置/库存补偿/统一 award 发放作为设计输出，零穿帮。脚本已入 reference/0002。

**没有的**：独立积分体系、邀请返现裂变（邀请只在宝箱助力内）、余额充值营销。**ot/ops 双轨并行**（老活动 OtActBaseInfo vs 新寻宝）= 优惠券重构叙事"现状混乱"的直接代码证据。

## 五、对已有材料的修正

1. reference/0002 Netty 题：旧脚本"6 位长度前缀+JSON+\r"仅对下行成立，上行是 `\n` 分隔——已改；补充长短心跳、channelInactive 双向映射清理等细节。
2. "多实例路由"题维持原样（方案文档口径不变）；现在有代码级根因：五张实例本地 Map。
3. 嘉立创对账题中"出海项目落地核对层一部分"表述经报告 2 证实（payResultSynJob 3 分钟主动查渠道），成立。

**Implications:** ①这批材料直接喂 W2 并发（EventLoop 保护/线程池/CAS 族谱）与 W3 MQ/Netty 专题；②分账延迟队列 = Redisson 延迟队列第二案例（与发票重试并列）；③学员面试自我介绍可加一句"订单-设备-资金三域都摸过"——这是全栈链路叙事；④openpay 若 JD 强调开放平台/安全可临时升为简历 bullet。
