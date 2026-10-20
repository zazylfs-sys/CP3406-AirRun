package au.edu.jcu.airrun.data.model

import java.time.OffsetDateTime

/** A latitude/longitude pair in decimal degrees. */
data class GeoPoint(val latitude: Double, val longitude: Double)

/**
 * The five NEA air-quality reporting regions.
 *
 * [apiKey] is the key used in the NEA JSON readings, and [labelLocation] is the
 * region's label point as published in the API's `regionMetadata`. It is used to
 * find the nearest 2-hour forecast town.
 */
enum class Region(val apiKey: String, val labelLocation: GeoPoint) {
    NORTH("north", GeoPoint(1.41803, 103.82)),
    CENTRAL("central", GeoPoint(1.35735, 103.82)),
    EAST("east", GeoPoint(1.35735, 103.94)),
    WEST("west", GeoPoint(1.35735, 103.70)),
    SOUTH("south", GeoPoint(1.29587, 103.82));

    companion object {
        fun fromApiKey(key: String): Region? = entries.firstOrNull { it.apiKey == key }
    }
}

/** One town's NEA 2-hour forecast, e.g. "Jurong West" -> "Partly Cloudy (Day)". */
data class AreaForecast(
    val area: String,
    val location: GeoPoint,
    val forecast: String,
)

/** The latest NEA 2-hour nowcast for every town. */
data class TwoHourForecast(
    val validPeriod: String,
    val areas: List<AreaForecast>,
)

/**
 * A snapshot of everything Air Run fetched in one refresh, for all regions at once.
 * Switching region in Settings only re-reads this snapshot; it doesn't fetch again.
 *
 * UV and the forecast are optional: they are shown when available but are not
 * essential, so a failure to load them doesn't block the main verdict.
 */
data class AirReadings(
    val psi24Hour: Map<Region, Int>,
    val pm25OneHour: Map<Region, Int>,
    val airMeasuredAt: OffsetDateTime?,
    val uvIndex: Int?,
    val forecast: TwoHourForecast?,
)
