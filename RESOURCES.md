# Java 高级开发 / 架构师面试 Resources

## Knowledge

- [JavaGuide（用户指定主参考）](https://javaguide.cn/)
  国内最系统的 Java 面试知识库。用于：八股主线的骨架——Java 基础/并发/JVM/MySQL/Redis/MQ/分布式/微服务/系统设计各专题的第一入口。
- [JavaGuide：Java 后端学习路线（2026 最新版）](https://javaguide.cn/roadmap/java-roadmap.html)
  2026 版已加入 AI 应用开发板块。用于：核对知识覆盖面、安排专题学习顺序。
- [JavaGuide GitHub 仓库](https://github.com/snailclimb/JavaGuide)
  仓库首页有"AI 智能面试辅助平台 + RAG 知识库"实战项目（Spring Boot 4.0 + Java 21 + Spring AI 2.0）。用于：AI 加分项的参考项目选型。
- [小林 coding — 图解 MySQL / 图解网络](https://xiaolincoding.com/)
  图解系列，B+ 树/索引/事务/锁/网络原理讲解质量极高。用于：MySQL 与计算机网络专题的第一参考。
  - [为什么 MySQL 采用 B+ 树作为索引？](https://xiaolincoding.com/mysql/index/why_index_chose_bpuls_tree.html)：二分 → 二叉查找树 → AVL/红黑树 → B 树 → B+ 树的动图推导链，与第 2 课链 1 推理链一一对应（2026-08-28 验证可访问）。
  - [MySQL 架构是怎样的？](https://www.xiaolincoding.com/mysql/architecture/mysql_architecture.html)：自底向上拼出 Server 层 + InnoDB（内存：buffer pool/change buffer/自适应哈希/redo log buffer；磁盘：ibd/undo/redo）全景图——"buffer pool 属于引擎层"问题的出处，对应第 2 课地基①②；buffer pool 三问 + undo/redo 因果链已沉淀为地基⑥（用户提供，2026-09-01 验证可访问）。
  - [事务隔离级别是怎么实现的？](https://www.xiaolincoding.com/mysql/transaction/mvcc.html)：隐藏列 / undo log 版本链 / ReadView 四字段 / 可见性判断规则，全图解——链 4 追问① 的第一图解参考（2026-09-01 检索命中验证）。
  - [可重复读隔离级别，完全解决幻读了吗？](https://www.xiaolincoding.com/mysql/transaction/phantom.html)：快照读 vs 当前读、RR 没完全解决幻读的两个场景——链 4 追问② 的图解版（2026-09-01 检索命中验证）。
- [美团技术团队：MySQL 索引原理及慢查询优化](https://tech.meituan.com/2014/06/30/mysql-index.html)
  2014 年常青文。磁盘 IO 与预读（"页"概念的原文版）、树高公式 h=㏒(m+1)N（链 1 容量计算的原版；逐符号拆解已沉淀为第 2 课地基④，2026-09-01）、建索引五大原则（最左前缀"范围之后全断"的底层拆解已沉淀为第 2 课地基⑤，2026-09-01）、带 explain 执行计划的真实慢查询案例——直接喂养链 3（EXPLAIN 四看法 + stage_poi 低区分度案例复盘已沉淀为第 2 课"链 3 补充"，2026-09-01）（2026-08-28 验证可访问）。
- MySQL 索引优化实战阅读包（2026-09-01 检索命中验证；配套应答手册"索引优化"题的场景认领清单 A-D，认领哪个场景读哪篇）：
  - [JavaGuide：MySQL 索引失效场景总结](https://javaguide.cn/database/mysql/mysql-index-invalidation.html)：SELECT * / 违背最左前缀 / 索引列函数与计算 / 类型转换 / LIKE / OR / IN 使用不当——场景 C 的清单原文（javaguide.cn 为用户指定主参考）。
  - [腾讯云开发者：索引失效的隐形杀手——隐式类型转换](https://developer.cloud.tencent.com/article/2706948)：字符串列传数字导致索引失效与查询结果异常的机理——场景 C 的深挖版。
  - [掘金：九个实验验证联合索引最左匹配原则](https://juejin.cn/post/7283832557502693413)：实验驱动，含"遇到范围查询停止匹配"的边界——场景 A/B 的动手验证版。
  - [博客园：最左前缀匹配原则 + EXPLAIN 命令详解](https://www.cnblogs.com/xuwc/p/14007766.html)：原则与执行计划对照读——"定位→量化→对症→验证"四步法中 explain 环节的配套。
  - [JavaGuide：深度分页介绍及优化建议](https://javaguide.cn/high-performance/deep-pagination-optimization.html)：延迟关联 / 游标分页（范围查询）的选型结论——场景 D 的结论版。
  - [京东云：千万级数据深分页 SQL 性能优化实践](https://developer.jdcloud.com/article/3201)：大厂实战案例（标签记录法/游标）——场景 D 的大厂叙事版。
- MySQL 事务与 MVCC 阅读包（2026-09-01 检索命中验证；链 4 配套深挖材料，按"原理图解 → 文字对照 → 实战案例"三层递进；小林两篇图解见上方小林条目；三概念分野 + 电商事故形态已沉淀为第 2 课"链 4 补充"，2026-09-01）：
  - [JavaGuide：InnoDB 存储引擎对 MVCC 的实现](https://javaguide.cn/database/mysql/innodb-implementation-of-mvcc.html)：隐藏列 / 版本链 / ReadView / 可见性规则推导的文字对照版（javaguide.cn 为用户指定主参考）。
  - [美团技术团队：Innodb 中的事务隔离级别和锁的关系](https://tech.meituan.com/2014/08/20/innodb-lock.html)：隔离级别与锁策略的对应关系——链 4 追问③ 与链 5 的桥梁。
  - [京东云：记一次线上问题引发的对 MySQL 锁机制分析](https://developer.jdcloud.com/article/3424)：大厂线上排查实战叙事——链 5 死锁排查的同类案例。
  - [掘金：一次线上报错引起对 MySQL 间隙锁的研究](https://juejin.cn/post/7090693923370172452)：线上报错 → 间隙锁根因的实战推演。
  - [火山引擎开发者社区：电商库存系统超卖事故的技术复盘与数据防护体系重构](https://developer.volcengine.com/articles/7543213619673366578)：生鲜秒杀超卖 287 单复盘——根因为回调重试无幂等 + 行锁超时 + Redis/DB 不一致；标题无"幻读"，但病因与不可重复读/幻读同族（check 与 act 之间世界已变），防护三原则（幂等/事务闭环/缓存不替库）——链 4/链 5 的业务事故对照（2026-09-01 检索命中并全文验证）。
  - [阿里云开发者社区：深入探讨 MySQL 中的幻读现象：原因、影响及解决方案](https://developer.aliyun.com/article/1311044)：幻读成因与解决方案综述（2026-09-01 检索命中验证，未全文精读）。
  - 配套讲数：45 讲第 3 讲（事务隔离）、第 8 讲（事务到底是隔离的还是不隔离的）、第 20/21 讲（幻读与加锁规则）。
- [《MySQL 实战 45 讲》笔记版（林晓斌专栏，GitBook 镜像）](https://jums.gitbook.io/mysql-shi-zhan-45-jiang)
  用户 2026-08-27 指定的深挖参考。第 1 讲（查询执行五组件）、第 2 讲（更新执行 + redo/binlog + 两阶段提交）、第 4 讲（索引模型对比）已沉淀为第 2 课"链 1 地基"一节。
- [牛客：【面经分享】2026 Java 后端开发面试真题汇总（含 AI 工程方向）](https://www.nowcoder.com/discuss/864594486704291840)
  一线面试官（后端团队负责人，月面 10+ 人）整理：传统八股 + AI 工程化双方向 + 项目深挖套路与加分项清单。用于：考点频率校准、AI 方向题库、面试官视角。
- [知乎：Java 高频面试题总结（2025 最新版，后端通用）](https://zhuanlan.zhihu.com/p/1962956461396173924)
  分析了几百份大中小厂面经的高频题清单。用于：阶段性自测。
- [美团技术团队博客](https://tech.meituan.com/)
  国内一线大厂工程实践深度文章（Java 性能优化、分布式、数据库）。用于：项目深挖时展示"大厂视角"。
- [Java21 虚拟线程实践：框架高并发升级之路 — 腾讯云开发者](https://cloud.tencent.com/developer/article/2640467)
  虚拟线程在 IO 密集高并发场景的落地重构。用于：2026 新趋势话题（JDK 17→21）。
- 本机项目文档：`/Users/1ea/IdeaProjects/power-bank/doc/`
  重构立项书、支付幂等分析、drive 多实例路由、广告计费方案。用于：项目深挖与架构师叙事的真实素材（最高信任级——本人亲历）。
- AI 工程化与 AI 辅助编程阅读包（2026-09-01 检索命中验证；AI 速成课 9/11 的骨架输入，简历技能第 8 条的两层依据）：
  - [JavaGuide：AI 应用开发知识体系](https://javaguide.cn/ai/)
    大模型调用 / Agent / RAG / MCP / Prompt 工程 / 向量数据库 / 评测 / 系统设计——AI 八股第二层的第一入口（javaguide.cn 为用户指定主参考）。
  - [AIGuide（Snailclimb 开源仓库）](https://github.com/Snailclimb/AIGuide)
    AI 应用开发 + AI 编程实战与面试指南（2026 版学习路线）：大模型基础 → LLM API → Prompt → RAG → Agent → 工程化 → 项目实战。
  - [JavaGuide：10 道 AI 编程相关的开放性面试问题](https://javaguide.cn/ai-coding/practices/ai-ide.html)
    Cursor / Claude Code / Trae 使用技巧、Spec Coding 与 Vibe Coding 区别、AI 对后端开发的影响——第一层"你怎么用 AI"的题源。
  - [牛客：7 道 AI 编程高频面试题](https://www.nowcoder.com/discuss/863477807953817600)
    涵盖 Cursor、Claude Code、Skills——第一层标配考点的清单原文。
  - [小林 coding：2026 最全 AI 大模型面试题](https://www.xiaolincoding.com/project/xiaolinnote.html)
    74 道大厂高频题；后端转 AI 应用开发按 Agent → RAG → 工具调用（MCP/网关）顺序补齐——速成顺序的依据。
  - [知乎：2026 年 Java AI 开发实战——Spring AI 完全指南](https://zhuanlan.zhihu.com/p/2026364563268879288)
    Spring AI 2.0 核心概念、Spring AI vs LangChain4j 选型。
  - [paicoding：Spring AI 面试题预测](https://paicoding.com/springai-interview-questions)
    JD 最常问的 Spring AI 题目：统一模型调用 / RAG / Function Calling 与 Spring 生态集成。

## Wisdom (Communities)

- [牛客网面经区](https://www.nowcoder.com/)
  实时面经流水，可按公司/岗位筛选，另有 AI 模拟面试功能。用于：目标公司投递前情报、模拟面试题源。
- [面试鸭](https://www.mianshiya.com/)
  面试题库 + 高频专题。用于：检索式自测（间隔重复的题源）。
- [掘金 / V2EX](https://juejin.cn/)
  职场与 offer 对比讨论。用于：谈薪阶段的行情参照、offer 选择讨论。

## Gaps
- ~~"AI 工程化 + Java 后端"缺少系统性教程~~（2026-09-01 已补：JavaGuide AI 知识体系 + AIGuide + 小林 74 题见上方阅读包；Spring AI 官方文档待速成课时按需补）
- 架构师级系统设计系统课（如《System Design Interview》vol 1/2、ByteByteGo）尚未评估引入，待进入系统设计专题时补充
