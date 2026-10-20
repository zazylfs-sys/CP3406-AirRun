package au.edu.jcu.airrun.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import au.edu.jcu.airrun.R
import au.edu.jcu.airrun.data.model.Intensity
import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.model.RunnerProfile
import au.edu.jcu.airrun.domain.Factor
import au.edu.jcu.airrun.domain.Pm25Band
import au.edu.jcu.airrun.domain.PsiCategory
import au.edu.jcu.airrun.domain.RainOutlook
import au.edu.jcu.airrun.domain.RiskLevel
import au.edu.jcu.airrun.domain.UvCategory
import au.edu.jcu.airrun.ui.theme.Tone
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/*
 * Maps domain values to user-facing text and visual levels. Keeping this out of the
 * domain layer means the rules stay plain Kotlin and all copy lives in strings.xml.
 */

/** How a reading is drawn on a 4-segment level bar. */
data class ReadingLevel(val segments: Int, val tone: Tone, @StringRes val label: Int)

val PsiCategory.level: ReadingLevel
    get() = when (this) {
        PsiCategory.GOOD -> ReadingLevel(1, Tone.CALM, R.string.psi_good)
        PsiCategory.MODERATE -> ReadingLevel(2, Tone.WARN, R.string.psi_moderate)
        PsiCategory.UNHEALTHY -> ReadingLevel(3, Tone.ALERT, R.string.psi_unhealthy)
        PsiCategory.VERY_UNHEALTHY -> ReadingLevel(4, Tone.ALERT, R.string.psi_very_unhealthy)
        PsiCategory.HAZARDOUS -> ReadingLevel(4, Tone.ALERT, R.string.psi_hazardous)
    }

val Pm25Band.level: ReadingLevel
    get() = when (this) {
        Pm25Band.NORMAL -> ReadingLevel(1, Tone.CALM, R.string.pm25_normal)
        Pm25Band.ELEVATED -> ReadingLevel(2, Tone.WARN, R.string.pm25_elevated)
        Pm25Band.HIGH -> ReadingLevel(3, Tone.ALERT, R.string.pm25_high)
        Pm25Band.VERY_HIGH -> ReadingLevel(4, Tone.ALERT, R.string.pm25_very_high)
    }

val UvCategory.level: ReadingLevel
    get() = when (this) {
        UvCategory.LOW -> ReadingLevel(1, Tone.CALM, R.string.uv_low)
        UvCategory.MODERATE -> ReadingLevel(2, Tone.WARN, R.string.uv_moderate)
        UvCategory.HIGH -> ReadingLevel(3, Tone.WARN, R.string.uv_high)
        UvCategory.VERY_HIGH -> ReadingLevel(4, Tone.ALERT, R.string.uv_very_high)
        UvCategory.EXTREME -> ReadingLevel(4, Tone.ALERT, R.string.uv_extreme)
    }

val RainOutlook.level: ReadingLevel
    get() = when (this) {
        RainOutlook.DRY -> ReadingLevel(1, Tone.CALM, R.string.rain_dry)
        RainOutlook.RAIN -> ReadingLevel(2, Tone.WARN, R.string.rain_wet)
        RainOutlook.THUNDERY -> ReadingLevel(4, Tone.ALERT, R.string.rain_thundery)
    }

@get:StringRes
val Region.label: Int
    get() = when (this) {
        Region.NORTH -> R.string.region_north
        Region.CENTRAL -> R.string.region_central
        Region.EAST -> R.string.region_east
        Region.WEST -> R.string.region_west
        Region.SOUTH -> R.string.region_south
    }

@get:StringRes
val RunnerProfile.label: Int
    get() = when (this) {
        RunnerProfile.GENERAL -> R.string.profile_general
        RunnerProfile.SENSITIVE -> R.string.profile_sensitive
    }

@get:StringRes
val Intensity.label: Int
    get() = when (this) {
        Intensity.WALK -> R.string.intensity_walk
        Intensity.JOG -> R.string.intensity_jog
        Intensity.INTENSE -> R.string.intensity_intense
    }

/** "West Singapore" */
@Composable
fun regionPlaceName(region: Region): String =
    stringResource(R.string.region_place, stringResource(region.label))

/** "RIGHT NOW · JOGGING · SENSITIVE" */
@Composable
fun verdictLabel(dashboard: NowDashboard): String {
    val settings = dashboard.settings
    val activity = when (settings.intensity) {
        Intensity.WALK -> R.string.activity_walk
        Intensity.JOG -> R.string.activity_jog
        Intensity.INTENSE -> R.string.activity_intense
    }
    val parts = buildList {
        add(stringResource(R.string.right_now))
        add(stringResource(activity))
        if (settings.profile == RunnerProfile.SENSITIVE) add(stringResource(R.string.profile_sensitive_tag))
    }
    return parts.joinToString(" · ").uppercase()
}

