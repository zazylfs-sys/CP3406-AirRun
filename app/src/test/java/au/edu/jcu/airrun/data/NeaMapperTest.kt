package au.edu.jcu.airrun.data

import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.remote.NeaMapper
import au.edu.jcu.airrun.data.remote.dto.NeaResponse
import au.edu.jcu.airrun.data.remote.dto.RegionReadingsData
import au.edu.jcu.airrun.data.remote.dto.TwoHourForecastData
import au.edu.jcu.airrun.data.remote.dto.UvData
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parses real NEA responses (captured from data.gov.sg on 14 Oct 2026, a haze morning)
 * stored under src/test/resources/nea.
 */
class NeaMapperTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private fun fixture(name: String): String =
        requireNotNull(javaClass.classLoader?.getResource("nea/$name")) { "missing fixture $name" }.readText()

    private val psi = json.decodeFromString<NeaResponse<RegionReadingsData>>(fixture("psi.json")).data
    private val pm25 = json.decodeFromString<NeaResponse<RegionReadingsData>>(fixture("pm25.json")).data
    private val uv = json.decodeFromString<NeaResponse<UvData>>(fixture("uv.json")).data
    private val forecast = json.decodeFromString<NeaResponse<TwoHourForecastData>>(fixture("two-hr-forecast.json")).data

    @Test
    fun psi_readsTwentyFourHourValuesForAllFiveRegions() {
        val values = NeaMapper.regionValues(psi, NeaMapper.PSI_24_HOUR)

        assertEquals(Region.entries.toSet(), values.keys)
        assertEquals(144, values[Region.WEST])
        assertEquals(106, values[Region.NORTH])
    }

    @Test
    fun pm25_readsOneHourValues() {
        val values = NeaMapper.regionValues(pm25, NeaMapper.PM25_1_HOUR)

        assertEquals(100, values[Region.WEST])
        assertEquals(104, values[Region.EAST])
    }

    @Test
    fun unknownMetric_givesEmptyMap() {
        assertTrue(NeaMapper.regionValues(psi, "not_a_metric").isEmpty())
        assertTrue(NeaMapper.regionValues(null, NeaMapper.PSI_24_HOUR).isEmpty())
    }

    @Test
    fun timestamp_isParsedWithSingaporeOffset() {
        val time = NeaMapper.latestTimestamp(pm25)

        assertNotNull(time)
        assertEquals(10, time!!.hour)
        assertEquals(8 * 3600, time.offset.totalSeconds)
    }

    @Test
    fun uv_takesTheMostRecentHour() {
        // The hourly list runs newest-first: 10:00 = 1, 09:00 = 0, ...
        assertEquals(1, NeaMapper.latestUvIndex(uv))
    }

    @Test
    fun uv_noRecords_isNull() {
        assertNull(NeaMapper.latestUvIndex(UvData(records = emptyList())))
    }

    @Test
    fun forecast_joinsEveryTownWithItsLocation() {
        val result = NeaMapper.twoHourForecast(forecast)

        assertNotNull(result)
        assertEquals(47, result!!.areas.size)
        assertEquals("10.30 am to 12.30 pm", result.validPeriod)
        val jurongWest = result.areas.first { it.area == "Jurong West" }
        assertEquals("Cloudy", jurongWest.forecast)
        assertEquals(103.705, jurongWest.location.longitude, 0.001)
    }

    @Test
    fun forecast_acceptsObjectShapedForecastText() {
        val objectShaped = """
            {"code":0,"data":{
              "area_metadata":[{"name":"Bedok","label_location":{"latitude":1.321,"longitude":103.924}}],
              "items":[{"valid_period":{"text":"1 pm to 3 pm"},
                        "forecasts":[{"area":"Bedok","forecast":{"code":"TS","text":"Thundery Showers"}}]}]
            }}
        """.trimIndent()
        val data = json.decodeFromString<NeaResponse<TwoHourForecastData>>(objectShaped).data

        assertEquals("Thundery Showers", NeaMapper.twoHourForecast(data)!!.areas.single().forecast)
    }
}
