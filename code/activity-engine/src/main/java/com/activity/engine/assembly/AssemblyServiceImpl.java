package com.activity.engine.assembly;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 装配缓存：运行态对象驻进程内存（多实例各装一份）。
 * 失效策略——生产：活动变更发 MQ 事件驱动重装（秒级生效）；本地切片：管理端接口手动触发。
 * 运行中库存/次数等状态变化只改 Redis/DB 计数，不回装（第五章配置流/状态流分离）。
 */
@Service
public class AssemblyServiceImpl implements IAssemblyService {

    @Resource
    private ActivityAssembler assembler;

    private final ConcurrentHashMap<Long, AssembledActivity> cache = new ConcurrentHashMap<>();

    @Override
    public AssembledActivity get(long activityId) {
        return cache.computeIfAbsent(activityId, assembler::assemble);
    }

    @Override
    public AssembledActivity reload(long activityId) {
        AssembledActivity assembled = assembler.assemble(activityId);
        cache.put(activityId, assembled);
        return assembled;
    }
}
