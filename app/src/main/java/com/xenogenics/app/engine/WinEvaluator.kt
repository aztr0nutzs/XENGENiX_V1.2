package com.xenogenics.app.engine

object WinEvaluator {
    fun evaluate(
        grid: List<List<Symbol>>,
        paylines: List<IntArray>,
        betPerLine: Int,
        denom: Int
    ): WinResult {
        val lineWins = mutableListOf<LineWin>()
        for ((index, line) in paylines.withIndex()) {
            val symbols = line.mapIndexed { reelIndex, rowIndex ->
                grid[reelIndex][rowIndex]
            }
            val lineWin = evaluateLine(symbols, index, betPerLine, denom)
            if (lineWin != null && lineWin.payout > 0) {
                lineWins.add(lineWin)
            }
        }

        val orbCount = grid.sumOf { column -> column.count { it == Symbol.ORB } }
        val scatterBase = Paytable.scatterPayout(orbCount)
        val scatterWin = scatterBase * denom * betPerLine

        val totalLineWin = lineWins.sumOf { it.payout }
        val totalWin = totalLineWin + scatterWin

        return WinResult(
            lineWins = lineWins,
            scatterWin = scatterWin,
            totalWin = totalWin,
            orbCount = orbCount
        )
    }

    private fun evaluateLine(
        symbols: List<Symbol>,
        lineIndex: Int,
        betPerLine: Int,
        denom: Int
    ): LineWin? {
        if (symbols.isEmpty()) return null
        if (symbols.first() == Symbol.ORB) return null

        val baseSymbol = symbols.firstOrNull { it != Symbol.WILD && it != Symbol.ORB } ?: Symbol.WILD
        var count = 0
        for (symbol in symbols) {
            if (symbol == Symbol.ORB) break
            if (symbol == baseSymbol || symbol == Symbol.WILD) {
                count++
            } else {
                break
            }
        }

        if (count < 3) return null

        val basePayout = Paytable.linePayout(baseSymbol, count)
        if (basePayout == 0) return null

        val payout = basePayout * betPerLine * denom
        return LineWin(lineIndex = lineIndex, symbol = baseSymbol, count = count, payout = payout)
    }
}
