package com.activity.engine.infra.mapper;

import com.activity.engine.infra.model.ActivityRow;

/**
 * SQL 在 mapper/ActivityMapper.xml。行映射走显式 resultMap(constructor)，
 * 不再依赖列位置——加列、调列序都不会错位。
 */
public interface ActivityMapper {

    ActivityRow findById(long id);
}
