package au.edu.jcu.airrun.domain

import au.edu.jcu.airrun.data.model.Intensity
import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.model.RunSettings
import au.edu.jcu.airrun.data.model.RunnerProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunAdvisorTest {

    private fun conditions(
        psi: Int? = 40,
        pm25: Int? = 20,
        uv: Int? = 2,
        forecast: String? = "Fair (Day)",
    ) = RegionalConditions(
        region = Region.WEST,
        psi24Hour = psi,
        pm25OneHour = pm25,
        uvIndex = uv,
        forecastArea = "Jurong West",
        forecast = forecast,
        measuredAt = null,
    )

    @Test
    fun clearDay_isGoodToRun_withUvAsDecidingFactor() {
        // The "clear air" mockup: PSI 48, PM2.5 22, UV 6, partly cloudy.
        val advice = RunAdvisor.advise(conditions(psi = 48, pm25 = 22, uv = 6, forecast = "Partly Cloudy (Day)"), RunSettings())

        assertEquals(RiskLevel.OK, advice.verdict)
        assertEquals(Factor.UV, advice.decidingFactor)
    }

    @Test
    fun hazeDay_sensitiveJogger_shouldTakeItIndoors_becauseOfPm25() {
        // The "haze day" mockup: PSI 142, PM2.5 168, sensitive profile.
        val settings = RunSettings(profile = RunnerProfile.SENSITIVE)
        val advice = RunAdvisor.advise(conditions(psi = 142, pm25 = 168, uv = 3, forecast = "Hazy"), settings)

        assertEquals(RiskLevel.AVOID, advice.verdict)
        assertEquals(Factor.PM25, advice.decidingFactor)
    }

    @Test
    fun unhealthyPsi_isCautionForGeneralAdults_butAvoidForSensitive() {
        val haze = conditions(psi = 144, pm25 = 40)

        assertEquals(RiskLevel.CAUTION, RunAdvisor.advise(haze, RunSettings()).verdict)
        assertEquals(
            RiskLevel.AVOID,
            RunAdvisor.advise(haze, RunSettings(profile = RunnerProfile.SENSITIVE)).verdict,
        )
    }

    @Test
    fun moderatePsi_isStillFineForEveryone() {
        val advice = RunAdvisor.advise(conditions(psi = 100, pm25 = 20), RunSettings())
        assertEquals(RiskLevel.OK, advice.verdict)
    }

    @Test
    fun intensity_tightensThePm25Limit() {
        val elevated = conditions(pm25 = 100) // Band II

        assertEquals(RiskLevel.OK, RunAdvisor.advise(elevated, RunSettings(intensity = Intensity.WALK)).verdict)
        assertEquals(RiskLevel.CAUTION, RunAdvisor.advise(elevated, RunSettings(intensity = Intensity.JOG)).verdict)
        assertEquals(RiskLevel.AVOID, RunAdvisor.advise(elevated, RunSettings(intensity = Intensity.INTENSE)).verdict)
    }

    @Test
    fun showers_meanCaution_andThunder_meansAvoid() {
        val showers = RunAdvisor.advise(conditions(forecast = "Passing Showers"), RunSettings())
        assertEquals(RiskLevel.CAUTION, showers.verdict)
        assertEquals(Factor.RAIN, showers.decidingFactor)

        val thunder = RunAdvisor.advise(conditions(forecast = "Thundery Showers"), RunSettings())
        assertEquals(RiskLevel.AVOID, thunder.verdict)
    }

    @Test
    fun excludingRain_ignoresTheForecast() {
        val settings = RunSettings(includeRain = false)
        val advice = RunAdvisor.advise(conditions(forecast = "Thundery Showers"), settings)

        assertEquals(RiskLevel.OK, advice.verdict)
        assertTrue(advice.assessments.none { it.factor == Factor.RAIN })
    }

    @Test
    fun uv_canCautionButNeverForcesIndoors() {
        assertEquals(RiskLevel.OK, RunAdvisor.advise(conditions(uv = 7), RunSettings()).verdict)
        assertEquals(RiskLevel.CAUTION, RunAdvisor.advise(conditions(uv = 8), RunSettings()).verdict)
        assertEquals(RiskLevel.CAUTION, RunAdvisor.advise(conditions(uv = 15), RunSettings()).verdict)
    }

    @Test
    fun excludingUv_dropsItFromTheVerdict() {
        val advice = RunAdvisor.advise(conditions(uv = 11), RunSettings(includeUv = false))

        assertEquals(RiskLevel.OK, advice.verdict)
        assertTrue(advice.assessments.none { it.factor == Factor.UV })
    }

    @Test
    fun missingReadings_areSkipped() {
        val advice = RunAdvisor.advise(conditions(psi = null, uv = null, forecast = null), RunSettings())

        assertEquals(listOf(Factor.PM25), advice.assessments.map { it.factor })
    }

    @Test
    fun noReadingsAtAll_defaultsToOkWithNoDecidingFactor() {
        val advice = RunAdvisor.advise(conditions(psi = null, pm25 = null, uv = null, forecast = null), RunSettings())

        assertEquals(RiskLevel.OK, advice.verdict)
        assertNull(advice.decidingFactor)
    }
}
