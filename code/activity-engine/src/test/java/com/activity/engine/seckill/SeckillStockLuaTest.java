package com.activity.engine.seckill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeckillStockLuaTest {

    @Test
    void luaScriptLoads() {
        String script = SeckillStockLua.load();
        assertTrue(script.contains("DECR"), "脚本应含库存预减");
        assertTrue(script.contains("SISMEMBER"), "脚本应含幂等校验");
    }
}
