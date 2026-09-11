package com.activity.engine.pipeline;

/**
 * 参与服务（五节点管线的同步入口）。
 * 实现：{@link ParticipateServiceImpl}——编排逻辑与事务边界见实现类注释。
 */
public interface IParticipateService {

    /**
     * 参与一次活动。
     *
     * @param clientRequestId 可选幂等键：带上则重复提交/超时重试安全；不带则每次生成新 bizKey
     */
    ParticipateResult participate(long activityId, long userId, String clientRequestId);
}
