package com.xenogenics.app.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class RngTest {
    @Test
    fun seededRngIsDeterministic() {
        val rngA = SeededRng(12345L)
        val rngB = SeededRng(12345L)

        repeat(10) {
            assertEquals(rngA.nextInt(1000), rngB.nextInt(1000))
        }
    }
}
