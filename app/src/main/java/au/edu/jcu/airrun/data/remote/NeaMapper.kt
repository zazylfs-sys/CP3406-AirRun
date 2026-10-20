package au.edu.jcu.airrun.data.remote

import au.edu.jcu.airrun.data.model.AreaForecast
import au.edu.jcu.airrun.data.model.GeoPoint
import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.model.TwoHourForecast
import au.edu.jcu.airrun.data.remote.dto.AreaForecastDto
import au.edu.jcu.airrun.data.remote.dto.RegionReadingsData
import au.edu.jcu.airrun.data.remote.dto.TwoHourForecastData
import au.edu.jcu.airrun.data.remote.dto.UvData
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

/** Converts NEA DTOs into Air Run's own models. Pure functions, so they're easy to unit test. */
object NeaMapper {

    const val PSI_24_HOUR = "psi_twenty_four_hourly"
    const val PM25_1_HOUR = "pm25_one_hourly"

    /**
     * Reads one metric (e.g. [PSI_24_HOUR]) from the newest item, keyed by [Region].
     * Unknown keys such as "national" are skipped.
     */
    fun regionValues(data: RegionReadingsData?, metric: String): Map<Region, Int> {
        val latest = data?.items?.maxByOrNull { it.timestamp } ?: return emptyMap()
        val values = latest.readings[metric] ?: return emptyMap()
        return values.mapNotNull { (key, value) ->
            Region.fromApiKey(key)?.let { region -> region to value.roundToInt() }
        }.toMap()
    }

    /** Timestamp of the newest reading, or null if there is none or it can't be parsed. */
    fun latestTimestamp(data: RegionReadingsData?): OffsetDateTime? =
        data?.items?.maxByOrNull { it.timestamp }?.timestamp?.let(::parseTimestamp)

    /** The most recent hourly UV index. */
    fun latestUvIndex(data: UvData?): Int? =
        data?.records
            ?.flatMap { it.index }
            ?.maxByOrNull { it.hour }
            ?.value
            ?.roundToInt()

    /** Joins each town's forecast with its location from `area_metadata`. */
    fun twoHourForecast(data: TwoHourForecastData?): TwoHourForecast? {
        val item = data?.items?.lastOrNull() ?: return null
        val locations = data.areaMetadata.associate {
            it.name to GeoPoint(it.labelLocation.latitude, it.labelLocation.longitude)
        }
        val areas = item.forecasts.mapNotNull { dto ->
            val location = locations[dto.area] ?: return@mapNotNull null
            val text = forecastText(dto) ?: return@mapNotNull null
            AreaForecast(area = dto.area, location = location, forecast = text)
        }
        if (areas.isEmpty()) return null
        return TwoHourForecast(validPeriod = item.validPeriod?.text.orEmpty(), areas = areas)
    }

    private fun forecastText(dto: AreaForecastDto): String? = when (val element = dto.forecast) {
        is JsonPrimitive -> element.contentOrNull
        is JsonObject -> (element["text"] as? JsonPrimitive)?.contentOrNull
        else -> null
    }

    private fun parseTimestamp(text: String): OffsetDateTime? = try {
        OffsetDateTime.parse(text)
    } catch (_: DateTimeParseException) {
        null
    }
}
