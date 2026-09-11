package com.activity.engine.pipeline.qualification;

import com.activity.engine.assembly.AssembledActivity;

/**
 * 资格链节点 SPI（①资格责任链）。
 * <p>
 * 定序两公理：<b>读前写后</b>（前置拒绝不产生写副作用）+ <b>便宜先贵后</b>（成本升序）。
 * 本切片的链：0 装配态（内存读）→ 4 频控（Redis INCR，第一个写）。
 * 次数账户（DB 写）逻辑上排 5 压尾，但物理上由编排器在事务段内调用——见 AccountNode 注释。
 * 扩展位：风控封禁(1) / 黑名单(2) / 人群(3)。
 */
public interface QualificationNode {

    /** 成本序：越小越先执行。 */
    int order();

    QualificationResult check(AssembledActivity activity, long userId);
}
