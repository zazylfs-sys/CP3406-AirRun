package au.edu.jcu.airrun.di

import au.edu.jcu.airrun.data.remote.NeaApi
import au.edu.jcu.airrun.data.repository.AirQualityRepository
import au.edu.jcu.airrun.data.repository.NetworkAirQualityRepository
import au.edu.jcu.airrun.data.repository.SettingsRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Manual dependency injection: the app-wide objects, created once in AirRunApplication. */
interface AppContainer {
    val airQualityRepository: AirQualityRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer : AppContainer {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val neaApi: NeaApi by lazy {
        Retrofit.Builder()
            .baseUrl(NeaApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NeaApi::class.java)
    }

    override val airQualityRepository: AirQualityRepository by lazy {
        NetworkAirQualityRepository(neaApi)
    }

    override val settingsRepository: SettingsRepository by lazy { SettingsRepository() }
}
