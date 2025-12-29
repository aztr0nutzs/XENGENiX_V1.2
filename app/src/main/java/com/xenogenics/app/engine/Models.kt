package com.xenogenics.app.engine

enum class Symbol {
    NINE,
    TEN,
    J,
    Q,
    K,
    A,
    MUTAGEN,
    NEURAL,
    SYNTH,
    CONTAINMENT,
    WILD,
    ORB
}

data class LineWin(
    val lineIndex: Int,
    val symbol: Symbol,
    val count: Int,
    val payout: Int
)

data class SpinResult(
    val grid: List<List<Symbol>>,
    val lineWins: List<LineWin>,
    val scatterWin: Int,
    val totalWin: Int,
    val orbCount: Int
)

data class WinResult(
    val lineWins: List<LineWin>,
    val scatterWin: Int,
    val totalWin: Int,
    val orbCount: Int
)

enum class JackpotType {
    MINI,
    MINOR,
    MAJOR,
    GRAND
}

data class JackpotMeters(
    val mini: Int,
    val minor: Int,
    val major: Int,
    val grand: Int
)

data class BonusOrb(
    val value: Int,
    val jackpot: JackpotType? = null
)

data class BonusState(
    val grid: List<List<BonusOrb?>>,
    val respinsLeft: Int,
    val total: Int,
    val jackpotsWon: List<JackpotType>
)

data class BonusOutcome(
    val finalState: BonusState,
    val totalWin: Int
)

data class SpinSummary(
    val grid: List<List<Symbol>>,
    val totalWin: Int,
    val orbCount: Int
)

data class SessionStats(
    val totalWagered: Long,
    val totalReturned: Long
) {
    val rtp: Double
        get() = if (totalWagered == 0L) 0.0 else (totalReturned.toDouble() / totalWagered.toDouble())
}

data class AutoSpinState(
    val remaining: Int,
    val stopOnFeature: Boolean
)
