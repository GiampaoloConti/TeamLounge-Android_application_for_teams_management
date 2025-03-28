package it.polito.teamlounge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColorScheme(
    primary = Purple200,
    secondary = Purple700,
    tertiary = Teal200
)

private val LightColorPalette = lightColorScheme(
    primary = PrimaryColor,
    secondary = Purple700,
    tertiary = Teal200

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

    @Composable
    fun TeamLoungeTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable() () -> Unit
    ) {
        val colors = LightColorPalette
    
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            shapes = Shapes,
        ) {
            content()
        }
    }