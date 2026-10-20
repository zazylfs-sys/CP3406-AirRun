package au.edu.jcu.airrun.ui

import au.edu.jcu.airrun.data.model.RunSettings
import au.edu.jcu.airrun.domain.RegionalConditions
import au.edu.jcu.airrun.domain.RunAdvice

/** Everything the Now screen draws once data has arrived. */
data class NowDashboard(
    val settings: RunSettings,
    val conditions: RegionalConditions,
    val advice: RunAdvice,
)

sealed interface NowUiState {
    /** First load, with nothing to show yet. */
    data object Loading : NowUiState

    /** First load failed, with nothing to show. */
    data object Error : NowUiState

    data class Ready(
        val dashboard: NowDashboard,
        val isRefreshing: Boolean = false,
        /** The latest refresh failed, so [dashboard] shows older data. */
        val refreshFailed: Boolean = false,
    ) : NowUiState
}
