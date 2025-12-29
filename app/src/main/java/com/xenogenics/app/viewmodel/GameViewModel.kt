package com.xenogenics.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xenogenics.app.data.GameDataStore
import com.xenogenics.app.engine.AutoSpinState
import com.xenogenics.app.engine.BonusFirelink
import com.xenogenics.app.engine.BonusState
import com.xenogenics.app.engine.CoreState
import com.xenogenics.app.engine.GameSettings
import com.xenogenics.app.engine.GameState
import com.xenogenics.app.engine.GameStateRules
import com.xenogenics.app.engine.JackpotMeters
import com.xenogenics.app.engine.JackpotType
import com.xenogenics.app.engine.Jackpots
import com.xenogenics.app.engine.PaylineDefinitions
import com.xenogenics.app.engine.SeededRng
import com.xenogenics.app.engine.SessionStats
import com.xenogenics.app.engine.SpinGenerator
import com.xenogenics.app.engine.SpinResult
import com.xenogenics.app.engine.SpinSummary
import com.xenogenics.app.engine.Symbol
import com.xenogenics.app.engine.WinEvaluator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = GameDataStore(application)
    private val rng = SeededRng()
    private val spinGenerator = SpinGenerator(rng)

    private val denoms = listOf(1, 2, 5, 10)
    private val betPerLineOptions = (1..5).toList()

    private val _state: MutableStateFlow<GameState> = MutableStateFlow(
        GameState.Attract(
            core = CoreState(
                credits = 10000,
                denomIndex = 0,
                betPerLine = 1,
                lines = PaylineDefinitions.lines.size,
                jackpotMeters = Jackpots.baseMeters,
                settings = GameSettings(),
                lastSpin = null,
                lastWin = 0,
                autoSpin = AutoSpinState(remaining = 0, stopOnFeature = true),
                sessionStats = SessionStats(0, 0),
                lastSpins = emptyList(),
                forceBonusNext = false,
                rngSeed = null
            )
        )
    )
    val state: StateFlow<GameState> = _state

    init {
        viewModelScope.launch {
            dataStore.settingsFlow.collect { settings ->
                updateCore { it.copy(settings = settings) }
            }
        }
        viewModelScope.launch {
            dataStore.metersFlow.collect { meters ->
                updateCore { it.copy(jackpotMeters = meters) }
            }
        }
        viewModelScope.launch {
            dataStore.sessionWageredFlow.collect { wagered ->
                updateCore { core -> core.copy(sessionStats = core.sessionStats.copy(totalWagered = wagered)) }
            }
        }
        viewModelScope.launch {
            dataStore.sessionReturnedFlow.collect { returned ->
                updateCore { core -> core.copy(sessionStats = core.sessionStats.copy(totalReturned = returned)) }
            }
        }

        updateState { state ->
            GameState.BaseIdle(state.core)
        }
    }

    fun onSpin() {
        val current = _state.value
        if (!GameStateRules.canSpin(current)) return
        val core = current.core
        val totalBet = totalBet(core)
        if (core.credits < totalBet) {
            stopAutoSpin()
            return
        }

        val autoSpin = if (core.autoSpin.remaining > 0) {
            core.autoSpin.copy(remaining = core.autoSpin.remaining - 1)
        } else {
            core.autoSpin
        }

        val nextCore = core.copy(
            credits = core.credits - totalBet,
            lastWin = 0,
            autoSpin = autoSpin,
            sessionStats = core.sessionStats.copy(totalWagered = core.sessionStats.totalWagered + totalBet)
        )

        updateState { GameState.Spinning(nextCore) }
        viewModelScope.launch {
            dataStore.setLastSpinOutcome(true)
            dataStore.updateSessionStats(nextCore.sessionStats.totalWagered, nextCore.sessionStats.totalReturned)
        }

        viewModelScope.launch {
            delay(spinDelayMillis(core.settings.fastSpin))
            finishSpin()
        }
    }

    private fun finishSpin() {
        val current = _state.value
        if (current !is GameState.Spinning) return
        val core = current.core

        var grid = spinGenerator.spin()
        if (core.forceBonusNext) {
            grid = injectOrbs(grid, 6)
        }

        val winResult = WinEvaluator.evaluate(
            grid = grid,
            paylines = PaylineDefinitions.lines,
            betPerLine = core.betPerLine,
            denom = denoms[core.denomIndex]
        )

        val totalWin = winResult.totalWin
        val updatedMeters = Jackpots.drift(core.jackpotMeters, rng)

        val shouldStopAutoOnFeature = core.autoSpin.stopOnFeature && (winResult.orbCount >= 6 || core.forceBonusNext)
        val updatedCore = core.copy(
            credits = core.credits + totalWin,
            lastSpin = SpinResult(
                grid = grid,
                lineWins = winResult.lineWins,
                scatterWin = winResult.scatterWin,
                totalWin = totalWin,
                orbCount = winResult.orbCount
            ),
            lastWin = totalWin,
            jackpotMeters = updatedMeters,
            forceBonusNext = false,
            lastSpins = updateLastSpins(core.lastSpins, grid, totalWin, winResult.orbCount),
            sessionStats = core.sessionStats.copy(totalReturned = core.sessionStats.totalReturned + totalWin),
            autoSpin = if (shouldStopAutoOnFeature) core.autoSpin.copy(remaining = 0) else core.autoSpin
        )

        viewModelScope.launch {
            dataStore.updateMeters(updatedMeters)
            dataStore.updateSessionStats(updatedCore.sessionStats.totalWagered, updatedCore.sessionStats.totalReturned)
        }

        val triggerBonus = winResult.orbCount >= 6 || core.forceBonusNext
        if (triggerBonus) {
            updateState { GameState.EnteringBonus(updatedCore, updatedCore.lastSpin!!) }
            viewModelScope.launch {
                delay(600)
                startBonus(updatedCore, grid)
            }
        } else {
            updateState { GameState.ShowingWins(updatedCore, updatedCore.lastSpin!!) }
            viewModelScope.launch {
                delay(winDelayMillis(core.settings.fastSpin))
                endWinPhase()
            }
        }
    }

    private fun startBonus(core: CoreState, grid: List<List<Symbol>>) {
        val initial = BonusFirelink.initialStateFromBase(grid, rng)
        updateState { GameState.BonusRespin(core, initial) }
        viewModelScope.launch {
            runBonusLoop(initial)
        }
    }

    private suspend fun runBonusLoop(initial: BonusState) {
        var state = initial
        while (state.respinsLeft > 0) {
            delay(bonusDelayMillis(_state.value.core.settings.fastSpin))
            state = BonusFirelink.respin(state, rng)
            updateState { GameState.BonusRespin(it.core, state) }
        }

        val core = _state.value.core
        val jackpotWin = state.jackpotsWon.sumOf { Jackpots.award(it, core.jackpotMeters) }
        val totalWin = state.total + jackpotWin
        val updatedCore = core.copy(
            credits = core.credits + totalWin,
            lastWin = totalWin,
            sessionStats = core.sessionStats.copy(totalReturned = core.sessionStats.totalReturned + totalWin),
            autoSpin = core.autoSpin.copy(remaining = 0)
        )

        updateState { GameState.BonusResult(updatedCore, state, totalWin) }
        viewModelScope.launch {
            dataStore.updateSessionStats(updatedCore.sessionStats.totalWagered, updatedCore.sessionStats.totalReturned)
            dataStore.setLastSpinOutcome(false)
        }

        delay(winDelayMillis(core.settings.fastSpin))
        updateState { GameState.BaseIdle(updatedCore) }
    }

    private fun endWinPhase() {
        val current = _state.value
        if (current !is GameState.ShowingWins) return
        updateState { GameState.BaseIdle(current.core) }
        viewModelScope.launch {
            dataStore.setLastSpinOutcome(false)
        }
        maybeAutoSpin()
    }

    private fun maybeAutoSpin() {
        val core = _state.value.core
        if (core.autoSpin.remaining <= 0) return
        if (core.credits < totalBet(core)) {
            stopAutoSpin()
            return
        }
        onSpin()
    }

    fun toggleSound() {
        val core = _state.value.core
        val next = core.settings.copy(soundEnabled = !core.settings.soundEnabled)
        updateSettings(next)
    }

    fun toggleHaptics() {
        val core = _state.value.core
        val next = core.settings.copy(hapticsEnabled = !core.settings.hapticsEnabled)
        updateSettings(next)
    }

    fun toggleFastSpin() {
        val core = _state.value.core
        val next = core.settings.copy(fastSpin = !core.settings.fastSpin)
        updateSettings(next)
    }

    fun toggleReducedMotion() {
        val core = _state.value.core
        val next = core.settings.copy(reducedMotion = !core.settings.reducedMotion)
        updateSettings(next)
    }

    fun toggleDevTools() {
        val core = _state.value.core
        val next = core.settings.copy(devToolsEnabled = !core.settings.devToolsEnabled)
        updateSettings(next)
    }

    fun increaseBet() {
        val core = _state.value.core
        val currentIndex = betPerLineOptions.indexOf(core.betPerLine).coerceAtLeast(0)
        val nextIndex = (currentIndex + 1).coerceAtMost(betPerLineOptions.lastIndex)
        updateCore { it.copy(betPerLine = betPerLineOptions[nextIndex]) }
    }

    fun decreaseBet() {
        val core = _state.value.core
        val currentIndex = betPerLineOptions.indexOf(core.betPerLine).coerceAtLeast(0)
        val nextIndex = (currentIndex - 1).coerceAtLeast(0)
        updateCore { it.copy(betPerLine = betPerLineOptions[nextIndex]) }
    }

    fun cycleDenom() {
        val core = _state.value.core
        val nextIndex = (core.denomIndex + 1) % denoms.size
        updateCore { it.copy(denomIndex = nextIndex) }
    }

    fun maxBet() {
        updateCore {
            it.copy(
                denomIndex = denoms.lastIndex,
                betPerLine = betPerLineOptions.last()
            )
        }
    }

    fun startAutoSpin(count: Int, stopOnFeature: Boolean) {
        updateCore { it.copy(autoSpin = AutoSpinState(remaining = count, stopOnFeature = stopOnFeature)) }
        onSpin()
    }

    fun stopAutoSpin() {
        updateCore { it.copy(autoSpin = it.autoSpin.copy(remaining = 0)) }
    }

    fun setCredits(amount: Int) {
        updateCore { it.copy(credits = amount.coerceAtLeast(0)) }
    }

    fun setForceBonusNext(force: Boolean) {
        updateCore { it.copy(forceBonusNext = force) }
    }

    fun setRngSeed(seed: Long?) {
        if (seed != null) {
            rng.reseed(seed)
        } else {
            rng.reseed(System.currentTimeMillis())
        }
        updateCore { it.copy(rngSeed = seed) }
    }

    private fun updateSettings(settings: GameSettings) {
        updateCore { it.copy(settings = settings) }
        viewModelScope.launch {
            dataStore.updateSettings(settings)
        }
    }

    private fun updateCore(block: (CoreState) -> CoreState) {
        updateState { state ->
            val core = block(state.core)
            when (state) {
                is GameState.Attract -> GameState.Attract(core)
                is GameState.BaseIdle -> GameState.BaseIdle(core)
                is GameState.Spinning -> GameState.Spinning(core)
                is GameState.ShowingWins -> GameState.ShowingWins(core, state.spinResult)
                is GameState.EnteringBonus -> GameState.EnteringBonus(core, state.triggerSpin)
                is GameState.BonusRespin -> GameState.BonusRespin(core, state.bonusState)
                is GameState.BonusResult -> GameState.BonusResult(core, state.bonusState, state.totalWin)
            }
        }
    }

    private fun updateState(block: (GameState) -> GameState) {
        _state.value = block(_state.value)
    }

    private fun totalBet(core: CoreState): Int {
        return denoms[core.denomIndex] * core.betPerLine * core.lines
    }

    private fun updateLastSpins(
        current: List<SpinSummary>,
        grid: List<List<Symbol>>,
        totalWin: Int,
        orbCount: Int
    ): List<SpinSummary> {
        val updated = listOf(SpinSummary(grid, totalWin, orbCount)) + current
        return updated.take(20)
    }

    private fun injectOrbs(grid: List<List<Symbol>>, required: Int): List<List<Symbol>> {
        val result = grid.map { it.toMutableList() }.toMutableList()
        val positions = mutableListOf<Pair<Int, Int>>()
        for (reel in result.indices) {
            for (row in result[reel].indices) {
                if (result[reel][row] != Symbol.ORB) {
                    positions.add(reel to row)
                }
            }
        }
        var count = result.sumOf { column -> column.count { it == Symbol.ORB } }
        while (count < required && positions.isNotEmpty()) {
            val index = rng.nextInt(positions.size)
            val (reel, row) = positions.removeAt(index)
            result[reel][row] = Symbol.ORB
            count++
        }
        return result
    }

    private fun spinDelayMillis(fastSpin: Boolean): Long = if (fastSpin) 250L else 900L

    private fun winDelayMillis(fastSpin: Boolean): Long = if (fastSpin) 450L else 1200L

    private fun bonusDelayMillis(fastSpin: Boolean): Long = if (fastSpin) 350L else 900L
}
