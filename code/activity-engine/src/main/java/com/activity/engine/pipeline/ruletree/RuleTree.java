package com.activity.engine.pipeline.ruletree;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 规则树执行器：按 order 逐个过节点，产出最终发奖决定。
 */
@Component
public class RuleTree {

    @Resource
    private List<RuleNode> nodes;

    public RuleTree() {
    }

    /** 测试装配口：手工传入节点即排好序。 */
    public RuleTree(List<RuleNode> nodes) {
        this.nodes = sorted(nodes);
    }

    @PostConstruct
    void sortNodes() {
        this.nodes = sorted(this.nodes);
    }

    private static List<RuleNode> sorted(List<RuleNode> nodes) {
        return nodes.stream().sorted(Comparator.comparingInt(RuleNode::order)).toList();
    }

    public RuleOutcome apply(RuleContext ctx) {
        RuleOutcome outcome = RuleOutcome.hit(ctx.getAwardId());
        for (RuleNode node : nodes) {
            outcome = node.apply(ctx, outcome);
        }
        return outcome;
    }
}
