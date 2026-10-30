package com.goldhardt.core.data.util

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** Utility to convert YearMonth to Instant range covering the entire month. */
fun yearMonthToInstantRange(month: YearMonth, zone: ZoneId = ZoneId.systemDefault()): Pair<Instant, Instant> {
    val start = month.atDay(1).atStartOfDay(zone).toInstant()
    val end = month.atEndOfMonth().atTime(23, 59, 59, 999_000_000).atZone(zone).toInstant()
    return start to end
}

