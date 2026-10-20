package au.edu.jcu.airrun.domain

import au.edu.jcu.airrun.data.model.AirReadings
import au.edu.jcu.airrun.data.model.AreaForecast
import au.edu.jcu.airrun.data.model.GeoPoint
import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.model.TwoHourForecast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegionalConditionsTest {

    // A few real towns from NEA's area_metadata.
    private val towns = listOf(
        AreaForecast("Jurong West", GeoPoint(1.34039, 103.705), "Cloudy"),
        AreaForecast("Tampines", GeoPoint(1.345, 103.944), "Light Rain"),
        AreaForecast("Mandai", GeoPoint(1.419, 103.812), "Fair (Day)"),
        AreaForecast("Bukit Merah", GeoPoint(1.277, 103.819), "Hazy"),
    )

    private val readings = AirReadings(
        psi24Hour = mapOf(Region.WEST to 144, Region.EAST to 139),
        pm25OneHour = mapOf(Region.WEST to 100),
        airMeasuredAt = null,
        uvIndex = 4,
        forecast = TwoHourForecast("10.30 am to 12.30 pm", towns),
    )

    @Test
    fun conditionsFor_picksTheRegionsReadings_andNearestTown() {
        val west = readings.conditionsFor(Region.WEST)

        assertEquals(144, west.psi24Hour)
        assertEquals(100, west.pm25OneHour)
        assertEquals(4, west.uvIndex)
        assertEquals("Jurong West", west.forecastArea)
        assertEquals("Cloudy", west.forecast)
    }

    @Test
    fun conditionsFor_eachRegionMapsToASensibleTown() {
        assertEquals("Tampines", readings.conditionsFor(Region.EAST).forecastArea)
        assertEquals("Mandai", readings.conditionsFor(Region.NORTH).forecastArea)
        assertEquals("Bukit Merah", readings.conditionsFor(Region.SOUTH).forecastArea)
    }

    @Test
    fun conditionsFor_missingRegionReading_isNull() {
        assertNull(readings.conditionsFor(Region.EAST).pm25OneHour)
    }

    @Test
    fun distanceKm_matchesKnownDistance() {
        // Changi to Tuas is roughly 40 km across the island.
        val changi = GeoPoint(1.357, 103.987)
        val tuas = GeoPoint(1.294, 103.635)
        assertEquals(39.8, distanceKm(changi, tuas), 1.0)
    }
}
