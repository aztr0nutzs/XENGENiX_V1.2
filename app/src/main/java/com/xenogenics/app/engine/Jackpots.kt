package com.xenogenics.app.engine

object Jackpots {
    val baseMeters = JackpotMeters(
        mini = 1000,
        minor = 5000,
        major = 20000,
        grand = 100000
    )

    fun drift(current: JackpotMeters, rng: Rng): JackpotMeters {
        val mini = driftValue(current.mini, baseMeters.mini, 2, 2, rng)
        val minor = driftValue(current.minor, baseMeters.minor, 5, 3, rng)
        val major = driftValue(current.major, baseMeters.major, 10, 2, rng)
        val grand = driftValue(current.grand, baseMeters.grand, 2, 1, rng)
        return JackpotMeters(mini = mini, minor = minor, major = major, grand = grand)
    }

    fun award(type: JackpotType, meters: JackpotMeters): Int {
        return when (type) {
            JackpotType.MINI -> meters.mini
            JackpotType.MINOR -> meters.minor
            JackpotType.MAJOR -> meters.major
            JackpotType.GRAND -> meters.grand
        }
    }

    private fun driftValue(current: Int, base: Int, maxStep: Int, maxMultiplier: Int, rng: Rng): Int {
        val cap = base * maxMultiplier
        val step = rng.nextInt(maxStep + 1)
        val next = current + step
        return next.coerceAtMost(cap)
    }
}
