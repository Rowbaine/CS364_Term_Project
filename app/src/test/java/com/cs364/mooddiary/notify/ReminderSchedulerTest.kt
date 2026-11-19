package com.cs364.mooddiary.notify

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** ทดสอบการคำนวณเวลาเตือนรอบถัดไป (ไม่ต้องใช้เครื่อง/emulator) */
class ReminderSchedulerTest {
    private val zone = ZoneId.of("Asia/Bangkok")
    private val everyDay = (1..7).toSet()

    private fun at(text: String): ZonedDateTime = LocalDateTime.parse(text).atZone(zone)
    private fun next(now: String, minutes: Int, days: Set<Int>) =
        ReminderScheduler.nextTrigger(at(now), minutes, days)?.toLocalDateTime()?.toString()

    // 2026-11-19 เป็นวันพฤหัสบดี (4)

    @Test fun beforeTodaysTime_remindsToday() =
        assertEquals("2026-11-19T20:00", next("2026-11-19T10:00", 20 * 60, everyDay))

    @Test fun afterTodaysTime_remindsTomorrow() =
        assertEquals("2026-11-20T20:00", next("2026-11-19T20:30", 20 * 60, everyDay))

    @Test fun exactlyAtTime_doesNotRepeatTheSameMinute() =
        assertEquals("2026-11-20T20:00", next("2026-11-19T20:00", 20 * 60, everyDay))

    @Test fun onlySelectedDays_skipsOthers() =
        // จันทร์/พุธ/เสาร์ จากวันพฤหัส → เสาร์ถัดไปคือ 21 พ.ย.
        assertEquals("2026-11-21T20:00", next("2026-11-19T10:00", 20 * 60, setOf(1, 3, 6)))

    @Test fun sameWeekdayButTimePassed_goesToNextWeek() =
        assertEquals("2026-11-26T20:00", next("2026-11-19T21:00", 20 * 60, setOf(4)))

    @Test fun noDaysSelected_returnsNull() =
        assertNull(next("2026-11-19T10:00", 20 * 60, emptySet()))

    @Test fun crossesYearBoundary() =
        assertEquals("2027-01-01T08:00", next("2026-12-31T23:00", 8 * 60, everyDay))
}
