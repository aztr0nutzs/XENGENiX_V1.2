package com.xenogenics.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xenogenics.app.engine.GameState
import com.xenogenics.app.engine.GameStateRules
import com.xenogenics.app.ui.theme.BioCyan
import com.xenogenics.app.ui.theme.BioMagenta
import com.xenogenics.app.ui.theme.OozeDark
import com.xenogenics.app.ui.theme.ToxicGreen

@Composable
fun ControlDeck(
    state: GameState,
    onSpin: () -> Unit,
    onAuto: () -> Unit,
    onMaxBet: () -> Unit,
    onBetPlus: () -> Unit,
    onBetMinus: () -> Unit,
    onDenom: () -> Unit,
    onInfo: () -> Unit,
    onSound: () -> Unit,
    onSettings: () -> Unit
) {
    val core = state.core
    val canSpin = GameStateRules.canSpin(state)
    val denom = when (core.denomIndex) {
        0 -> 1
        1 -> 2
        2 -> 5
        else -> 10
    }
    val totalBet = core.betPerLine * core.lines * denom

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(OozeDark, Color.Black)),
                RoundedCornerShape(16.dp)
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MeterChip(label = "CREDITS", value = core.credits.toString())
            MeterChip(label = "BET/L", value = core.betPerLine.toString())
            MeterChip(label = "DENOM", value = denom.toString())
            MeterChip(label = "LINES", value = core.lines.toString())
            MeterChip(label = "BET", value = totalBet.toString())
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeonButton(text = "BET -", color = BioMagenta, onClick = onBetMinus)
            NeonButton(text = "BET +", color = BioMagenta, onClick = onBetPlus)
            NeonButton(text = "DENOM", color = BioCyan, onClick = onDenom)
            NeonButton(text = "MAX BET", color = ToxicGreen, onClick = onMaxBet)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeonButton(text = "INFO", color = BioCyan, onClick = onInfo)
            NeonButton(text = if (core.settings.soundEnabled) "SOUND ON" else "SOUND OFF", color = BioCyan, onClick = onSound)
            NeonButton(text = "SETTINGS", color = BioCyan, onClick = onSettings)
            NeonButton(text = "AUTO", color = BioMagenta, onClick = onAuto)
            NeonButton(text = "SPIN", color = ToxicGreen, onClick = onSpin, large = true, enabled = canSpin)
        }
    }
}

@Composable
private fun MeterChip(label: String, value: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF102024), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8FFFEF))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NeonButton(text: String, color: Color, onClick: () -> Unit, large: Boolean = false, enabled: Boolean = true) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = Modifier
            .sizeIn(minWidth = if (large) 140.dp else 90.dp)
            .background(Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = if (enabled) color else color.copy(alpha = 0.5f),
            contentColor = Color.Black,
            disabledContainerColor = color.copy(alpha = 0.4f),
            disabledContentColor = Color.Black.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}
