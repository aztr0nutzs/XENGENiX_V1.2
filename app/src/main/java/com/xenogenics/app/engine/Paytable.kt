package com.xenogenics.app.engine

object Paytable {
    private val linePays: Map<Symbol, IntArray> = mapOf(
        Symbol.NINE to intArrayOf(0, 0, 4, 8, 16, 24),
        Symbol.TEN to intArrayOf(0, 0, 4, 8, 16, 24),
        Symbol.J to intArrayOf(0, 0, 5, 10, 20, 30),
        Symbol.Q to intArrayOf(0, 0, 5, 10, 20, 30),
        Symbol.K to intArrayOf(0, 0, 6, 12, 24, 36),
        Symbol.A to intArrayOf(0, 0, 6, 12, 24, 36),
        Symbol.MUTAGEN to intArrayOf(0, 0, 10, 25, 60, 100),
        Symbol.NEURAL to intArrayOf(0, 0, 12, 30, 70, 120),
        Symbol.SYNTH to intArrayOf(0, 0, 15, 40, 90, 150),
        Symbol.CONTAINMENT to intArrayOf(0, 0, 20, 60, 140, 200),
        Symbol.WILD to intArrayOf(0, 0, 15, 50, 120, 200)
    )

    private val scatterPays: Map<Int, Int> = mapOf(
        3 to 10,
        4 to 25,
        5 to 50,
        6 to 100,
        7 to 150,
        8 to 200,
        9 to 250,
        10 to 300,
        11 to 400,
        12 to 500,
        13 to 700,
        14 to 900,
        15 to 1200
    )

    fun linePayout(symbol: Symbol, count: Int): Int {
        val pays = linePays[symbol] ?: return 0
        if (count < 0 || count >= pays.size) return 0
        return pays[count]
    }

    fun scatterPayout(orbCount: Int): Int {
        return scatterPays[orbCount] ?: 0
    }
}
