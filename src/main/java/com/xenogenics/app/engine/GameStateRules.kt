package com.xenogenics.app.engine

object GameStateRules {
    fun canSpin(state: GameState): Boolean {
        return when (state) {
            is GameState.BaseIdle -> true
            is GameState.Attract -> true
            else -> false
        }
    }
}
