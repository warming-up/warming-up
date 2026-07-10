package com.example.warming_up.ui.routine

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class RoutineCreateViewModelTest {
    @Test
    fun `builds arrival date time with today's date`() {
        val today = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.JULY)
            set(Calendar.DAY_OF_MONTH, 10)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 15)
            set(Calendar.SECOND, 30)
            set(Calendar.MILLISECOND, 123)
        }

        assertEquals(
            "2026-07-10T19:30:00",
            buildTodayArrivalDateTime("19:30", today),
        )
    }

    @Test
    fun `returns blank for invalid arrival clock time`() {
        assertEquals("", buildTodayArrivalDateTime("25:00"))
        assertEquals("", buildTodayArrivalDateTime("19:30abc"))
    }
}
