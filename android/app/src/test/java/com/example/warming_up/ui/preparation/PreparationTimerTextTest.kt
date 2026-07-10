package com.example.warming_up.ui.preparation

import org.junit.Assert.assertEquals
import org.junit.Test

class PreparationTimerTextTest {
    @Test
    fun `formats remaining seconds as minute and two digit seconds`() {
        assertEquals("5:00", 300.toTimerText())
        assertEquals("0:09", 9.toTimerText())
        assertEquals("1:05", 65.toTimerText())
    }

    @Test
    fun `does not show negative remaining time`() {
        assertEquals("0:00", (-1).toTimerText())
    }
}
