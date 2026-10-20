package au.edu.jcu.airrun.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import au.edu.jcu.airrun.AirRunApplication
import au.edu.jcu.airrun.data.model.AirReadings
import au.edu.jcu.airrun.data.model.Intensity
import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.model.RunSettings
import au.edu.jcu.airrun.data.model.RunnerProfile
import au.edu.jcu.airrun.data.repository.AirQualityRepository
import au.edu.jcu.airrun.data.repository.SettingsRepository
import au.edu.jcu.airrun.domain.RunAdvisor
import au.edu.jcu.airrun.domain.conditionsFor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Shared by the Now and Settings screens. Settings changes recombine with the last
 * fetched readings straight away, with no new network call, so the verdict updates
 * the moment a toggle flips.
 */
class AirRunViewModel(
    private val airQualityRepository: AirQualityRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    private data class FetchState(
        val readings: AirReadings? = null,
        val fetchedAt: Instant? = null,
        val isRefreshing: Boolean = false,
        val lastAttemptFailed: Boolean = false,
    )

    private val fetchState = MutableStateFlow(FetchState())
    private var refreshJob: Job? = null

    val settings: StateFlow<RunSettings> = settingsRepository.settings

    val nowUiState: StateFlow<NowUiState> =
        combine(fetchState, settings, ::toUiState).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NowUiState.Loading,
        )

    init {
        refresh()
    }

    /** Fetches fresh readings. Ignored while a refresh is already running. */
    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            fetchState.update { it.copy(isRefreshing = true) }
            try {
                val readings = airQualityRepository.fetchReadings()
                fetchState.value = FetchState(readings = readings, fetchedAt = clock.instant())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep any previous readings on screen and flag the failure.
                fetchState.update { it.copy(isRefreshing = false, lastAttemptFailed = true) }
            }
        }
    }

    /**
     * Called when the app returns to the foreground. NEA updates air readings hourly
     * and the forecast every 30 minutes, so data older than [STALE_AFTER] is refetched.
     */
    fun refreshIfStale() {
        val fetchedAt = fetchState.value.fetchedAt
        if (fetchedAt == null || Duration.between(fetchedAt, clock.instant()) >= STALE_AFTER) {
            refresh()
        }
    }

    fun selectRegion(region: Region) = settingsRepository.update { it.copy(region = region) }

    fun selectProfile(profile: RunnerProfile) = settingsRepository.update { it.copy(profile = profile) }

    fun selectIntensity(intensity: Intensity) = settingsRepository.update { it.copy(intensity = intensity) }

    fun setIncludeRain(include: Boolean) = settingsRepository.update { it.copy(includeRain = include) }

    fun setIncludeUv(include: Boolean) = settingsRepository.update { it.copy(includeUv = include) }

    private fun toUiState(fetch: FetchState, settings: RunSettings): NowUiState {
        val readings = fetch.readings ?: return when {
            fetch.lastAttemptFailed && !fetch.isRefreshing -> NowUiState.Error
            else -> NowUiState.Loading
        }
        val conditions = readings.conditionsFor(settings.region)
        return NowUiState.Ready(
            dashboard = NowDashboard(
                settings = settings,
                conditions = conditions,
                advice = RunAdvisor.advise(conditions, settings),
            ),
            isRefreshing = fetch.isRefreshing,
            refreshFailed = fetch.lastAttemptFailed,
        )
    }

    companion object {
        val STALE_AFTER: Duration = Duration.ofMinutes(15)

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as AirRunApplication).container
                AirRunViewModel(container.airQualityRepository, container.settingsRepository)
            }
        }
    }
}
