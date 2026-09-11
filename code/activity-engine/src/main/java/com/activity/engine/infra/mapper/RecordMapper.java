package com.activity.engine.infra.mapper;

import com.activity.engine.infra.model.ParticipationRow;
import org.apache.ibatis.annotations.Param;

/**
 * 流水写入：参与流水（含被拒/未中）+ 发奖流水。SQL 在 mapper/RecordMapper.xml。
 * 两表唯一键 (activity_id, user_id, biz_key) 是幂等最终防线——
 * insert 撞 DuplicateKeyException 即重放，由调用方转查询返回。
 */
public interface RecordMapper {

    /** 参与流水占号：初始 PENDING，管线末端翻转终态。 */
    int insertParticipation(@Param("activityId") long activityId, @Param("userId") long userId,
                            @Param("bizKey") String bizKey, @Param("channel") String channel);

    ParticipationRow findParticipation(@Param("activityId") long activityId, @Param("userId") long userId,
                                       @Param("bizKey") String bizKey);

    /** 终态翻转按唯一键定位——不占号方不需要先查出 id。 */
    int updateParticipationResult(@Param("activityId") long activityId, @Param("userId") long userId,
                                  @Param("bizKey") String bizKey, @Param("result") String result,
                                  @Param("rejectNode") String rejectNode);

    int insertAwardRecord(@Param("activityId") long activityId, @Param("userId") long userId,
                          @Param("bizKey") String bizKey, @Param("awardId") long awardId,
                          @Param("benefitType") String benefitType);

    Long findAwardIdByBizKey(@Param("activityId") long activityId, @Param("userId") long userId,
                             @Param("bizKey") String bizKey);
}
