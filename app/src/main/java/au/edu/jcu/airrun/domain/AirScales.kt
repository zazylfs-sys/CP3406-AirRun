package au.edu.jcu.airrun.domain

/*
 * Official scales used to label each reading.
 *  - PSI: NEA 24-hour PSI health bands.
 *  - PM2.5: NEA 1-hour PM2.5 bands I–IV.
 *  - UV: WHO / NEA UV index categories.
 */

/** An inclusive integer range on an official scale, e.g. PSI "Good" = 0..50. */
data class Band(val low: Int, val high: Int)

/**
 * Where [value] sits on a banded scale, as `bandIndex + fractionThroughBand`.
 * PSI 48 on [PsiCategory] gives 0.94, and PSI 144 gives 2.43.
 * Values past the last band are capped just below the next whole number.
 */
fun scalePosition(value: Int, bands: List<Band>): Double {
    val index = bands.indexOfFirst { value <= it.high }.takeIf { it >= 0 } ?: bands.lastIndex
    val band = bands[index]
    val fraction = (value - band.low).toDouble() / (band.high - band.low + 1)
    return index + fraction.coerceIn(0.0, 0.99)
}

enum class PsiCategory(val band: Band) {
    GOOD(Band(0, 50)),
    MODERATE(Band(51, 100)),
    UNHEALTHY(Band(101, 200)),
    VERY_UNHEALTHY(Band(201, 300)),
    HAZARDOUS(Band(301, 500));

    companion object {
        private val bands = entries.map { it.band }
        fun of(psi: Int): PsiCategory = entries[scalePosition(psi, bands).toInt()]
        fun position(psi: Int): Double = scalePosition(psi, bands)
    }
}

enum class Pm25Band(val band: Band) {
    NORMAL(Band(0, 55)),
    ELEVATED(Band(56, 150)),
    HIGH(Band(151, 250)),
    VERY_HIGH(Band(251, 500));

    companion object {
        private val bands = entries.map { it.band }
        fun of(pm25: Int): Pm25Band = entries[scalePosition(pm25, bands).toInt()]
        fun position(pm25: Int): Double = scalePosition(pm25, bands)
    }
}

enum class UvCategory(val band: Band) {
    LOW(Band(0, 2)),
    MODERATE(Band(3, 5)),
    HIGH(Band(6, 7)),
    VERY_HIGH(Band(8, 10)),
    EXTREME(Band(11, 20));

    companion object {
        private val bands = entries.map { it.band }
        fun of(uv: Int): UvCategory = entries[scalePosition(uv, bands).toInt()]
    }
}

/** What NEA's 2-hour forecast text means for a run. */
enum class RainOutlook {
    DRY, RAIN, THUNDERY;

    companion object {
        /**
         * Classifies NEA forecast text, e.g. "Thundery Showers" -> THUNDERY,
         * "Light Rain" / "Passing Showers" -> RAIN, "Partly Cloudy (Day)" -> DRY.
         */
        fun of(forecast: String): RainOutlook {
            val text = forecast.lowercase()
            return when {
                "thunder" in text -> THUNDERY
                "rain" in text || "shower" in text || "drizzle" in text -> RAIN
                else -> DRY
            }
        }
    }
}
