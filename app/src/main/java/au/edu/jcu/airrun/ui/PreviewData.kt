package au.edu.jcu.airrun.ui

import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.model.RunSettings
import au.edu.jcu.airrun.data.model.RunnerProfile
import au.edu.jcu.airrun.domain.RegionalConditions
import au.edu.jcu.airrun.domain.RunAdvisor
import java.time.OffsetDateTime

/** Sample dashboards for @Preview functions, matching the two mockup screens. */
object PreviewData {

    private fun dashboard(conditions: RegionalConditions, settings: RunSettings) =
        NowDashboard(settings, conditions, RunAdvisor.advise(conditions, settings))

    val clearDay: NowDashboard = dashboard(
        conditions = RegionalConditions(
            region = Region.WEST,
            psi24Hour = 48,
            pm25OneHour = 22,
            uvIndex = 6,
            forecastArea = "Jurong West",
            forecast = "Partly Cloudy (Day)",
            measuredAt = OffsetDateTime.parse("2026-10-14T19:00:00+08:00"),
        ),
        settings = RunSettings(),
    )

    val hazeDaySensitive: NowDashboard = dashboard(
        conditions = RegionalConditions(
            region = Region.WEST,
            psi24Hour = 142,
            pm25OneHour = 168,
            uvIndex = 3,
            forecastArea = "Jurong West",
            forecast = "Hazy",
            measuredAt = OffsetDateTime.parse("2026-10-14T16:00:00+08:00"),
        ),
        settings = RunSettings(profile = RunnerProfile.SENSITIVE),
    )
}
