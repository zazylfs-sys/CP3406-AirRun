package au.edu.jcu.airrun.data.remote

import au.edu.jcu.airrun.data.remote.dto.NeaResponse
import au.edu.jcu.airrun.data.remote.dto.RegionReadingsData
import au.edu.jcu.airrun.data.remote.dto.TwoHourForecastData
import au.edu.jcu.airrun.data.remote.dto.UvData
import retrofit2.http.GET

/**
 * Retrofit interface for Singapore's National Environment Agency (NEA) real-time data,
 * published on data.gov.sg. No API key is needed.
 *
 * Docs: https://data.gov.sg/collections?agencies=National+Environment+Agency+(NEA)
 */
interface NeaApi {

    /** Pollutant Standards Index, updated hourly. Air Run uses `psi_twenty_four_hourly`. */
    @GET("psi")
    suspend fun getPsi(): NeaResponse<RegionReadingsData>

    /** 1-hour PM2.5 concentration (µg/m³) per region, updated hourly. */
    @GET("pm25")
    suspend fun getPm25(): NeaResponse<RegionReadingsData>

    /** Island-wide UV index, updated hourly from 7 AM to 7 PM. */
    @GET("uv")
    suspend fun getUvIndex(): NeaResponse<UvData>

    /** 2-hour weather nowcast for each of ~47 towns, updated every 30 minutes. */
    @GET("two-hr-forecast")
    suspend fun getTwoHourForecast(): NeaResponse<TwoHourForecastData>

    companion object {
        const val BASE_URL = "https://api-open.data.gov.sg/v2/real-time/api/"
    }
}
