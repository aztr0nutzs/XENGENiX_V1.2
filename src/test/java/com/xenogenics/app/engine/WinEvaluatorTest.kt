package com.xenogenics.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WinEvaluatorTest {
    @Test
    fun evaluatesFiveOfAKindOnMiddleLine() {
        val grid = listOf(
            listOf(Symbol.NINE, Symbol.A, Symbol.TEN),
            listOf(Symbol.J, Symbol.A, Symbol.Q),
            listOf(Symbol.K, Symbol.A, Symbol.NINE),
            listOf(Symbol.Q, Symbol.A, Symbol.TEN),
            listOf(Symbol.J, Symbol.A, Symbol.NINE)
        )

        val result = WinEvaluator.evaluate(
            grid = grid,
            paylines = PaylineDefinitions.lines,
            betPerLine = 1,
            denom = 1
        )

        assertTrue(result.lineWins.isNotEmpty())
        val middleWin = result.lineWins.first { it.lineIndex == 0 }
        assertEquals(Symbol.A, middleWin.symbol)
        assertEquals(5, middleWin.count)
        assertEquals(36, middleWin.payout)
        assertEquals(36, result.totalWin)
    }
}
