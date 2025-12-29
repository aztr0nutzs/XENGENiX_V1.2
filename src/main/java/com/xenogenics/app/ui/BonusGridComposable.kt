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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xenogenics.app.engine.BonusOrb
import com.xenogenics.app.engine.GameState
import com.xenogenics.app.engine.JackpotType
import com.xenogenics.app.ui.theme.BioCyan
import com.xenogenics.app.ui.theme.BioMagenta
import com.xenogenics.app.ui.theme.OozeDark
import com.xenogenics.app.ui.theme.PanelGlow
import com.xenogenics.app.ui.theme.ToxicGreen

@Composable
fun BonusGridComposable(state: GameState) {
    val bonusState = when (state) {
        is GameState.BonusRespin -> state.bonusState
        is GameState.BonusResult -> state.bonusState
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(
                Brush.verticalGradient(listOf(OozeDark, Color.Black)),
                RoundedCornerShape(18.dp)
            )
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RESPINS ${bonusState?.respinsLeft ?: 0}",
                    style = MaterialTheme.typography.titleMedium,
                    color = ToxicGreen
                )
                Text(
                    text = "TOTAL ${bonusState?.total ?: 0}",
                    style = MaterialTheme.typography.titleMedium,
                    color = BioCyan
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (reel in 0 until 5) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (row in 0 until 3) {
                            BonusCell(orb = bonusState?.grid?.get(reel)?.get(row))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BonusCell(orb: BonusOrb?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color(0xFF061012), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (orb != null) {
            val color = when (orb.jackpot) {
                JackpotType.GRAND -> BioMagenta
                JackpotType.MAJOR -> BioCyan
                JackpotType.MINOR -> PanelGlow
                JackpotType.MINI -> ToxicGreen
                null -> BioMagenta
            }
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(color.copy(alpha = 0.25f), RoundedCornerShape(50))
            )
            Text(
                text = orb.jackpot?.name ?: orb.value.toString(),
                color = Color.White,
                fontSize = if (orb.jackpot != null) 12.sp else 18.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = "",
                color = Color.Transparent
            )
        }
    }
}
