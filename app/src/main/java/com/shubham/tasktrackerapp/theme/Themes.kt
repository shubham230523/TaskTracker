package com.shubham.tasktrackerapp.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val TaskTrackerDarkColorScheme = darkColorScheme(
    primary = Teal200,
    onPrimary = Black,
    inversePrimary = Teal400,
    primaryContainer = Teal600,
    onPrimaryContainer = Teal100,
    secondary = Blue200,
    onSecondary = Black,
    secondaryContainer = Blue600,
    onSecondaryContainer = Blue100,
    tertiary = Purple200,
    onTertiary = Black,
    tertiaryContainer = Purple600,
    onTertiaryContainer = Purple100,
    error = Red200,
    onError = Black,
    errorContainer = Red400,
    onErrorContainer = Red100,
    background = Black,
    onBackground = Grey100,
    surface = Grey800,
    onSurface = White,
    surfaceVariant = Grey400,
    onSurfaceVariant = White
)

private val TaskTrackerLightColorScheme = lightColorScheme(
    primary = Teal600,
    onPrimary = White,
    inversePrimary = Teal200,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal600,
    secondary = Blue600,
    onSecondary = White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Blue600,
    tertiary = Purple600,
    onTertiary = White,
    tertiaryContainer = Purple100,
    onTertiaryContainer = Purple600,
    error = Red600,
    onError = White,
    errorContainer = Red100,
    onErrorContainer = Red600,
    background = White,
    onBackground = Black,
    surface = Grey100,
    onSurface = Black,
    surfaceVariant = BlueGrey100,
    onSurfaceVariant = Black
)

@Composable
fun TaskTrackerTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && false
    val myColorScheme = when {
        dynamicColor && isDarkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        dynamicColor && !isDarkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        isDarkTheme -> TaskTrackerDarkColorScheme
        else -> TaskTrackerLightColorScheme
    }

    MaterialTheme(
        colorScheme = myColorScheme,
        typography = TaskTrackerTopography,
        content = content
    )
}
