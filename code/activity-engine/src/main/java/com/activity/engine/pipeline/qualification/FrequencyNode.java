package com.activity.engine.pipeline.qualification;

import com.activity.engine.assembly.AssembledActivity;
import com.activity.engine.support.BizCalendar;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 节点 4：频控（日/总次数上限，Redis INCR）——链上第一个写节点。
 * 读前写后公理：排它之前的节点全是纯读，被拒的请求不留下计数。
 * <p>
 * INCR+EXPIRE 必须原子（分开两条命令，进程崩在中间就留下永不过期的 key），
 * 所以走 Lua——与 seckill/pre_deduct.lua 同款手法。
 * 日计数 key 带 dayKey（America/Sao_Paulo），TTL 48h 覆盖跨日，过期即懒重置。
 */
@Component
public class FrequencyNode implements QualificationNode {

    private static final DefaultRedisScript<Long> INCR_WITH_TTL = new DefaultRedisScript<>(
            "local c = redis.call('INCR', KEYS[1]) "
                    + "if c == 1 and tonumber(ARGV[1]) > 0 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end "
                    + "return c",
            Long.class);

    @Resource
    private StringRedisTemplate redis;

    @Override
    public int order() {
        return 4;
    }

    @Override
    public QualificationResult check(AssembledActivity activity, long userId) {
        if (activity.getFreqDayLimit() > 0) {
            String dayKey = "ae:freq:d:" + activity.getActivityId() + ":" + userId + ":" + BizCalendar.dayKey();
            Long count = redis.execute(INCR_WITH_TTL, List.of(dayKey), "172800");
            if (count != null && count > activity.getFreqDayLimit()) {
                return QualificationResult.reject("FREQ_DAY");
            }
        }
        if (activity.getFreqTotalLimit() > 0) {
            String totalKey = "ae:freq:t:" + activity.getActivityId() + ":" + userId;
            Long count = redis.execute(INCR_WITH_TTL, List.of(totalKey), "0");
            if (count != null && count > activity.getFreqTotalLimit()) {
                return QualificationResult.reject("FREQ_TOTAL");
            }
        }
        return QualificationResult.pass();
    }
}
