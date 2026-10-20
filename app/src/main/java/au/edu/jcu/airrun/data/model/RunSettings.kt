package au.edu.jcu.airrun.data.model

/** Who is running. Sensitive runners get stricter air-quality thresholds. */
enum class RunnerProfile { GENERAL, SENSITIVE }

/** How hard the session is. Harder sessions mean deeper breathing, so PM2.5 matters more. */
enum class Intensity { WALK, JOG, INTENSE }

/**
 * Everything on the Settings screen. The assessment spec says settings don't need to
 * persist, so these live in memory only (see SettingsRepository).
 */
data class RunSettings(
    val region: Region = Region.WEST,
    val profile: RunnerProfile = RunnerProfile.GENERAL,
    val intensity: Intensity = Intensity.JOG,
    val includeRain: Boolean = true,
    val includeUv: Boolean = true,
)
