package com.xenogenics.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BioColorScheme = lightColorScheme(
    primary = ToxicGreen,
    secondary = BioCyan,
    tertiary = BioMagenta,
    background = DeepLab,
    surface = LabSteel,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun XenogenicsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BioColorScheme,
        typography = BioTypography,
        content = content
    )
}
