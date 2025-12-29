package com.xenogenics.app.engine

import kotlin.math.max

object BonusFirelink {
    data class BonusConfig(
        val gridWidth: Int = 5,
        val gridHeight: Int = 3,
        val respins: Int = 3,
        val orbWeights: List<Pair<Int, Int>> = listOf(
            10 to 30,
            15 to 20,
            20 to 15,
            25 to 10,
            30 to 8,
            40 to 6,
            100 to 3,
            125 to 2,
            150 to 2,
            1000 to 1
        ),
        val jackpotChance: Double = 0.02,
        val jackpotWeights: Map<JackpotType, Int> = mapOf(
            JackpotType.MINI to 70,
            JackpotType.MINOR to 20,
            JackpotType.MAJOR to 9,
            JackpotType.GRAND to 1
        )
    )

    fun initialStateFromBase(
        baseGrid: List<List<Symbol>>,
        rng: Rng,
        config: BonusConfig = BonusConfig()
    ): BonusState {
        val grid = MutableList(config.gridWidth) { MutableList<BonusOrb?>(config.gridHeight) { null } }
        val jackpots = mutableListOf<JackpotType>()
        for (reel in 0 until config.gridWidth) {
            for (row in 0 until config.gridHeight) {
                if (baseGrid[reel][row] == Symbol.ORB) {
                    val orb = rollOrb(rng, config)
                    if (orb.jackpot != null) jackpots.add(orb.jackpot)
                    grid[reel][row] = orb
                }
            }
        }
        val total = grid.sumOf { column -> column.filterNotNull().sumOf { it.value } }
        return BonusState(
            grid = grid,
            respinsLeft = config.respins,
            total = total,
            jackpotsWon = jackpots
        )
    }

    fun respin(
        state: BonusState,
        rng: Rng,
        config: BonusConfig = BonusConfig()
    ): BonusState {
        val grid = state.grid.map { it.toMutableList() }.toMutableList()
        val filled = grid.sumOf { column -> column.count { it != null } }
        val spawnChance = spawnChanceForFilled(filled, config.gridWidth * config.gridHeight)
        var anyNew = false
        val jackpotsWon = state.jackpotsWon.toMutableList()

        for (reel in 0 until config.gridWidth) {
            for (row in 0 until config.gridHeight) {
                if (grid[reel][row] == null) {
                    if (rng.nextDouble() < spawnChance) {
                        val orb = rollOrb(rng, config)
                        if (orb.jackpot != null) jackpotsWon.add(orb.jackpot)
                        grid[reel][row] = orb
                        anyNew = true
                    }
                }
            }
        }

        val nextRespins = if (anyNew) config.respins else max(0, state.respinsLeft - 1)
        val total = grid.sumOf { column -> column.filterNotNull().sumOf { it.value } }

        return BonusState(
            grid = grid,
            respinsLeft = nextRespins,
            total = total,
            jackpotsWon = jackpotsWon
        )
    }

    private fun spawnChanceForFilled(filled: Int, total: Int): Double {
        val base = 0.28
        val decay = 0.012
        val chance = base - (filled * decay)
        val minChance = 0.06
        val maxChance = 0.28
        return chance.coerceIn(minChance, maxChance)
    }

    private fun rollOrb(rng: Rng, config: BonusConfig): BonusOrb {
        val jackpotRoll = rng.nextDouble()
        return if (jackpotRoll < config.jackpotChance) {
            val jackpot = rollWeighted(config.jackpotWeights.entries.map { it.key to it.value }, rng)
            BonusOrb(value = 0, jackpot = jackpot)
        } else {
            val value = rollWeighted(config.orbWeights, rng)
            BonusOrb(value = value, jackpot = null)
        }
    }

    private fun <T> rollWeighted(options: List<Pair<T, Int>>, rng: Rng): T {
        val total = options.sumOf { it.second }
        if (total <= 0) return options.first().first
        var roll = rng.nextInt(total)
        for (option in options) {
            roll -= option.second
            if (roll < 0) return option.first
        }
        return options.last().first
    }
}
