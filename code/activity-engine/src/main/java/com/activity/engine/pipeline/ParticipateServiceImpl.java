package com.activity.engine.pipeline;

import com.activity.engine.assembly.AssembledActivity;
import com.activity.engine.assembly.AssembledAward;
import com.activity.engine.assembly.IAssemblyService;
import com.activity.engine.decision.DecisionContext;
import com.activity.engine.decision.DecisionStrategy;
import com.activity.engine.infra.mapper.RecordMapper;
import com.activity.engine.infra.model.ParticipationRow;
import com.activity.engine.pipeline.award.AwardIssuer;
import com.activity.engine.pipeline.award.IssueContext;
import com.activity.engine.pipeline.qualification.AccountNode;
import com.activity.engine.pipeline.qualification.QualificationChain;
import com.activity.engine.pipeline.qualification.QualificationResult;
import com.activity.engine.pipeline.ruletree.RuleContext;
import com.activity.engine.pipeline.ruletree.RuleOutcome;
import com.activity.engine.pipeline.ruletree.RuleTree;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

/**
 * 参与编排器：五节点管线的同步入口实现（设计稿第三章）。
 * <pre>
 * 占号（唯一索引幂等）→ 装配 → ①资格链（内存/Redis，事务外）
 *   → ②b 决策（纯函数）→ ②a 空任务直穿（v1 无任务节点）
 *   → 事务段：[①尾 次数账户扣减 + ③规则树 库存/兜底 + ④发奖流水 + 参与流水终态]
 * </pre>
 * 事务边界的设计依据（第六章）：
 * <ul>
 *   <li>"扣了库存没写成流水"由同事务回滚解决；系统异常回滚，次数自然退还；</li>
 *   <li>Redis 频控计数不可回滚——事务外先执行，允许失败时的轻微多计（频控本来就是近似语义，
 *       宁可多挡不可少挡）；DB 是最终一致性依据；</li>
 *   <li>决策是纯函数，放在事务外，不白占事务时长。</li>
 * </ul>
 */
@Service
public class ParticipateServiceImpl implements IParticipateService {

    @Resource
    private IAssemblyService assemblyService;
    @Resource
    private QualificationChain qualificationChain;
    @Resource
    private AccountNode accountNode;
    @Resource
    private RuleTree ruleTree;
    /**
     * 全部 AwardIssuer 实现按类型收全量，运行时按 benefitType 挑选。
     */
    @Resource
    private List<AwardIssuer> issuers;
    @Resource
    private RecordMapper recordMapper;
    @Resource
    private PlatformTransactionManager txManager;

    private TransactionTemplate txTemplate;

    @PostConstruct
    void init() {
        this.txTemplate = new TransactionTemplate(txManager);
    }

    @Override
    public ParticipateResult participate(long activityId, long userId, String clientRequestId) {
        String bizKey = (clientRequestId == null || clientRequestId.isBlank()) ? UUID.randomUUID().toString() : clientRequestId;

        // 幂等占号：唯一索引撞键即重放，读已落库结果直接返回
        try {
            recordMapper.insertParticipation(activityId, userId, bizKey, "SYNC");
        } catch (DuplicateKeyException e) {
            return replay(activityId, userId, bizKey);
        }

        try {
            AssembledActivity act = assemblyService.get(activityId);

            // ① 资格链（装配态/频控——内存与 Redis 节点，事务外）
            QualificationResult q = qualificationChain.check(act, userId);
            if (!q.isPassed()) {
                markResult(activityId, userId, bizKey, ParticipateResult.REJECT, q.getNode());
                return ParticipateResult.reject(q.getNode(), bizKey, false);
            }

            // ②b 决策（纯函数）
            long awardId = act.getDecision().decide(new DecisionContext(activityId, userId, act.getPool()));

            // 事务段
            return txTemplate.execute(status -> finishInTx(act, userId, bizKey, awardId));
        } catch (DataAccessException e) {
            // 事务已回滚，参与流水不能停在 PENDING——翻成终态让重放拿得到结果
            markResultQuietly(activityId, userId, bizKey, ParticipateResult.REJECT, "SYSTEM");
            throw e;
        }
    }

    /**
     * 事务段：①尾 账户扣减 → ③ 规则树 → ④ 发奖 → 参与流水终态。
     */
    private ParticipateResult finishInTx(AssembledActivity act, long userId, String bizKey, long awardId) {
        if (!accountNode.tryDeduct(act, userId)) {
            markResult(act.getActivityId(), userId, bizKey, ParticipateResult.REJECT, "ACCOUNT");
            return ParticipateResult.reject("ACCOUNT", bizKey, false);
        }
        if (awardId == DecisionStrategy.MISS) {
            // 参与即扣：未中不退次数（第六章次数语义）
            markResult(act.getActivityId(), userId, bizKey, ParticipateResult.MISS, "");
            return ParticipateResult.miss(bizKey, false);
        }

        RuleOutcome outcome = ruleTree.apply(new RuleContext(act, userId, awardId));
        if (outcome.getAwardId() == DecisionStrategy.MISS) {
            // 兜底也兜不住：记 SOLD_OUT 便于漏斗区分"概率未中"与"库存未中"
            markResult(act.getActivityId(), userId, bizKey, ParticipateResult.MISS, "SOLD_OUT");
            return ParticipateResult.miss(bizKey, false);
        }

        AssembledAward award = act.award(outcome.getAwardId());
        issuerFor(award.getBenefitType()).issue(new IssueContext(act, userId, bizKey, award));
        markResult(act.getActivityId(), userId, bizKey, ParticipateResult.HIT, "");
        return ParticipateResult.hit(award, false, bizKey);
    }

    /**
     * 重放：按已落库终态回放结果；PENDING = 原请求仍在途，让客户端稍后重试。
     */
    private ParticipateResult replay(long activityId, long userId, String bizKey) {
        ParticipationRow row = recordMapper.findParticipation(activityId, userId, bizKey);
        if (row == null) {
            return ParticipateResult.processing(bizKey);
        }
        return switch (row.getResult()) {
            case ParticipateResult.HIT -> {
                Long awardId = recordMapper.findAwardIdByBizKey(activityId, userId, bizKey);
                AssembledAward award = assemblyService.get(activityId).award(awardId);
                yield ParticipateResult.hit(award, true, bizKey);
            }
            case ParticipateResult.MISS -> ParticipateResult.miss(bizKey, true);
            case ParticipateResult.PROCESSING, "PENDING" -> ParticipateResult.processing(bizKey);
            default -> ParticipateResult.reject(row.getRejectNode(), bizKey, true);
        };
    }

    private AwardIssuer issuerFor(String benefitType) {
        return issuers.stream().filter(i -> i.supports(benefitType)).findFirst()
                .orElseThrow(() -> new IllegalStateException("没有承接该权益类型的 Issuer: " + benefitType));
    }

    private void markResult(long activityId, long userId, String bizKey, String result, String rejectNode) {
        recordMapper.updateParticipationResult(activityId, userId, bizKey, result, rejectNode);
    }

    private void markResultQuietly(long activityId, long userId, String bizKey, String result, String rejectNode) {
        try {
            markResult(activityId, userId, bizKey, result, rejectNode);
        } catch (DataAccessException ignored) {
            // 连终态都写不进（库挂了）——留给对账任务扫 PENDING 超时单（第六章对账）
        }
    }
}
