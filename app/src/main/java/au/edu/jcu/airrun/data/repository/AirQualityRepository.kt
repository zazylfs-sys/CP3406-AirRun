package au.edu.jcu.airrun.data.repository

import au.edu.jcu.airrun.data.model.AirReadings
import au.edu.jcu.airrun.data.remote.NeaApi
import au.edu.jcu.airrun.data.remote.NeaMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.IOException

/** Source of the air and weather readings shown on the Now screen. */
interface AirQualityRepository {
    /**
     * Fetches the latest readings for all regions.
     * @throws AirDataUnavailableException if neither PSI nor PM2.5 could be loaded.
     */
    suspend fun fetchReadings(): AirReadings
}

class AirDataUnavailableException(cause: Throwable?) :
    IOException("NEA air-quality data is unavailable", cause)

/**
 * Loads the four NEA feeds in parallel, so one refresh takes about as long as the
 * slowest single request.
 *
 * Each feed fails on its own: if UV or the forecast is down, the verdict still
 * works from PSI and PM2.5. Only losing both air-quality feeds counts as an error.
 */
class NetworkAirQualityRepository(
    private val api: NeaApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AirQualityRepository {

    override suspend fun fetchReadings(): AirReadings = withContext(ioDispatcher) {
        coroutineScope {
            val psiCall = async { attempt { api.getPsi().data } }
            val pm25Call = async { attempt { api.getPm25().data } }
            val uvCall = async { attempt { api.getUvIndex().data } }
            val forecastCall = async { attempt { api.getTwoHourForecast().data } }

            val psi = psiCall.await()
            val pm25 = pm25Call.await()
            val psiValues = NeaMapper.regionValues(psi.getOrNull(), NeaMapper.PSI_24_HOUR)
            val pm25Values = NeaMapper.regionValues(pm25.getOrNull(), NeaMapper.PM25_1_HOUR)
            if (psiValues.isEmpty() && pm25Values.isEmpty()) {
                throw AirDataUnavailableException(psi.exceptionOrNull() ?: pm25.exceptionOrNull())
            }

            AirReadings(
                psi24Hour = psiValues,
                pm25OneHour = pm25Values,
                airMeasuredAt = NeaMapper.latestTimestamp(pm25.getOrNull())
                    ?: NeaMapper.latestTimestamp(psi.getOrNull()),
                uvIndex = NeaMapper.latestUvIndex(uvCall.await().getOrNull()),
                forecast = NeaMapper.twoHourForecast(forecastCall.await().getOrNull()),
            )
        }
    }

    /** Like runCatching, but never swallows coroutine cancellation. */
    private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
