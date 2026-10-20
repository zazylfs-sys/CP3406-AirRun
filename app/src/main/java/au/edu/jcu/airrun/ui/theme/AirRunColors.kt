package au.edu.jcu.airrun.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import au.edu.jcu.airrun.domain.RiskLevel

/** Colours for one verdict card (go / caution / avoid). */
@Immutable
data class VerdictPalette(
    val container: Color,
    val title: Color,
    val body: Color,
    val label: Color,
    val divider: Color,
    val badge: Color,
    val onBadge: Color,
    val particle: Color,
    val chip: Color,
    val onChip: Color,
)

/** Calm / warn / alert: how worrying a reading is on its own official scale. */
enum class Tone { CALM, WARN, ALERT }

/**
 * App-specific colours that Material's ColorScheme has no slot for: the verdict card
 * palettes and the level-bar tones.
 */
@Immutable
data class AirRunColors(
    val go: VerdictPalette,
    val caution: VerdictPalette,
    val avoid: VerdictPalette,
    val calm: Color,
    val warn: Color,
    val alert: Color,
    val calmText: Color,
    val warnText: Color,
    val alertText: Color,
    val track: Color,
    val sunGlow: Color,
    val haze: Color,
) {
    fun verdict(level: RiskLevel): VerdictPalette = when (level) {
        RiskLevel.OK -> go
        RiskLevel.CAUTION -> caution
        RiskLevel.AVOID -> avoid
    }

    fun bar(tone: Tone): Color = when (tone) {
        Tone.CALM -> calm
        Tone.WARN -> warn
        Tone.ALERT -> alert
    }

    fun text(tone: Tone): Color = when (tone) {
        Tone.CALM -> calmText
        Tone.WARN -> warnText
        Tone.ALERT -> alertText
    }
}

val LightAirRunColors = AirRunColors(
    go = VerdictPalette(
        container = Mint, title = PineDeep, body = Color(0xFF2E4A3F), label = Color(0xFF2E5A49),
        divider = Color(0xFFBFDCCD), badge = Pine, onBadge = Color.White,
        particle = Color.White, chip = Color.White, onChip = Ink,
    ),
    caution = VerdictPalette(
        container = Color(0xFFFBEBC8), title = Color(0xFF3F2B00), body = Color(0xFF5A4410),
        label = Color(0xFF6E5208), divider = Color(0xFFEBD39A), badge = Color(0xFFB7791F),
        onBadge = Color.White, particle = Color(0xFFD9A441), chip = Color.White, onChip = Ink,
    ),
    avoid = VerdictPalette(
        container = Color(0xFFF8DED3), title = Color(0xFF4F1A0A), body = Color(0xFF6B3424),
        label = Color(0xFF83391F), divider = Color(0xFFEBC0AE), badge = Color(0xFFC2502A),
        onBadge = Color.White, particle = Color(0xFFC98468), chip = Color.White, onChip = Ink,
    ),
    calm = Color(0xFF2FA37C),
    warn = Color(0xFFE0A526),
    alert = Color(0xFFE0703A),
    calmText = Pine,
    warnText = Color(0xFF8A5A00),
    alertText = Color(0xFFA8401A),
    track = Hairline,
    sunGlow = Color(0xFFFFD26E),
    haze = Color(0xFFD68C69),
)

val DarkAirRunColors = AirRunColors(
    go = VerdictPalette(
        container = Color(0xFF16352A), title = MintInk, body = Color(0xFFB5D9C8),
        label = Color(0xFF8FC7AE), divider = Color(0xFF275240), badge = Jade,
        onBadge = GroundDark, particle = Color(0xFFBFEBD8), chip = GroundDark, onChip = MintInk,
    ),
    caution = VerdictPalette(
        container = Color(0xFF3A2F12), title = Color(0xFFFFEFC7), body = Color(0xFFEBD9AE),
        label = Color(0xFFF2C14E), divider = Color(0xFF5A4A1F), badge = Color(0xFFF2C14E),
        onBadge = Color(0xFF2A1F05), particle = Color(0xFFE8C77A), chip = GroundDark,
        onChip = Color(0xFFFFEFC7),
    ),
    avoid = VerdictPalette(
        container = Color(0xFF3A2018), title = Color(0xFFFFE6D8), body = Color(0xFFF0D2C2),
        label = Color(0xFFF3C2A8), divider = Color(0xFF5A3427), badge = Color(0xFFFF9A5C),
        onBadge = Color(0xFF2A140C), particle = Color(0xFFE7A98A), chip = GroundDark,
        onChip = Color(0xFFFFE6D8),
    ),
    calm = Jade,
    warn = Color(0xFFF2C14E),
    alert = Color(0xFFFF9A5C),
    calmText = Jade,
    warnText = Color(0xFFF2C14E),
    alertText = Color(0xFFFFB38A),
    track = Color(0xFF2A3430),
    sunGlow = Color(0xFFFFD26E),
    haze = Color(0xFFD68C69),
)

val LocalAirRunColors = staticCompositionLocalOf { LightAirRunColors }
