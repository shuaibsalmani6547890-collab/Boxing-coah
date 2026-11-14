package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.AppThemeStyle

data class ArenaColors(
    val primary: Color = ArenaCrimson,
    val secondary: Color = MoltenGold,
    val tertiary: Color = SahiEmerald,
    val bg: Color = ObsidianBg,
    val surface: Color = ObsidianSurface,
    val card: Color = ObsidianCard,
    val cardElevated: Color = ObsidianCardElevated,
    val outline: Color = ObsidianOutline
)

val LocalArenaColors = staticCompositionLocalOf { ArenaColors() }

private fun buildColorScheme(style: AppThemeStyle) = when (style) {
    AppThemeStyle.CRIMSON_ARENA -> darkColorScheme(
        primary = ArenaCrimson,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF3C0D17),
        onPrimaryContainer = Color(0xFFFFB3BE),
        secondary = MoltenGold,
        onSecondary = ObsidianBg,
        secondaryContainer = Color(0xFF382904),
        onSecondaryContainer = MoltenGoldGlow,
        tertiary = SahiEmerald,
        onTertiary = ObsidianBg,
        tertiaryContainer = Color(0xFF06321F),
        onTertiaryContainer = SahiEmerald,
        error = GalatCrimson,
        onError = TextPrimary,
        background = ObsidianBg,
        onBackground = TextPrimary,
        surface = ObsidianSurface,
        onSurface = TextPrimary,
        surfaceVariant = ObsidianCard,
        onSurfaceVariant = TextSecondary,
        surfaceContainerLow = ObsidianSurface,
        surfaceContainer = ObsidianCard,
        surfaceContainerHigh = ObsidianCardElevated,
        outline = ObsidianOutline
    )
    AppThemeStyle.HUNTER_BLUE -> darkColorScheme(
        primary = HunterCyan,
        onPrimary = ObsidianBg,
        primaryContainer = Color(0xFF0A2A3F),
        onPrimaryContainer = HunterCyan,
        secondary = HunterViolet,
        onSecondary = TextPrimary,
        secondaryContainer = Color(0xFF281744),
        onSecondaryContainer = Color(0xFFC77DFF),
        tertiary = SahiEmerald,
        onTertiary = ObsidianBg,
        error = GalatCrimson,
        onError = TextPrimary,
        background = Color(0xFF080B14),
        onBackground = TextPrimary,
        surface = Color(0xFF0F1424),
        onSurface = TextPrimary,
        surfaceVariant = Color(0xFF151C32),
        onSurfaceVariant = TextSecondary,
        surfaceContainerLow = Color(0xFF0F1424),
        surfaceContainer = Color(0xFF151C32),
        surfaceContainerHigh = Color(0xFF1D2642),
        outline = Color(0xFF29355C)
    )
    AppThemeStyle.VIPER_EMERALD -> darkColorScheme(
        primary = ViperEmerald,
        onPrimary = ObsidianBg,
        primaryContainer = Color(0xFF073323),
        onPrimaryContainer = ViperEmerald,
        secondary = ViperLime,
        onSecondary = ObsidianBg,
        secondaryContainer = Color(0xFF253005),
        onSecondaryContainer = ViperLime,
        tertiary = MoltenGold,
        onTertiary = ObsidianBg,
        error = GalatCrimson,
        onError = TextPrimary,
        background = Color(0xFF070D0B),
        onBackground = TextPrimary,
        surface = Color(0xFF0E1714),
        onSurface = TextPrimary,
        surfaceVariant = Color(0xFF14211D),
        onSurfaceVariant = TextSecondary,
        surfaceContainerLow = Color(0xFF0E1714),
        surfaceContainer = Color(0xFF14211D),
        surfaceContainerHigh = Color(0xFF1C2D28),
        outline = Color(0xFF284239)
    )
}

val HunterShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun ShadowBoxingTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.CRIMSON_ARENA,
    content: @Composable () -> Unit
) {
    val scheme = buildColorScheme(themeStyle)
    val arenaColors = when (themeStyle) {
        AppThemeStyle.CRIMSON_ARENA -> ArenaColors(primary = ArenaCrimson, secondary = MoltenGold)
        AppThemeStyle.HUNTER_BLUE -> ArenaColors(primary = HunterCyan, secondary = HunterViolet, bg = Color(0xFF080B14), surface = Color(0xFF0F1424), card = Color(0xFF151C32))
        AppThemeStyle.VIPER_EMERALD -> ArenaColors(primary = ViperEmerald, secondary = ViperLime, bg = Color(0xFF070D0B), surface = Color(0xFF0E1714), card = Color(0xFF14211D))
    }

    CompositionLocalProvider(LocalArenaColors provides arenaColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            shapes = HunterShapes,
            content = content
        )
    }
}
