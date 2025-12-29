package com.xenogenics.app.engine

data class GameSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val fastSpin: Boolean = false,
    val reducedMotion: Boolean = false,
    val devToolsEnabled: Boolean = false
)

data class CoreState(
    val credits: Int,
    val denomIndex: Int,
    val betPerLine: Int,
    val lines: Int,
    val jackpotMeters: JackpotMeters,
    val settings: GameSettings,
    val lastSpin: SpinResult?,
    val lastWin: Int,
    val autoSpin: AutoSpinState,
    val sessionStats: SessionStats,
    val lastSpins: List<SpinSummary>,
    val forceBonusNext: Boolean,
    val rngSeed: Long?
)

sealed class GameState(open val core: CoreState) {
    data class Attract(override val core: CoreState) : GameState(core)
    data class BaseIdle(override val core: CoreState) : GameState(core)
    data class Spinning(override val core: CoreState) : GameState(core)
    data class ShowingWins(override val core: CoreState, val spinResult: SpinResult) : GameState(core)
    data class EnteringBonus(override val core: CoreState, val triggerSpin: SpinResult) : GameState(core)
    data class BonusRespin(override val core: CoreState, val bonusState: BonusState) : GameState(core)
    data class BonusResult(override val core: CoreState, val bonusState: BonusState, val totalWin: Int) : GameState(core)
}
