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
- [美团技术团队：MySQL 索引原理及慢查询优化](https://tech.meituan.com/2014/06/30/mysql-index.html)
  2014 年常青文。磁盘 IO 与预读（"页"概念的原文版）、树高公式 h=㏒(m+1)N（链 1 容量计算的原版）、建索引五大原则、带 explain 执行计划的真实慢查询案例——直接喂养链 3（2026-08-28 验证可访问）。
- MySQL 索引优化实战阅读包（2026-09-01 检索命中验证；配套应答手册"索引优化"题的场景认领清单 A-D，认领哪个场景读哪篇）：
  - [JavaGuide：MySQL 索引失效场景总结](https://javaguide.cn/database/mysql/mysql-index-invalidation.html)：SELECT * / 违背最左前缀 / 索引列函数与计算 / 类型转换 / LIKE / OR / IN 使用不当——场景 C 的清单原文（javaguide.cn 为用户指定主参考）。
  - [腾讯云开发者：索引失效的隐形杀手——隐式类型转换](https://developer.cloud.tencent.com/article/2706948)：字符串列传数字导致索引失效与查询结果异常的机理——场景 C 的深挖版。
  - [掘金：九个实验验证联合索引最左匹配原则](https://juejin.cn/post/7283832557502693413)：实验驱动，含"遇到范围查询停止匹配"的边界——场景 A/B 的动手验证版。
  - [博客园：最左前缀匹配原则 + EXPLAIN 命令详解](https://www.cnblogs.com/xuwc/p/14007766.html)：原则与执行计划对照读——"定位→量化→对症→验证"四步法中 explain 环节的配套。
  - [JavaGuide：深度分页介绍及优化建议](https://javaguide.cn/high-performance/deep-pagination-optimization.html)：延迟关联 / 游标分页（范围查询）的选型结论——场景 D 的结论版。
  - [京东云：千万级数据深分页 SQL 性能优化实践](https://developer.jdcloud.com/article/3201)：大厂实战案例（标签记录法/游标）——场景 D 的大厂叙事版。
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

## Wisdom (Communities)

- [牛客网面经区](https://www.nowcoder.com/)
  实时面经流水，可按公司/岗位筛选，另有 AI 模拟面试功能。用于：目标公司投递前情报、模拟面试题源。
- [面试鸭](https://www.mianshiya.com/)
  面试题库 + 高频专题。用于：检索式自测（间隔重复的题源）。
- [掘金 / V2EX](https://juejin.cn/)
  职场与 offer 对比讨论。用于：谈薪阶段的行情参照、offer 选择讨论。

## Gaps
- "AI 工程化 + Java 后端"缺少系统性教程（目前多为散点面经）——后续需专门检索 Spring AI 官方文档与 RAG 实战文章补齐
- 架构师级系统设计系统课（如《System Design Interview》vol 1/2、ByteByteGo）尚未评估引入，待进入系统设计专题时补充
