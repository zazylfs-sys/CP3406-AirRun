package au.edu.jcu.airrun.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Pine,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = PineDeep,
    secondaryContainer = Mint,
    onSecondaryContainer = PineDeep,
    background = Ground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    onSurfaceVariant = InkMuted,
    surfaceContainer = Color.White,
    surfaceContainerHighest = Hairline,
    outline = ChipBorder,
    outlineVariant = Hairline,
)

private val DarkColorScheme = darkColorScheme(
    primary = Jade,
    onPrimary = GroundDark,
    primaryContainer = MintDark,
    onPrimaryContainer = MintInk,
    secondaryContainer = MintDark,
    onSecondaryContainer = MintInk,
    background = GroundDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    onSurfaceVariant = InkMutedDark,
    surfaceContainer = NavDark,
    surfaceContainerHighest = Color(0xFF2A3430),
    outline = ChipBorderDark,
    outlineVariant = HairlineDark,
)

/**
 * Air Run's Material 3 theme. Dynamic colour is deliberately off, because the greens
 * and oranges carry meaning (clean air vs. haze) and shouldn't change with the wallpaper.
 */
@Composable
fun AirRunTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAirRunColors provides if (darkTheme) DarkAirRunColors else LightAirRunColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content,
        )
    }
}

/** Shortcut for the app-specific colours: `AirRunTheme.colors.calm`. */
object AirRunTheme {
    val colors: AirRunColors
        @Composable @ReadOnlyComposable
        get() = LocalAirRunColors.current
}
