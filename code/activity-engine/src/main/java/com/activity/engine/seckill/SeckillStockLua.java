package com.activity.engine.seckill;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * 秒杀预减脚本的加载器（P2 片段：脚本本体见 resources/seckill/pre_deduct.lua）。
 * 生产侧由 FirstCome 决策的异步通道调用（Redis evalsha），切片只保证脚本可加载、语法可读。
 */
public final class SeckillStockLua {

    private SeckillStockLua() {
    }

    public static String load() {
        try (var in = SeckillStockLua.class.getResourceAsStream("/seckill/pre_deduct.lua")) {
            if (in == null) {
                throw new IllegalStateException("pre_deduct.lua 不在 classpath");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
