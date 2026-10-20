package au.edu.jcu.airrun.domain

import au.edu.jcu.airrun.data.model.Intensity
import au.edu.jcu.airrun.data.model.RunSettings
import au.edu.jcu.airrun.data.model.RunnerProfile
import kotlin.math.max

/** The answer the app exists to give: should I run outside right now? */
enum class RiskLevel {
    /** Go ahead. */
    OK,
    /** Go, but shorter or easier. */
    CAUTION,
    /** Move it indoors or wait. */
    AVOID;

    companion object {
        fun fromSeverity(severity: Double): RiskLevel = when {
            severity < 1.0 -> OK
            severity < 2.0 -> CAUTION
            else -> AVOID
        }
    }
}

enum class Factor { PSI, PM25, UV, RAIN }

/**
 * How much one factor counts against running, on a shared scale:
 * 0 ≤ severity < 1 is OK, 1 to 2 is CAUTION, 2 or more is AVOID.
 * The scale is continuous, so the factor *closest* to a limit can be named as the
 * deciding factor even when everything is OK.
 */
data class FactorAssessment(val factor: Factor, val severity: Double) {
    val risk: RiskLevel get() = RiskLevel.fromSeverity(severity)
}

data class RunAdvice(
    val verdict: RiskLevel,
    /** The factor with the highest severity: the one that decided the verdict. */
    val decidingFactor: Factor?,
    val assessments: List<FactorAssessment>,
)

/**
 * Turns readings plus settings into one verdict.
 *
 * The rules follow NEA's health advisories:
 *  - PSI ≤ 100 is fine for normal activity, PSI 101–200 means cutting down on strenuous
 *    exercise, and above 200 means avoiding it. A sensitive profile shifts every PSI
 *    threshold one band stricter.
 *  - PM2.5 Band I is fine, Band II calls for caution and Band III+ means avoid, for a jog.
 *    Walking relaxes this by one band, an intense session tightens it by one, and a
 *    sensitive profile tightens it by one more.
 *  - UV only ever calls for caution (8+, "very high"), because sunscreen and timing deal
 *    with it.
 *  - Showers call for caution because paths get slippery. Thundery showers mean avoid,
 *    because of lightning.
 *
 * The verdict is the worst included factor.
 */
object RunAdvisor {

    fun advise(conditions: RegionalConditions, settings: RunSettings): RunAdvice {
        val sensitiveShift = if (settings.profile == RunnerProfile.SENSITIVE) 1.0 else 0.0
        val intensityShift = when (settings.intensity) {
            Intensity.WALK -> -1.0
            Intensity.JOG -> 0.0
            Intensity.INTENSE -> 1.0
        }

        val assessments = buildList {
            conditions.psi24Hour?.let { psi ->
                // GOOD and MODERATE (positions 0–2) are both OK, so shift down by one band.
                add(FactorAssessment(Factor.PSI, nonNegative(PsiCategory.position(psi) - 1 + sensitiveShift)))
            }
            conditions.pm25OneHour?.let { pm25 ->
                add(FactorAssessment(Factor.PM25, nonNegative(Pm25Band.position(pm25) + sensitiveShift + intensityShift)))
            }
            if (settings.includeUv) {
                conditions.uvIndex?.let { uv ->
                    add(FactorAssessment(Factor.UV, (uv / UV_CAUTION_FROM).coerceAtMost(MAX_UV_SEVERITY)))
                }
            }
            if (settings.includeRain) {
                conditions.forecast?.let { forecast ->
                    val severity = when (RainOutlook.of(forecast)) {
                        RainOutlook.DRY -> 0.0
                        RainOutlook.RAIN -> 1.0
                        RainOutlook.THUNDERY -> 2.0
                    }
                    add(FactorAssessment(Factor.RAIN, severity))
                }
            }
        }

        val deciding = assessments.maxByOrNull { it.severity }
        return RunAdvice(
            verdict = deciding?.risk ?: RiskLevel.OK,
            decidingFactor = deciding?.factor,
            assessments = assessments,
        )
    }

    /** UV index at which a run needs caution ("very high" on the WHO scale). */
    private const val UV_CAUTION_FROM = 8.0

    /** UV never causes AVOID on its own, so keep its severity under 2. */
    private const val MAX_UV_SEVERITY = 1.99

    private fun nonNegative(value: Double) = max(0.0, value)
}
