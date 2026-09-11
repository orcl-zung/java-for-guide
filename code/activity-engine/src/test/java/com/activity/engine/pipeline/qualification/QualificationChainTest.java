package com.activity.engine.pipeline.qualification;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 资格链：节点按成本升序执行 + 首个拒绝短路。
 */
class QualificationChainTest {

    /** 打桩节点：记录自己被调用的次序。 */
    private static QualificationNode stub(int order, boolean pass, List<Integer> calls) {
        return new QualificationNode() {
            @Override
            public int order() {
                return order;
            }

            @Override
            public QualificationResult check(com.activity.engine.assembly.AssembledActivity activity, long userId) {
                calls.add(order);
                return pass ? QualificationResult.pass() : QualificationResult.reject("N" + order);
            }
        };
    }

    @Test
    void nodesRunInCostAscendingOrder() {
        List<Integer> calls = new ArrayList<>();
        // 故意乱序注入，链必须按 order 重排
        QualificationChain chain = new QualificationChain(List.of(
                stub(4, true, calls), stub(0, true, calls), stub(2, true, calls)));
        assertTrue(chain.check(null, 1L).isPassed());
        assertEquals(List.of(0, 2, 4), calls);
    }

    @Test
    void firstRejectShortCircuits() {
        List<Integer> calls = new ArrayList<>();
        QualificationChain chain = new QualificationChain(List.of(
                stub(0, false, calls), stub(1, true, calls)));
        var result = chain.check(null, 1L);
        assertEquals("N0", result.getNode());
        assertEquals(List.of(0), calls, "被拒后下游节点不许再执行（短路）");
    }
}
