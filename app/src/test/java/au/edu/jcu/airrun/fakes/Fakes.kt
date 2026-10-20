package au.edu.jcu.airrun.fakes

import au.edu.jcu.airrun.data.model.AirReadings
import au.edu.jcu.airrun.data.model.AreaForecast
import au.edu.jcu.airrun.data.model.GeoPoint
import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.model.TwoHourForecast
import au.edu.jcu.airrun.data.remote.NeaApi
import au.edu.jcu.airrun.data.remote.dto.NeaResponse
import au.edu.jcu.airrun.data.remote.dto.RegionReadingsData
import au.edu.jcu.airrun.data.remote.dto.RegionReadingsItem
import au.edu.jcu.airrun.data.remote.dto.TwoHourForecastData
import au.edu.jcu.airrun.data.remote.dto.UvData
import au.edu.jcu.airrun.data.remote.dto.UvHourlyValue
import au.edu.jcu.airrun.data.remote.dto.UvRecord
import au.edu.jcu.airrun.data.repository.AirQualityRepository
import java.io.IOException

/** Repository that returns queued results in order, then repeats the last one. */
class FakeAirQualityRepository(results: List<Result<AirReadings>>) : AirQualityRepository {
    constructor(readings: AirReadings) : this(listOf(Result.success(readings)))

    private val queue = ArrayDeque(results)
    var calls = 0
        private set

    override suspend fun fetchReadings(): AirReadings {
        calls++
        val next = if (queue.size > 1) queue.removeFirst() else queue.first()
        return next.getOrThrow()
    }
}

/** NeaApi stub where each feed can be made to fail. */
class FakeNeaApi(
    private val psiFails: Boolean = false,
    private val pm25Fails: Boolean = false,
    private val uvFails: Boolean = false,
    private val forecastFails: Boolean = false,
) : NeaApi {

    private fun readings(metric: String, value: Double) = NeaResponse(
        code = 0,
        data = RegionReadingsData(
            items = listOf(
                RegionReadingsItem(
                    timestamp = "2026-10-14T10:00:00+08:00",
                    readings = mapOf(metric to Region.entries.associate { it.apiKey to value }),
                )
            )
        ),
    )

    override suspend fun getPsi(): NeaResponse<RegionReadingsData> {
        if (psiFails) throw IOException("psi down")
        return readings("psi_twenty_four_hourly", 48.0)
    }

    override suspend fun getPm25(): NeaResponse<RegionReadingsData> {
        if (pm25Fails) throw IOException("pm25 down")
        return readings("pm25_one_hourly", 22.0)
    }

    override suspend fun getUvIndex(): NeaResponse<UvData> {
        if (uvFails) throw IOException("uv down")
        return NeaResponse(0, UvData(listOf(UvRecord(listOf(UvHourlyValue("2026-10-14T10:00:00+08:00", 6.0))))))
    }

    override suspend fun getTwoHourForecast(): NeaResponse<TwoHourForecastData> {
        if (forecastFails) throw IOException("forecast down")
        return NeaResponse(0, TwoHourForecastData())
    }
}

object TestReadings {
    val clear = AirReadings(
        psi24Hour = Region.entries.associateWith { 48 },
        pm25OneHour = Region.entries.associateWith { 22 },
        airMeasuredAt = null,
        uvIndex = 6,
        forecast = TwoHourForecast(
            validPeriod = "",
            areas = listOf(AreaForecast("Jurong West", GeoPoint(1.34039, 103.705), "Partly Cloudy (Day)")),
        ),
    )

    /** Unhealthy PSI and Band II PM2.5: caution for most people, avoid for sensitive runners. */
    val haze = clear.copy(
        psi24Hour = Region.entries.associateWith { 142 },
        pm25OneHour = Region.entries.associateWith { 100 },
    )
}
