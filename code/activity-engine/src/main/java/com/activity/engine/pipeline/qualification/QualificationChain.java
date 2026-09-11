package com.activity.engine.pipeline.qualification;

import com.activity.engine.assembly.AssembledActivity;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 资格责任链：按节点成本序装配，首个拒绝即短路。
 * Spring 注入全部 QualificationNode 实现，按 order() 排序——
 * 新增节点（风控/黑名单/人群）只需加 @Component 实现，链自动纳入。
 */
@Component
public class QualificationChain {

    @Resource
    private List<QualificationNode> nodes;

    /** 无参构造 + 字段注入是 Spring 路径；排序在 @PostConstruct 统一完成。 */
    public QualificationChain() {
    }

    /** 测试装配口：手工传入节点即排好序。 */
    public QualificationChain(List<QualificationNode> nodes) {
        this.nodes = sorted(nodes);
    }

    @PostConstruct
    void sortNodes() {
        this.nodes = sorted(this.nodes);
    }

    private static List<QualificationNode> sorted(List<QualificationNode> nodes) {
        return nodes.stream().sorted(Comparator.comparingInt(QualificationNode::order)).toList();
    }

    public QualificationResult check(AssembledActivity activity, long userId) {
        for (QualificationNode node : nodes) {
            QualificationResult result = node.check(activity, userId);
            if (!result.isPassed()) {
                return result;
            }
        }
        return QualificationResult.pass();
    }

    /** 测试探针：链上节点顺序（成本升序的验收口）。 */
    List<QualificationNode> nodes() {
        return nodes;
    }
}
