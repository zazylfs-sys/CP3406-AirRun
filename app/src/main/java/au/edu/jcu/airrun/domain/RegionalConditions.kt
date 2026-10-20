package au.edu.jcu.airrun.domain

import au.edu.jcu.airrun.data.model.AirReadings
import au.edu.jcu.airrun.data.model.AreaForecast
import au.edu.jcu.airrun.data.model.GeoPoint
import au.edu.jcu.airrun.data.model.Region
import java.time.OffsetDateTime
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** The readings that matter for one region, ready for [RunAdvisor] and the Now screen. */
data class RegionalConditions(
    val region: Region,
    val psi24Hour: Int?,
    val pm25OneHour: Int?,
    val uvIndex: Int?,
    /** The forecast town closest to the region, e.g. "Jurong West" for [Region.WEST]. */
    val forecastArea: String?,
    val forecast: String?,
    val measuredAt: OffsetDateTime?,
)

/** Picks out [region]'s readings, pairing it with the nearest forecast town. */
fun AirReadings.conditionsFor(region: Region): RegionalConditions {
    val nearest = forecast?.areas?.let { nearestArea(region.labelLocation, it) }
    return RegionalConditions(
        region = region,
        psi24Hour = psi24Hour[region],
        pm25OneHour = pm25OneHour[region],
        uvIndex = uvIndex,
        forecastArea = nearest?.area,
        forecast = nearest?.forecast,
        measuredAt = airMeasuredAt,
    )
}

/** The forecast area closest to [point], measured along the Earth's surface. */
fun nearestArea(point: GeoPoint, areas: List<AreaForecast>): AreaForecast? =
    areas.minByOrNull { distanceKm(point, it.location) }

/** Great-circle distance (haversine formula). */
fun distanceKm(a: GeoPoint, b: GeoPoint): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val h = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLon / 2).pow(2)
    return 2 * earthRadiusKm * asin(sqrt(h))
}
