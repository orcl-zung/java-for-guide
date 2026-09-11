package com.activity.engine.pipeline.qualification;

import lombok.Value;

/**
 * 资格校验结果：通过 or 被拒（带节点名——参与流水 reject_node 列的取值，漏斗分析靠它）。
 */
@Value
public class QualificationResult {

    /** 单例通过结果，避免每次参与都分配对象（static 字段，不是组件）。 */
    static final QualificationResult PASS = new QualificationResult(true, "");

    boolean passed;
    String node;

    public static QualificationResult pass() {
        return PASS;
    }

    public static QualificationResult reject(String node) {
        return new QualificationResult(false, node);
    }
}
