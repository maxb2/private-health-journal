package com.privatehealthjournal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val BiometricOrangeLight = Color(0xFFFFE0CC)
val BiometricOrangeMed = Color(0xFFE89048)
val BiometricOrangeDark = Color(0xFF8A4214)
val BiometricOrangeContainerDark = Color(0xFF4A2810)
val BiometricOrangeAccentDark = Color(0xFFFFB07A)

@Composable
fun biometricContainerColor(): Color =
    if (isSystemInDarkTheme()) BiometricOrangeContainerDark else BiometricOrangeLight

@Composable
fun onBiometricContainerColor(): Color =
    if (isSystemInDarkTheme()) BiometricOrangeLight else BiometricOrangeDark

@Composable
fun biometricAccentColor(): Color =
    if (isSystemInDarkTheme()) BiometricOrangeAccentDark else BiometricOrangeMed

val CycleMaroonLight = Color(0xFFF5D5DA)
val CycleMaroonAccent = Color(0xFF7A2230)
val CycleMaroonDark = Color(0xFF4D0F1A)
val CycleMaroonContainerDark = Color(0xFF3D101A)
val CycleMaroonAccentDark = Color(0xFFD88898)

@Composable
fun cycleContainerColor(): Color =
    if (isSystemInDarkTheme()) CycleMaroonContainerDark else CycleMaroonLight

@Composable
fun onCycleContainerColor(): Color =
    if (isSystemInDarkTheme()) CycleMaroonLight else CycleMaroonDark

@Composable
fun cycleAccentColor(): Color =
    if (isSystemInDarkTheme()) CycleMaroonAccentDark else CycleMaroonAccent

val OtherGreyLight = Color(0xFFE8E8E8)
val OtherGreyAccent = Color(0xFF6B6B6B)
val OtherGreyDark = Color(0xFF3A3A3A)
val OtherGreyContainerDark = Color(0xFF2A2A2A)
val OtherGreyAccentDark = Color(0xFFA0A0A0)

@Composable
fun otherContainerColor(): Color =
    if (isSystemInDarkTheme()) OtherGreyContainerDark else OtherGreyLight

@Composable
fun onOtherContainerColor(): Color =
    if (isSystemInDarkTheme()) OtherGreyLight else OtherGreyDark

@Composable
fun otherAccentColor(): Color =
    if (isSystemInDarkTheme()) OtherGreyAccentDark else OtherGreyAccent

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5FA0D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEDF5),
    onPrimaryContainer = Color(0xFF1F4666),
    secondary = Color(0xFF4A9938),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDEBCE),
    onSecondaryContainer = Color(0xFF1F4612),
    tertiary = Color(0xFFD55050),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF8D7D6),
    onTertiaryContainer = Color(0xFF6E1622),
    error = Color(0xFFA82238),
    onError = Color.White,
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB6D9EC),
    onPrimary = Color(0xFF1F4666),
    primaryContainer = Color(0xFF24445E),
    onPrimaryContainer = Color(0xFFDCEDF5),
    secondary = Color(0xFF7DBE58),
    onSecondary = Color(0xFF1F4612),
    secondaryContainer = Color(0xFF274E1B),
    onSecondaryContainer = Color(0xFFDDEBCE),
    tertiary = Color(0xFFE97A78),
    onTertiary = Color(0xFF6E1622),
    tertiaryContainer = Color(0xFF5A1A26),
    onTertiaryContainer = Color(0xFFF8D7D6),
    error = Color(0xFFE97A78),
    onError = Color(0xFF6E1622),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
)

@Composable
fun PrivateHealthJournalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
