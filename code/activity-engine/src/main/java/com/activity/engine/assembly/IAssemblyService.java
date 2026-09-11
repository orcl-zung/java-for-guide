package com.activity.engine.assembly;

/**
 * 装配服务：活动配置 → 运行态对象（含缓存与重装）。
 * 实现：{@link AssemblyServiceImpl}。
 */
public interface IAssemblyService {

    /** 取运行态对象，未装配则现场装配（首次参与懒装配）。 */
    AssembledActivity get(long activityId);

    /** 强制重装（活动发布/编辑/下架后调用；生产走 MQ 事件，本地走管理端接口）。 */
    AssembledActivity reload(long activityId);
}
