package au.edu.jcu.airrun.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/*
 * DTOs for the NEA real-time APIs on data.gov.sg (v2).
 * Only the fields Air Run uses are declared; the JSON parser ignores the rest.
 */

/** Every v2 endpoint wraps its payload as `{ "code": 0, "data": {...}, "errorMsg": "" }`. */
@Serializable
data class NeaResponse<T>(
    val code: Int,
    val data: T? = null,
    val errorMsg: String? = null,
)

// --- PSI and PM2.5 (same shape: readings keyed by region) ---

@Serializable
data class RegionReadingsData(
    val items: List<RegionReadingsItem> = emptyList(),
)

@Serializable
data class RegionReadingsItem(
    val timestamp: String,
    /** e.g. `"psi_twenty_four_hourly" -> { "west": 48, "east": 52, ... }` */
    val readings: Map<String, Map<String, Double>> = emptyMap(),
)

// --- UV index (island-wide, hourly) ---

@Serializable
data class UvData(
    val records: List<UvRecord> = emptyList(),
)

@Serializable
data class UvRecord(
    val index: List<UvHourlyValue> = emptyList(),
)

@Serializable
data class UvHourlyValue(
    val hour: String,
    val value: Double,
)

// --- 2-hour weather forecast (per town) ---

@Serializable
data class TwoHourForecastData(
    @SerialName("area_metadata") val areaMetadata: List<AreaMetadata> = emptyList(),
    val items: List<TwoHourForecastItem> = emptyList(),
)

@Serializable
data class AreaMetadata(
    val name: String,
    @SerialName("label_location") val labelLocation: LatLng,
)

@Serializable
data class LatLng(val latitude: Double, val longitude: Double)

@Serializable
data class TwoHourForecastItem(
    @SerialName("valid_period") val validPeriod: ValidPeriod? = null,
    val forecasts: List<AreaForecastDto> = emptyList(),
)

@Serializable
data class ValidPeriod(val text: String = "")

@Serializable
data class AreaForecastDto(
    val area: String,
    /**
     * Usually a plain string such as "Partly Cloudy (Day)", but some NEA endpoints
     * return `{ "code": "PC", "text": "Partly Cloudy" }`, so both shapes are accepted.
     */
    val forecast: JsonElement,
)
