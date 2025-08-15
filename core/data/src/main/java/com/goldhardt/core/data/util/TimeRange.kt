package com.goldhardt.core.data.util

import com.google.firebase.Timestamp
import java.time.YearMonth
import java.time.ZoneId

/** Utility to convert YearMonth to Firestore Timestamp range covering the entire month. */
fun yearMonthToTimestampRange(month: YearMonth, zone: ZoneId = ZoneId.systemDefault()): Pair<Timestamp, Timestamp> {
    val start = month.atDay(1).atStartOfDay(zone).toInstant()
    val end = month.atEndOfMonth().atTime(23, 59, 59, 999_000_000).atZone(zone).toInstant()
    return Timestamp(start.epochSecond, start.nano) to Timestamp(end.epochSecond, end.nano)
}

