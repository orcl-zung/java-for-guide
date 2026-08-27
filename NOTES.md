# NOTES.md — 教学工作笔记

## 学员画像
- 钟浩，男，1998.11 生，湖南工商大学计算机科学与技术（2016.09-2020.06），CET-4
- 坐标深圳，2019.06 入行（Java 第六年）
- **2026-08-25 被裁**（公司融资问题，非绩效原因）——注意在教学中保护心态，同时保持任务导向

## 基线自评（2026-08-27，学员手贴实分）
- 实分：项目叙事 2 / 并发 2 / Spring·微服务 2；MySQL·Redis·系统设计·线上排查·MQ·JVM·分布式·AI·算法 全 1
- urgency（权重×2−得分）：MySQL·Redis·系统设计(9) > 项目·并发(8) > 排查·MQ·JVM·分布式·AI(7) > Spring(6) > 算法(5)
- 与实战证据矛盾（四次扣费、OOM、85 项重构评审均亲手做）→ 判定**会做不会讲 + 裁员后信心低谷**，非知识空白，不加重学量
- **学员两条约束（2026-08-27）：① 项目深挖叙事排最后做；② 8/29-30 周末先改简历，8/30 晚投第一批 3-5 家练手维持不变**
- 风险补丁：简历项目描述 = 最小可用故事卡；MQ 课顺带理清四次扣费为项目深挖预热；收到首个面试邀约 → 当天插队做对应 STAR 卡（24h 内）
- 注：学员拒绝翻浏览器 localStorage 取数，改手动贴分——私人目录先征求同意
- 已交付：0002 MySQL 追问链（8/27-8/28 主线，自评 1 → 目标 4，过关后回 0001 自评表改分）；含"链 1 地基"扩展节（对照《MySQL 实战 45 讲》第 1/2/4 讲——用户指定深挖参考，已入 RESOURCES.md）

## 职业时间线（注意：简历上嘉立创写到 2025.10，当前公司经历尚未写入简历）
- 2019.06-2023.01 深圳品沃网络：话费充值交易平台（TPS 500 / QPS 1000，推单/配单）、宝厢智能货柜（IoT / MQTT / 1000+ 设备 / 日均订单数万）
- 2023.03-2025.10 深圳嘉立创：用户增长与互动营销平台（DDD、任务中心、积分商城、RocketMQ 最终一致性、Sentinel、Prometheus+Grafana）
- 2025.10/11-2026.08 当前公司（被裁）：Ingoo 共享充电宝（**巴西出海**，Spring Boot 3.2.2 / Java 17 / Spring Cloud Alibaba 2023）
- 简历文件：/Users/1ea/Downloads/钟浩---Java开发工程师-.pdf；项目：/Users/1ea/IdeaProjects/power-bank

## 现有技术栈（简历自述 + 项目代码核实）
- Java 并发：volatile / synchronized / ReentrantLock / AQS / 线程池（简历自称"精通"——面试必被往死里问）
- Spring 全家桶：SpringBoot / SpringMVC / Mybatis(-Plus)，读过部分 Spring 源码（IoC/AOP）
- 微服务：Spring Cloud Alibaba（Nacos / Gateway / Sentinel）、Sa-Token、SkyWalking（项目里有）
- 数据：MySQL 调优、Oracle、Redis + Redisson
- MQ：RocketMQ（顺序/幂等/积压，简历重点）+ **RabbitMQ（power-bank 实际在用，简历未写）**
- 运维：Linux、Docker + Jenkins、Prometheus + Grafana
- 出海特色：多支付渠道对接（Stripe / Phiz / Global）、巴西市场

## 明显差距点（对比 2026-08 市场 JD，教学优先级输入）
- **AI 工程化**（RAG / Agent / MCP / Spring AI）——2026 热点加分项，完全空白
- Kafka（JD 常见，只有 RocketMQ，可类比迁移讲解）
- Netty / NIO（JD 高频词；IoT 项目 TCP 长连接实战可映射成故事）
- 分库分表（话费项目有"每月数百万数据 + 数据归档"实战，可深挖）
- K8s / 云原生（JD 出现频率上升，Docker 有基础）
- JDK21 虚拟线程（新趋势，掌握话题即可成亮点）

## power-bank 项目面试金矿（已核实真实文档）
- `doc/refactor-proposal-architect-review.md`：85 项问题对抗式交叉核验（61 确认/19 部分/1 反驳），"披着微服务外衣的单体"，止血→安全网→结构解耦三阶段渐进式重构 → **架构师叙事王牌**
- `doc/pay-duplicate-and-no-dispense-analysis.md`：四次扣费事故、RabbitMQ manual-ack、幂等缺失、CAS 幂等修复、连接泄漏 → 幂等/资损防控王牌
- `doc/drive-multi-instance-routing-plan.md`：设备长连接单点 → 多实例路由 → 类 Netty 网关设计题素材
- `doc/广告系统.md`：计费引擎、授信额度、实时暂停投放 → 计费系统设计素材

## 教学偏好
- 中文教学，任务导向：一切服务"拿到 offer"
- 每课短小、可快速完成、有交互反馈（自评/测验类 widget）
- 面试题答案要教"追问链"：一层答案 + 预判的二三层追问
- **冲刺周期：三周**（2026-08-25 → 09-14），不是六周——所有课程安排按三周排布，宁可砍量不拖周期

## 工作区布局约定（对齐 english-learn，用户 2026-08-25 指定）
- 根目录 `index.html` = 课程主页（卡片式课程列表 + Roadmap + 工作区文件入口）
- `assets/`：`style.css`（唯一样式表，含暗色主题与侧边栏样式）、`nav.js`（自动注入侧边栏，**靠 `style.css` 链接推断路径前缀，文件名不能改**）、`theme.js`（明暗切换，首帧前设置 data-theme）、`favicon.svg`
- 根目录 `.nojekyll`：为 GitHub Pages 直出做准备
- **新增课程时的三步登记**：① 文件放入 `lessons/`（或 `reference/`）；② `assets/nav.js` 的 NAV 数组加条目；③ `index.html` 加卡片 + Roadmap 勾选项
- 每个 HTML 的 head 四件套：favicon + style.css + theme.js + nav.js（相对路径按目录深度 `../assets/...`）

## 市场数据快照（2026-08-25 检索，来源见 RESOURCES.md）
- 薪资：猎聘深圳架构师 28-40K（上市集团）；BOSS 微服务架构师 10-15K（小厂）——目标中上区间 25-40K
- JD 高频词：JVM 调优实战、MySQL 分库分表、3 年+分布式实战、高并发架构设计、微服务治理、容器化 CI/CD、可观测性、带教初级
- 面试形式：一面基础八股+算法 → 二面项目深挖（**挂率最高**）→ 三面架构/主管面 → HR 面；每轮 1-3 道算法题
- 2026 新增热点：AI 工程化（RAG 架构、Agent 组件、MCP/Skill 协议、TTFT、Prompt 注入与版本管理）、JDK21 虚拟线程、Spring AI
- 面试官原话：跟简历相关的内容务必多准备；项目相关基础知识是深挖重灾区
