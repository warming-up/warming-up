package com.example.warming_up.ui.preparation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppointmentTimeTextTest {
    @Test
    fun `formats server local date time without offset`() {
        assertEquals("19:30", "2026-07-10T19:30:00".toClockText())
    }

    @Test
    fun `formats server local date time with microseconds`() {
        assertEquals("19:30", "2026-07-10T19:30:00.123456".toClockText())
    }

    @Test
    fun `calculates minutes between server local date times`() {
        assertEquals(
            45,
            minutesBetween(
                start = "2026-07-10T18:45:00",
                end = "2026-07-10T19:30:00",
            ),
        )
    }
}
