package com.xenogenics.app.engine

import kotlin.random.Random

interface Rng {
    fun nextInt(bound: Int): Int
    fun nextDouble(): Double
    fun reseed(seed: Long)
}

class SeededRng(seed: Long? = null) : Rng {
    private var random: Random = seed?.let { Random(it) } ?: Random.Default

    override fun nextInt(bound: Int): Int = random.nextInt(bound)

    override fun nextDouble(): Double = random.nextDouble()

    override fun reseed(seed: Long) {
        random = Random(seed)
    }
}

class SequenceRng(
    private val ints: MutableList<Int> = mutableListOf(),
    private val doubles: MutableList<Double> = mutableListOf()
) : Rng {
    override fun nextInt(bound: Int): Int {
        return if (ints.isNotEmpty()) {
            val v = ints.removeAt(0)
            if (bound == 0) 0 else (kotlin.math.abs(v) % bound)
        } else {
            0
        }
    }

    override fun nextDouble(): Double {
        return if (doubles.isNotEmpty()) {
            val v = doubles.removeAt(0)
            v.coerceIn(0.0, 1.0)
        } else {
            0.0
        }
    }

    override fun reseed(seed: Long) {
        // No-op for deterministic sequence in tests.
    }
}
