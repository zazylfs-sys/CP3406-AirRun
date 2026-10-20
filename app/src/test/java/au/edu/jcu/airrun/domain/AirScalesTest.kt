package au.edu.jcu.airrun.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AirScalesTest {

    @Test
    fun psiCategory_followsNeaBandEdges() {
        assertEquals(PsiCategory.GOOD, PsiCategory.of(0))
        assertEquals(PsiCategory.GOOD, PsiCategory.of(50))
        assertEquals(PsiCategory.MODERATE, PsiCategory.of(51))
        assertEquals(PsiCategory.MODERATE, PsiCategory.of(100))
        assertEquals(PsiCategory.UNHEALTHY, PsiCategory.of(101))
        assertEquals(PsiCategory.UNHEALTHY, PsiCategory.of(200))
        assertEquals(PsiCategory.VERY_UNHEALTHY, PsiCategory.of(201))
        assertEquals(PsiCategory.HAZARDOUS, PsiCategory.of(301))
    }

    @Test
    fun psiCategory_offTheScaleStaysHazardous() {
        assertEquals(PsiCategory.HAZARDOUS, PsiCategory.of(999))
    }

    @Test
    fun pm25Band_followsNeaOneHourBands() {
        assertEquals(Pm25Band.NORMAL, Pm25Band.of(55))
        assertEquals(Pm25Band.ELEVATED, Pm25Band.of(56))
        assertEquals(Pm25Band.ELEVATED, Pm25Band.of(150))
        assertEquals(Pm25Band.HIGH, Pm25Band.of(151))
        assertEquals(Pm25Band.VERY_HIGH, Pm25Band.of(251))
    }

    @Test
    fun uvCategory_followsWhoScale() {
        assertEquals(UvCategory.LOW, UvCategory.of(2))
        assertEquals(UvCategory.MODERATE, UvCategory.of(3))
        assertEquals(UvCategory.HIGH, UvCategory.of(6))
        assertEquals(UvCategory.VERY_HIGH, UvCategory.of(8))
        assertEquals(UvCategory.EXTREME, UvCategory.of(11))
    }

    @Test
    fun scalePosition_isBandIndexPlusFraction() {
        assertEquals(0.94, PsiCategory.position(48), 0.01)
        assertEquals(2.43, PsiCategory.position(144), 0.01)
        // The top of a band stays inside it.
        assertEquals(1, PsiCategory.position(100).toInt())
    }

    @Test
    fun rainOutlook_readsNeaForecastWording() {
        assertEquals(RainOutlook.THUNDERY, RainOutlook.of("Heavy Thundery Showers with Gusty Winds"))
        assertEquals(RainOutlook.THUNDERY, RainOutlook.of("Thundery Showers"))
        assertEquals(RainOutlook.RAIN, RainOutlook.of("Light Rain"))
        assertEquals(RainOutlook.RAIN, RainOutlook.of("Passing Showers"))
        assertEquals(RainOutlook.DRY, RainOutlook.of("Partly Cloudy (Day)"))
        assertEquals(RainOutlook.DRY, RainOutlook.of("Hazy"))
        assertEquals(RainOutlook.DRY, RainOutlook.of("Fair (Night)"))
    }
}
