package com.xenogenics.app.engine

class SpinGenerator(
    private val rng: Rng,
    private val strips: List<List<Symbol>> = ReelStrips.reels
) {
    fun spin(): List<List<Symbol>> {
        val grid = mutableListOf<List<Symbol>>()
        for (reel in strips) {
            val stop = rng.nextInt(reel.size)
            val column = listOf(
                reel[(stop) % reel.size],
                reel[(stop + 1) % reel.size],
                reel[(stop + 2) % reel.size]
            )
            grid.add(column)
        }
        return grid
    }
}
