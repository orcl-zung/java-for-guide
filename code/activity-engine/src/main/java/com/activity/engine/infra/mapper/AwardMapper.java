package com.activity.engine.infra.mapper;

import com.activity.engine.infra.model.AwardRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SQL 在 mapper/AwardMapper.xml。
 */
public interface AwardMapper {

    /** ORDER BY id 保序——同一库两次装配产出同一张查找表。 */
    List<AwardRow> listByActivity(@Param("activityId") long activityId);

    /**
     * 库存最终防线：条件更新，只在有限库存奖项上调用（stock_total=0 为不限库存，跳过）。
     *
     * @return 影响行数：1=扣成功，0=售罄
     */
    int deductStock(@Param("awardId") long awardId);
}
