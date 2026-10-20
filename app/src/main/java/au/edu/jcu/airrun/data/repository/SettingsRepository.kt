package au.edu.jcu.airrun.data.repository

import au.edu.jcu.airrun.data.model.RunSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the user's settings in memory. The spec doesn't require them to persist,
 * and keeping them behind a repository means DataStore could replace this later
 * without touching the ViewModel.
 */
class SettingsRepository(initial: RunSettings = RunSettings()) {

    private val _settings = MutableStateFlow(initial)
    val settings: StateFlow<RunSettings> = _settings.asStateFlow()

    fun update(transform: (RunSettings) -> RunSettings) {
        _settings.update(transform)
    }
}
