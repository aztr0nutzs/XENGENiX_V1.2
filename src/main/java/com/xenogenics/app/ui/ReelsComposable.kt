package com.xenogenics.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xenogenics.app.engine.GameState
import com.xenogenics.app.engine.Symbol
import com.xenogenics.app.ui.theme.BioCyan
import com.xenogenics.app.ui.theme.BioMagenta
import com.xenogenics.app.ui.theme.ReelFrame
import com.xenogenics.app.ui.theme.ReelHighlight
import com.xenogenics.app.ui.theme.ToxicGreen

@Composable
fun ReelsComposable(state: GameState) {
    val grid = state.core.lastSpin?.grid ?: defaultGrid()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .shadow(10.dp, RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(listOf(ReelFrame, Color.Black)),
                RoundedCornerShape(18.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (reel in 0 until 5) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (row in 0 until 3) {
                        ReelCell(symbol = grid[reel][row])
                    }
                }
            }
        }

        if (state is GameState.Spinning) {
            Text(
                text = "SPINNING",
                style = MaterialTheme.typography.headlineMedium,
                color = ToxicGreen,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun ReelCell(symbol: Symbol) {
    val glow = when (symbol) {
        Symbol.ORB -> BioMagenta
        Symbol.WILD -> BioCyan
        Symbol.MUTAGEN -> ToxicGreen
        Symbol.NEURAL -> BioCyan
        Symbol.SYNTH -> ReelHighlight
        Symbol.CONTAINMENT -> BioMagenta
        else -> Color(0xFFE3F2FF)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color(0xFF061418), RoundedCornerShape(10.dp))
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(glow.copy(alpha = 0.45f), Color.Transparent)
                ),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        SymbolIcon(symbol = symbol)
    }
}

@Composable
private fun SymbolIcon(symbol: Symbol) {
    val label = when (symbol) {
        Symbol.NINE -> "9"
        Symbol.TEN -> "10"
        Symbol.J -> "J"
        Symbol.Q -> "Q"
        Symbol.K -> "K"
        Symbol.A -> "A"
        Symbol.MUTAGEN -> "M"
        Symbol.NEURAL -> "N"
        Symbol.SYNTH -> "S"
        Symbol.CONTAINMENT -> "C"
        Symbol.WILD -> "W"
        Symbol.ORB -> "ORB"
    }
    val color = when (symbol) {
        Symbol.ORB -> BioMagenta
        Symbol.WILD -> BioCyan
        Symbol.MUTAGEN -> ToxicGreen
        Symbol.NEURAL -> BioCyan
        Symbol.SYNTH -> ReelHighlight
        Symbol.CONTAINMENT -> BioMagenta
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = if (label.length > 1) 16.sp else 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun defaultGrid(): List<List<Symbol>> {
    return listOf(
        listOf(Symbol.A, Symbol.K, Symbol.Q),
        listOf(Symbol.J, Symbol.TEN, Symbol.NINE),
        listOf(Symbol.MUTAGEN, Symbol.NEURAL, Symbol.SYNTH),
        listOf(Symbol.CONTAINMENT, Symbol.A, Symbol.K),
        listOf(Symbol.Q, Symbol.J, Symbol.TEN)
    )
}
