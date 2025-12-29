package com.xenogenics.app.ui

import android.app.Application
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xenogenics.app.engine.GameState
import com.xenogenics.app.ui.theme.BioCyan
import com.xenogenics.app.ui.theme.BioMagenta
import com.xenogenics.app.ui.theme.DeepLab
import com.xenogenics.app.ui.theme.LabSteel
import com.xenogenics.app.ui.theme.PanelGlow
import com.xenogenics.app.ui.theme.ToxicGreen
import com.xenogenics.app.viewmodel.GameViewModel
import com.xenogenics.app.viewmodel.GameViewModelFactory

@Composable
fun CabinetScreen() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "cabinet") {
        composable("cabinet") {
            CabinetRoute()
        }
    }
}

@Composable
private fun CabinetRoute() {
    val context = LocalContext.current
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(context.applicationContext as Application)
    )
    val state by viewModel.state.collectAsState()
    val haptics = LocalHapticFeedback.current
    val audioController = remember { AudioController(context) }
    DisposableEffect(Unit) {
        onDispose { audioController.release() }
    }

    var lastState: GameState? by remember { mutableStateOf(null) }
    var lastBonusFilled by remember { mutableIntStateOf(0) }

    LaunchedEffect(state) {
        val settings = state.core.settings
        when {
            state is GameState.Spinning && lastState !is GameState.Spinning -> {
                audioController.play(SoundEvent.SPIN, settings.soundEnabled)
            }
            state is GameState.ShowingWins && lastState !is GameState.ShowingWins -> {
                audioController.play(SoundEvent.WIN_TICK, settings.soundEnabled)
                if (settings.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            state is GameState.EnteringBonus && lastState !is GameState.EnteringBonus -> {
                audioController.play(SoundEvent.BONUS_TRIGGER, settings.soundEnabled)
                if (settings.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            state is GameState.BonusRespin -> {
                val filled = state.bonusState.grid.sumOf { column -> column.count { it != null } }
                if (filled > lastBonusFilled) {
                    audioController.play(SoundEvent.ORB_LAND, settings.soundEnabled)
                    if (settings.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                lastBonusFilled = filled
            }
            state is GameState.BonusResult && lastState !is GameState.BonusResult -> {
                audioController.play(SoundEvent.JACKPOT, settings.soundEnabled)
                if (settings.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
        lastState = state
    }

    var overlay by remember { mutableStateOf(Overlay.None) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepLab, LabSteel, DeepLab)
                )
            )
    ) {
        val targetRatio = 16f / 9f
        val useHeight = maxWidth / maxHeight > targetRatio
        val cabinetModifier = if (useHeight) {
            Modifier.fillMaxHeight().aspectRatio(targetRatio)
        } else {
            Modifier.fillMaxWidth().aspectRatio(targetRatio)
        }
        val scanlineOffset = if (state.core.settings.reducedMotion) {
            0f
        } else {
            rememberInfiniteTransition(label = "scanlines")
                .animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
                    label = "scanlines"
                ).value
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = cabinetModifier.padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                JackpotPanel(state.core.jackpotMeters)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (state is GameState.BonusRespin || state is GameState.BonusResult || state is GameState.EnteringBonus) {
                        BonusGridComposable(state)
                    } else {
                        ReelsComposable(state)
                    }
                    ReelFrameOverlay(scanlineOffset, state)
                }
                Spacer(modifier = Modifier.height(8.dp))
                ControlDeck(
                    state = state,
                    onSpin = { viewModel.onSpin() },
                    onAuto = { overlay = Overlay.AutoSpin },
                    onMaxBet = { viewModel.maxBet() },
                    onBetPlus = { viewModel.increaseBet() },
                    onBetMinus = { viewModel.decreaseBet() },
                    onDenom = { viewModel.cycleDenom() },
                    onInfo = { overlay = Overlay.Info },
                    onSound = { viewModel.toggleSound() },
                    onSettings = { overlay = Overlay.Settings }
                )
            }
        }

        BioCabinetChrome()
        Scanlines(scanlineOffset)
        GlitchOverlay(scanlineOffset)

        Overlays(
            overlay = overlay,
            state = state,
            onDismiss = { overlay = Overlay.None },
            onStartAuto = { count, stopOnFeature ->
                overlay = Overlay.None
                viewModel.startAutoSpin(count, stopOnFeature)
            },
            onStopAuto = { viewModel.stopAutoSpin() },
            onToggleFast = { viewModel.toggleFastSpin() },
            onToggleHaptics = { viewModel.toggleHaptics() },
            onToggleReducedMotion = { viewModel.toggleReducedMotion() },
            onToggleDevTools = { viewModel.toggleDevTools() },
            onSetCredits = { viewModel.setCredits(it) },
            onForceBonus = { viewModel.setForceBonusNext(it) },
            onSetSeed = { viewModel.setRngSeed(it) }
        )
    }
}

@Composable
private fun ReelFrameOverlay(scanlineOffset: Float, state: GameState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
    ) {
        val indicator = if (state is GameState.BonusRespin || state is GameState.BonusResult || state is GameState.EnteringBonus) {
            "BONUS REELS IN PLAY"
        } else {
            "LAST SPIN"
        }
        Text(
            text = indicator,
            style = MaterialTheme.typography.titleMedium,
            color = ToxicGreen,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
        Text(
            text = "WIN ${state.core.lastWin}",
            style = MaterialTheme.typography.titleMedium,
            color = BioCyan,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )

        val dripAlpha = 0.35f + 0.15f * kotlin.math.sin(scanlineOffset * Math.PI * 2).toFloat()
        OozeDrips(alpha = dripAlpha)
    }
}

@Composable
private fun BioCabinetChrome() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "ULTIMATE BIO-LINK",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = BioMagenta,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun Scanlines(offset: Float) {
    val alpha = 0.08f
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = 8f
        var y = (offset * spacing * 4)
        while (y < size.height) {
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += spacing
        }
    }
}

@Composable
private fun GlitchOverlay(phase: Float) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val bandHeight = 6f
        val count = 6
        for (i in 0 until count) {
            val y = (phase * size.height + i * 120f) % size.height
            drawRect(
                color = BioMagenta.copy(alpha = 0.08f),
                topLeft = Offset(0f, y),
                size = androidx.compose.ui.geometry.Size(size.width, bandHeight)
            )
        }
    }
}

@Composable
private fun OozeDrips(alpha: Float) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val width = size.width
        val dripHeight = size.height * 0.08f
        path.moveTo(0f, 0f)
        val step = width / 6
        for (i in 0..6) {
            val x = i * step
            val y = if (i % 2 == 0) dripHeight else dripHeight * 0.5f
            path.lineTo(x, y)
        }
        path.lineTo(width, 0f)
        path.close()
        drawPath(path, color = PanelGlow.copy(alpha = alpha))

        drawPath(
            path,
            color = BioCyan.copy(alpha = alpha * 0.5f),
            style = Stroke(width = 3f)
        )
    }
}
