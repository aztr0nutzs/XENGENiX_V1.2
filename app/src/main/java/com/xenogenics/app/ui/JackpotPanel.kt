package com.xenogenics.app.ui

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xenogenics.app.engine.JackpotMeters
import com.xenogenics.app.ui.theme.BioCyan
import com.xenogenics.app.ui.theme.BioMagenta
import com.xenogenics.app.ui.theme.JackpotGold
import com.xenogenics.app.ui.theme.PanelGlow

@Composable
fun JackpotPanel(meters: JackpotMeters) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0C1F22), Color(0xFF081114))),
                RoundedCornerShape(14.dp)
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        JackpotItem(label = "GRAND", value = meters.grand, color = BioMagenta)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            JackpotItem(label = "MINOR", value = meters.minor, color = PanelGlow, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.padding(horizontal = 6.dp))
            JackpotItem(label = "MAJOR", value = meters.major, color = BioCyan, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.padding(horizontal = 6.dp))
            JackpotItem(label = "MINI", value = meters.mini, color = JackpotGold, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun JackpotItem(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    val animated by animateIntAsState(targetValue = value, label = "jackpot")
    Box(
        modifier = modifier
            .background(Color(0xFF0F262B), RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
            Text(
                text = animated.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
