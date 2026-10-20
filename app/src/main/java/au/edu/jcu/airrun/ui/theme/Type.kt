package au.edu.jcu.airrun.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import au.edu.jcu.airrun.R

/** Body and UI text (bundled from Google Fonts, OFL licence). */
val DmSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
)

/** Big numbers and headlines. */
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
)

private val base = Typography()

val Typography = Typography(
    // Verdict headline, e.g. "Good to run"
    displayMedium = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,
        fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = (-1).sp,
    ),
    // Metric numbers, e.g. PSI "48"
    displaySmall = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,
        fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1).sp,
    ),
    // Screen title, e.g. "Settings"
    headlineMedium = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,
        fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp,
    ),
    // Short forecast text inside a tile, e.g. "Partly Cloudy"
    titleLarge = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 24.sp,
    ),
    titleMedium = base.titleMedium.copy(fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    titleSmall = base.titleSmall.copy(fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge = base.bodyLarge.copy(fontFamily = DmSans, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.sp),
    bodyMedium = base.bodyMedium.copy(fontFamily = DmSans, fontSize = 14.sp, letterSpacing = 0.sp),
    bodySmall = base.bodySmall.copy(fontFamily = DmSans, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
    labelLarge = base.labelLarge.copy(fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = base.labelMedium.copy(fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelSmall = base.labelSmall.copy(fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.2.sp),
)
