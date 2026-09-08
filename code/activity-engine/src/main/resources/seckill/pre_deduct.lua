-- 秒杀库存预减（P2 片段，FirstCome 决策的异步通道第一跳）
-- KEYS[1] = 库存 key（活动发布时 SET 为可抢数量）
-- KEYS[2] = 用户参与标记 key（幂等：一人一次）
-- 返回：1=预减成功  0=已抢完  -1=重复参与
-- 原子性：Redis 单线程执行 Lua，check-and-set 无竞态
-- 兜底纪律：Redis 只是削峰，最终一致性由 DB 条件更新（stock > 0）+ 对账任务保证

if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -1
end

local stock = tonumber(redis.call('GET', KEYS[1]) or '-1')
if stock <= 0 then
    return 0
end

redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
return 1

-- 少卖回补：超时未支付由定时任务驱动，反向执行 INCR + SREM（幂等键=订单号）
