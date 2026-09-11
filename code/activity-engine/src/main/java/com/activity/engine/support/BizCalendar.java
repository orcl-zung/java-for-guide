package com.activity.engine.support;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 业务日历：日/月边界一律按 America/Sao_Paulo——旧寻宝用 JVM 默认时区，巴西错 3h（缺陷审计①）。
 * DB 的 DATETIME 存 UTC；只有"日/月"归属用业务时区。
 */
public final class BizCalendar {

    public static final ZoneId BIZ_ZONE = ZoneId.of("America/Sao_Paulo");

    private BizCalendar() {
    }

    /** yyyyMMdd */
    public static String dayKey() {
        return LocalDate.now(BIZ_ZONE).format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    /** yyyyMM */
    public static String monthKey() {
        return LocalDate.now(BIZ_ZONE).format(DateTimeFormatter.ofPattern("yyyyMM"));
    }
}
