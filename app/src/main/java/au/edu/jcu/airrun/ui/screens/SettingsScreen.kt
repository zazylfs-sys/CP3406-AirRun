package au.edu.jcu.airrun.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.edu.jcu.airrun.R
import au.edu.jcu.airrun.data.model.Intensity
import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.model.RunSettings
import au.edu.jcu.airrun.data.model.RunnerProfile
import au.edu.jcu.airrun.ui.components.ChoiceGrid
import au.edu.jcu.airrun.ui.components.SegmentedChoice
import au.edu.jcu.airrun.ui.components.SettingsSection
import au.edu.jcu.airrun.ui.components.SwitchRow
import au.edu.jcu.airrun.ui.label
import au.edu.jcu.airrun.ui.theme.AirRunTheme

/** Callbacks for every control on the Settings screen. */
data class SettingsActions(
    val onRegionSelected: (Region) -> Unit = {},
    val onProfileSelected: (RunnerProfile) -> Unit = {},
    val onIntensitySelected: (Intensity) -> Unit = {},
    val onIncludeRainChanged: (Boolean) -> Unit = {},
    val onIncludeUvChanged: (Boolean) -> Unit = {},
)

/**
 * Settings that change what the Now screen shows and how it judges the readings.
 *
 * @param forecastArea the town the 2-hour forecast is taken from, once known.
 */
@Composable
fun SettingsScreen(
    settings: RunSettings,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
    forecastArea: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            Modifier.padding(start = 4.dp, top = 20.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.settings_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingsSection(
            title = stringResource(R.string.settings_where),
            help = if (forecastArea != null) stringResource(R.string.settings_where_help_town, forecastArea)
            else stringResource(R.string.settings_where_help),
        ) {
            ChoiceGrid(
                options = Region.entries,
                selected = settings.region,
                onSelect = actions.onRegionSelected,
                label = { stringResource(it.label) },
            )
        }

        SettingsSection(
            title = stringResource(R.string.settings_who),
            help = stringResource(R.string.settings_who_help),
        ) {
            SegmentedChoice(
                options = RunnerProfile.entries,
                selected = settings.profile,
                onSelect = actions.onProfileSelected,
                label = { stringResource(it.label) },
            )
        }

        SettingsSection(
            title = stringResource(R.string.settings_intensity),
            help = stringResource(R.string.settings_intensity_help),
        ) {
            SegmentedChoice(
                options = Intensity.entries,
                selected = settings.intensity,
                onSelect = actions.onIntensitySelected,
                label = { stringResource(it.label) },
            )
        }

        SettingsSection(title = stringResource(R.string.settings_include)) {
            Column {
                SwitchRow(
                    title = stringResource(R.string.include_rain),
                    help = stringResource(R.string.include_rain_help),
                    checked = settings.includeRain,
                    onCheckedChange = actions.onIncludeRainChanged,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SwitchRow(
                    title = stringResource(R.string.include_uv),
                    help = stringResource(R.string.include_uv_help),
                    checked = settings.includeUv,
                    onCheckedChange = actions.onIncludeUvChanged,
                )
            }
        }

        Text(
            text = stringResource(R.string.settings_footnote),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Preview(showBackground = true, heightDp = 900, backgroundColor = 0xFFF3F5F4)
@Composable
private fun SettingsScreenPreview() {
    AirRunTheme(darkTheme = false) {
        SettingsScreen(
            settings = RunSettings(includeUv = false),
            actions = SettingsActions(),
            forecastArea = "Jurong West",
        )
    }
}
