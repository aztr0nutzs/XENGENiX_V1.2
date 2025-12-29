package com.xenogenics.app.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateRulesTest {
    private fun core(): CoreState {
        return CoreState(
            credits = 10000,
            denomIndex = 0,
            betPerLine = 1,
            lines = PaylineDefinitions.lines.size,
            jackpotMeters = Jackpots.baseMeters,
            settings = GameSettings(),
            lastSpin = null,
            lastWin = 0,
            autoSpin = AutoSpinState(0, true),
            sessionStats = SessionStats(0, 0),
            lastSpins = emptyList(),
            forceBonusNext = false,
            rngSeed = null
        )
    }

    @Test
    fun cannotSpinWhileSpinning() {
        val core = core()
        assertTrue(GameStateRules.canSpin(GameState.BaseIdle(core)))
        assertTrue(GameStateRules.canSpin(GameState.Attract(core)))
        assertFalse(GameStateRules.canSpin(GameState.Spinning(core)))
        assertFalse(GameStateRules.canSpin(GameState.EnteringBonus(core, spinResult())))
        assertFalse(GameStateRules.canSpin(GameState.BonusRespin(core, bonusState())))
    }

    private fun spinResult(): SpinResult {
        return SpinResult(
            grid = listOf(
                listOf(Symbol.A, Symbol.K, Symbol.Q),
                listOf(Symbol.J, Symbol.TEN, Symbol.NINE),
                listOf(Symbol.MUTAGEN, Symbol.NEURAL, Symbol.SYNTH),
                listOf(Symbol.CONTAINMENT, Symbol.A, Symbol.K),
                listOf(Symbol.Q, Symbol.J, Symbol.TEN)
            ),
            lineWins = emptyList(),
            scatterWin = 0,
            totalWin = 0,
            orbCount = 0
        )
    }

    private fun bonusState(): BonusState {
        return BonusState(
            grid = List(5) { MutableList<BonusOrb?>(3) { null } },
            respinsLeft = 3,
            total = 0,
            jackpotsWon = emptyList()
        )
    }
}
