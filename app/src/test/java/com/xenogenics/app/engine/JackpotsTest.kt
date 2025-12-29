package com.xenogenics.app.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class JackpotsTest {
    @Test
    fun jackpotAwardUsesMeterValue() {
        val meters = JackpotMeters(mini = 111, minor = 222, major = 333, grand = 444)
        assertEquals(111, Jackpots.award(JackpotType.MINI, meters))
        assertEquals(222, Jackpots.award(JackpotType.MINOR, meters))
        assertEquals(333, Jackpots.award(JackpotType.MAJOR, meters))
        assertEquals(444, Jackpots.award(JackpotType.GRAND, meters))
    }
}
