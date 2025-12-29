package com.xenogenics.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BonusFirelinkTest {
    @Test
    fun respinResetsWhenNewOrbLands() {
        val emptyGrid = List(5) { MutableList<BonusOrb?>(3) { null } }
        val state = BonusState(
            grid = emptyGrid,
            respinsLeft = 1,
            total = 0,
            jackpotsWon = emptyList()
        )
        val rng = SequenceRng(
            ints = MutableList(100) { 0 },
            doubles = MutableList(100) { 0.0 }
        )

        val next = BonusFirelink.respin(state, rng)

        val filled = next.grid.sumOf { column -> column.count { it != null } }
        assertTrue(filled > 0)
        assertEquals(3, next.respinsLeft)
    }
}
