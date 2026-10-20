package au.edu.jcu.airrun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.airrun.ui.AirRunViewModel
import au.edu.jcu.airrun.ui.NowUiState
import au.edu.jcu.airrun.ui.screens.NowScreen
import au.edu.jcu.airrun.ui.screens.SettingsActions
import au.edu.jcu.airrun.ui.screens.SettingsScreen
import au.edu.jcu.airrun.ui.theme.AirRunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AirRunTheme {
                AirRunApp()
            }
        }
    }
}

/** The two bottom-navigation destinations from the starter template. */
enum class AppTab(@StringRes val label: Int, @DrawableRes val icon: Int) {
    NOW(R.string.nav_now, R.drawable.ic_air),
    SETTINGS(R.string.nav_settings, R.drawable.ic_tune),
}

/**
 * App scaffold: a bottom navigation bar switching between Now and Settings.
 * Both tabs share one [AirRunViewModel], so settings take effect immediately.
 */
@Composable
fun AirRunApp(viewModel: AirRunViewModel = viewModel(factory = AirRunViewModel.Factory)) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.NOW) }
    val nowUiState by viewModel.nowUiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Coming back to the app after a while? Fetch again if the readings are stale.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshIfStale() }

    val settingsActions = remember(viewModel) {
        SettingsActions(
            onRegionSelected = viewModel::selectRegion,
            onProfileSelected = viewModel::selectProfile,
            onIntensitySelected = viewModel::selectIntensity,
            onIncludeRainChanged = viewModel::setIncludeRain,
            onIncludeUvChanged = viewModel::setIncludeUv,
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                        label = { Text(stringResource(tab.label)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (selectedTab) {
                AppTab.NOW -> NowScreen(uiState = nowUiState, onRefresh = viewModel::refresh)
                AppTab.SETTINGS -> SettingsScreen(
                    settings = settings,
                    actions = settingsActions,
                    forecastArea = (nowUiState as? NowUiState.Ready)?.dashboard?.conditions?.forecastArea,
                )
            }
        }
    }
}
