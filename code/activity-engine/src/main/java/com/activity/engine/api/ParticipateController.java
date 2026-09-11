package com.activity.engine.api;

import com.activity.engine.assembly.IAssemblyService;
import com.activity.engine.pipeline.IParticipateService;
import com.activity.engine.pipeline.ParticipateResult;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 同步入口（设计稿第三章"触发双入口"的 SYNC 侧；MQ 异步入口是扩展位）。
 */
@RestController
@RequestMapping("/activity")
public class ParticipateController {

    @Resource
    private IParticipateService participateService;

    @Resource
    private IAssemblyService assemblyService;

    /**
     * 参与一次。clientRequestId 可选：带上则获得幂等保护（重复提交/超时重试安全）；
     * 不带则每次生成新 bizKey——等同于放弃去重。
     */
    @PostMapping("/{id}/participate")
    public ParticipateResult participate(@PathVariable long id, @RequestParam long userId,
                                         @RequestParam(required = false) String clientRequestId) {
        try {
            return participateService.participate(id, userId, clientRequestId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    /**
     * 查看装配态奖池：售罄置空 + MISS 补足后的真实分布。
     */
    @GetMapping("/{id}/pool")
    public PoolView pool(@PathVariable long id) {
        try {
            return PoolView.of(assemblyService.get(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    /**
     * 强制重装配：生产上 MQ 事件驱动重装的本地降级入口。
     */
    @PostMapping("/{id}/assemble")
    public PoolView assemble(@PathVariable long id) {
        try {
            return PoolView.of(assemblyService.reload(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}
