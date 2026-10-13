package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.compositionLocalOf

val LocalDarkThemeOverride = compositionLocalOf<Boolean?> { null }

private val SapphireTeal = Color(0xFF0284C7)

private val DarkColorScheme = darkColorScheme(
    primary = BlueAccent,
    onPrimary = Color.White,
    primaryContainer = NavyDark,
    onPrimaryContainer = BlueLight,
    secondary = SapphireTeal,
    onSecondary = Color.White,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = DarkHeadingBlue,        // Headings in blue in dark mode
    onSurface = DarkHeadingBlue,           // Surface headings in blue
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = DarkSubheadingOrange, // Subheadings in orange in dark mode
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = BlueLight,
    onPrimaryContainer = NavyDark,
    secondary = SapphireTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    background = Slate50,
    surface = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200
)

/**
 * Returns dynamic heading color: Blue, Green, or Orange in Dark mode; Slate900 in Light mode.
 */
@Composable
fun appHeadingColor(
    darkColor: Color = DarkHeadingBlue,
    lightColor: Color = Slate900
): Color {
    val isDark = LocalDarkThemeOverride.current ?: isSystemInDarkTheme()
    return if (isDark) darkColor else lightColor
}

/**
 * Returns dynamic subheading color: Orange, Blue, or Green in Dark mode; Slate500/Slate600 in Light mode.
 */
@Composable
fun appSubheadingColor(
    darkColor: Color = DarkSubheadingOrange,
    lightColor: Color = Slate500
): Color {
    val isDark = LocalDarkThemeOverride.current ?: isSystemInDarkTheme()
    return if (isDark) darkColor else lightColor
}

@Composable
fun appHeadingBlue(): Color = appHeadingColor(DarkHeadingBlue, Slate900)

@Composable
fun appHeadingGreen(): Color = appHeadingColor(DarkHeadingGreen, Slate900)

@Composable
fun appHeadingOrange(): Color = appHeadingColor(DarkHeadingOrange, Slate900)

@Composable
fun appSubheadingOrange(): Color = appSubheadingColor(DarkSubheadingOrange, Slate500)

@Composable
fun appSubheadingBlue(): Color = appSubheadingColor(DarkSubheadingBlue, Slate500)

@Composable
fun appSubheadingGreen(): Color = appSubheadingColor(DarkSubheadingGreen, Slate500)

/**
 * Enforces requirement: in dark mode, input text must be Black or Green (NOT white),
 * and placeholders must be Black (NOT white).
 */
@Composable
fun appTextFieldColors(
    preferGreenText: Boolean = true,
    placeholderIsBlack: Boolean = true
): TextFieldColors {
    val isDark = LocalDarkThemeOverride.current ?: isSystemInDarkTheme()
    val textColor = if (isDark) {
        if (preferGreenText) DarkInputTextGreen else DarkInputTextBlack
    } else Slate900

    val placeholderColor = if (isDark) {
        if (placeholderIsBlack) DarkInputPlaceholderBlack else Color.Black
    } else Slate400

    val containerColor = if (isDark) DarkInputContainerLight else Slate50

    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = textColor,
        unfocusedTextColor = textColor,
        focusedContainerColor = containerColor,
        unfocusedContainerColor = containerColor,
        focusedPlaceholderColor = placeholderColor,
        unfocusedPlaceholderColor = placeholderColor,
        focusedLabelColor = if (isDark) DarkInputTextGreen else RoyalBlue,
        unfocusedLabelColor = if (isDark) DarkInputPlaceholderBlack else Slate500,
        focusedBorderColor = if (isDark) DarkInputTextGreen else RoyalBlue,
        unfocusedBorderColor = if (isDark) Color(0xFF94A3B8) else Slate200,
        cursorColor = if (isDark) DarkInputTextGreen else RoyalBlue
    )
}


@Composable
fun CapitalHomeTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