@Composable
fun verdictTitle(verdict: RiskLevel, intensity: Intensity): String = stringResource(
    when (verdict) {
        RiskLevel.OK -> if (intensity == Intensity.WALK) R.string.verdict_go_walk else R.string.verdict_go
        RiskLevel.CAUTION -> R.string.verdict_caution
        RiskLevel.AVOID -> R.string.verdict_avoid
    }
)

/** The verdict explanation: why, plus a sunscreen tip when the sun is strong. */
@Composable
fun verdictBody(dashboard: NowDashboard): String {
    val conditions = dashboard.conditions
    val advice = dashboard.advice
    val place = regionPlaceName(conditions.region)
    val town = conditions.forecastArea ?: stringResource(R.string.nearby)
    val forecast = conditions.forecast.orEmpty()
    val sensitive = dashboard.settings.profile == RunnerProfile.SENSITIVE

    val main = when (advice.verdict) {
        RiskLevel.OK -> stringResource(R.string.body_go, place)
        RiskLevel.CAUTION -> when (advice.decidingFactor) {
            Factor.PSI -> {
                val psi = conditions.psi24Hour ?: 0
                val category = stringResource(PsiCategory.of(psi).level.label).lowercase()
                stringResource(R.string.body_caution_psi, category, place, psi)
            }
            Factor.PM25 -> stringResource(R.string.body_caution_pm25, conditions.pm25OneHour ?: 0, place)
            Factor.RAIN -> stringResource(R.string.body_caution_rain, forecast, town)
            Factor.UV, null -> stringResource(R.string.body_caution_uv)
        }
        RiskLevel.AVOID -> when (advice.decidingFactor) {
            Factor.RAIN -> stringResource(R.string.body_avoid_thunder, forecast, town)
            else -> {
                val pollutant = if (advice.decidingFactor == Factor.PSI) "PSI" else "PM2.5"
                val res = if (sensitive) R.string.body_avoid_air_sensitive else R.string.body_avoid_air
                stringResource(res, pollutant, place)
            }
        }
    }

    // Sunscreen tip: when UV is at least moderate, the run is still on, and UV isn't
    // already the main message.
    val uv = conditions.uvIndex
    val uvIsMainMessage = advice.verdict == RiskLevel.CAUTION && advice.decidingFactor == Factor.UV
    if (uv == null || uv < 3 || !dashboard.settings.includeUv ||
        advice.verdict == RiskLevel.AVOID || uvIsMainMessage
    ) {
        return main
    }
    val uvWord = stringResource(UvCategory.of(uv).level.label).lowercase()
    return main + " " + stringResource(R.string.tip_sunscreen, uvWord)
}

/** Short chip text for the deciding factor, e.g. "PM2.5 168 µg/m³". */
@Composable
fun decidingFactorText(dashboard: NowDashboard): String? {
    val conditions = dashboard.conditions
    return when (dashboard.advice.decidingFactor) {
        Factor.PSI -> conditions.psi24Hour?.let { stringResource(R.string.factor_psi, it) }
        Factor.PM25 -> conditions.pm25OneHour?.let { stringResource(R.string.factor_pm25, it) }
        Factor.UV -> conditions.uvIndex?.let { stringResource(R.string.factor_uv, it) }
        Factor.RAIN -> conditions.forecast
        null -> null
    }
}

/** The tone of the deciding factor's own reading, used for the chip's dot. */
fun decidingFactorTone(dashboard: NowDashboard): Tone {
    val c = dashboard.conditions
    return when (dashboard.advice.decidingFactor) {
        Factor.PSI -> c.psi24Hour?.let { PsiCategory.of(it).level.tone }
        Factor.PM25 -> c.pm25OneHour?.let { Pm25Band.of(it).level.tone }
        Factor.UV -> c.uvIndex?.let { UvCategory.of(it).level.tone }
        Factor.RAIN -> c.forecast?.let { RainOutlook.of(it).level.tone }
        null -> null
    } ?: Tone.CALM
}

/** "7:00 PM" in the device's time zone and locale. */
fun formatClockTime(time: OffsetDateTime, zone: ZoneId = ZoneId.systemDefault()): String =
    time.atZoneSameInstant(zone).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
