package com.xenogenics.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xenogenics.app.engine.GameState

@Composable
fun Overlays(
    overlay: Overlay,
    state: GameState,
    onDismiss: () -> Unit,
    onStartAuto: (Int, Boolean) -> Unit,
    onStopAuto: () -> Unit,
    onToggleFast: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleReducedMotion: () -> Unit,
    onToggleDevTools: () -> Unit,
    onSetCredits: (Int) -> Unit,
    onForceBonus: (Boolean) -> Unit,
    onSetSeed: (Long?) -> Unit
) {
    when (overlay) {
        Overlay.Info -> InfoOverlay(onDismiss)
        Overlay.Settings -> SettingsOverlay(
            state = state,
            onDismiss = onDismiss,
            onToggleFast = onToggleFast,
            onToggleHaptics = onToggleHaptics,
            onToggleReducedMotion = onToggleReducedMotion,
            onToggleDevTools = onToggleDevTools,
            onSetCredits = onSetCredits,
            onForceBonus = onForceBonus,
            onSetSeed = onSetSeed
        )
        Overlay.AutoSpin -> AutoSpinOverlay(
            onDismiss = onDismiss,
            onStart = onStartAuto,
            onStop = onStopAuto
        )
        Overlay.None -> Unit
    }
}

enum class Overlay {
    None,
    Info,
    Settings,
    AutoSpin
}

@Composable
private fun InfoOverlay(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text("CLOSE") } },
        title = { Text("PAYTABLE + HELP") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("50 fixed paylines. 3+ matching symbols left-to-right pay. Wild substitutes for all line symbols.")
                Text("BIO-CORE ORBS: 6+ anywhere triggers BIO-LINK bonus.")
                Text("Bonus: Hold-and-Spin 5x3. New orbs reset respins to 3. Values add up, jackpots award meters.")
            }
        }
    )
}

@Composable
private fun AutoSpinOverlay(
    onDismiss: () -> Unit,
    onStart: (Int, Boolean) -> Unit,
    onStop: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onStart(10, true) }) { Text("AUTO 10") }
                Button(onClick = { onStart(25, true) }) { Text("AUTO 25") }
                Button(onClick = { onStart(50, true) }) { Text("AUTO 50") }
                Button(onClick = { onStart(100, true) }) { Text("AUTO 100") }
            }
        },
        dismissButton = {
            Button(onClick = {
                onStop()
                onDismiss()
            }) { Text("STOP") }
        },
        title = { Text("AUTO SPIN") },
        text = {
            Text("Stops on insufficient credits or feature entry.")
        }
    )
}

@Composable
private fun SettingsOverlay(
    state: GameState,
    onDismiss: () -> Unit,
    onToggleFast: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleReducedMotion: () -> Unit,
    onToggleDevTools: () -> Unit,
    onSetCredits: (Int) -> Unit,
    onForceBonus: (Boolean) -> Unit,
    onSetSeed: (Long?) -> Unit
) {
    var creditsText by remember { mutableStateOf(state.core.credits.toString()) }
    var seedText by remember { mutableStateOf(state.core.rngSeed?.toString() ?: "") }
    var forceBonus by remember { mutableStateOf(state.core.forceBonusNext) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onDismiss()
            }) { Text("CLOSE") }
        },
        title = { Text("SETTINGS") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsRow(
                    label = "Fast Spin",
                    value = if (state.core.settings.fastSpin) "ON" else "OFF",
                    onToggle = onToggleFast
                )
                SettingsRow(
                    label = "Haptics",
                    value = if (state.core.settings.hapticsEnabled) "ON" else "OFF",
                    onToggle = onToggleHaptics
                )
                SettingsRow(
                    label = "Reduced Motion",
                    value = if (state.core.settings.reducedMotion) "ON" else "OFF",
                    onToggle = onToggleReducedMotion
                )
                SettingsRow(
                    label = "Dev Tools",
                    value = if (state.core.settings.devToolsEnabled) "ON" else "OFF",
                    onToggle = onToggleDevTools
                )

                if (state.core.settings.devToolsEnabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF121B1C), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("DEV PANEL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = creditsText,
                            onValueChange = { creditsText = it },
                            label = { Text("Set Credits") }
                        )
                        Button(onClick = {
                            val value = creditsText.toIntOrNull() ?: state.core.credits
                            onSetCredits(value)
                        }) { Text("APPLY CREDITS") }

                        OutlinedTextField(
                            value = seedText,
                            onValueChange = { seedText = it },
                            label = { Text("RNG Seed (blank = random)") }
                        )
                        Button(onClick = {
                            val seed = seedText.toLongOrNull()
                            onSetSeed(seed)
                        }) { Text("SET SEED") }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                forceBonus = !forceBonus
                                onForceBonus(forceBonus)
                            }) { Text(if (forceBonus) "BONUS FORCED" else "FORCE BONUS") }
                        }

                        Text(
                            text = "Session RTP ${(state.core.sessionStats.rtp * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Wagered ${state.core.sessionStats.totalWagered} | Returned ${state.core.sessionStats.totalReturned}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Last 20 spins",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        state.core.lastSpins.forEachIndexed { index, spin ->
                            Text("#${index + 1} Win ${spin.totalWin} Orbs ${spin.orbCount}")
                            Text(formatGrid(spin.grid), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun SettingsRow(label: String, value: String, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF162024), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Button(onClick = onToggle) { Text(value) }
    }
}

private fun formatGrid(grid: List<List<com.xenogenics.app.engine.Symbol>>): String {
    val rows = (0..2).map { row ->
        grid.joinToString(" ") { column ->
            when (column[row]) {
                com.xenogenics.app.engine.Symbol.NINE -> "9"
                com.xenogenics.app.engine.Symbol.TEN -> "10"
                com.xenogenics.app.engine.Symbol.J -> "J"
                com.xenogenics.app.engine.Symbol.Q -> "Q"
                com.xenogenics.app.engine.Symbol.K -> "K"
                com.xenogenics.app.engine.Symbol.A -> "A"
                com.xenogenics.app.engine.Symbol.MUTAGEN -> "M"
                com.xenogenics.app.engine.Symbol.NEURAL -> "N"
                com.xenogenics.app.engine.Symbol.SYNTH -> "S"
                com.xenogenics.app.engine.Symbol.CONTAINMENT -> "C"
                com.xenogenics.app.engine.Symbol.WILD -> "W"
                com.xenogenics.app.engine.Symbol.ORB -> "O"
            }
        }
    }
    return rows.joinToString(" | ")
}
